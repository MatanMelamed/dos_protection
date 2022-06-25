package com.ermetic.dosserver.services.time_frame_managers;

import com.ermetic.dosserver.services.ClientTimeFrame;
import com.ermetic.dosserver.services.IClientFrameTimeManager;
import com.ermetic.dosserver.sync.ClientIdSyncExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Service
public class LocalCacheClientFrameTimeManager implements IClientFrameTimeManager {

    @Autowired
    private ClientIdSyncExecutor clientIdSyncExecutor;

    private final Map<Integer, ClientTimeFrame> timeFrameMap;
    private final TaskScheduler cleanupScheduler;

    int timeFrameDurationMS = 5000;

    public LocalCacheClientFrameTimeManager() {
        this.timeFrameMap = new ConcurrentHashMap<>();

        ScheduledExecutorService localExecutor = Executors.newSingleThreadScheduledExecutor();
        cleanupScheduler = new ConcurrentTaskScheduler(localExecutor);
    }

    @Override
    public ClientTimeFrame getClientTimeFrame(int clientId) {
        return timeFrameMap.get(clientId);
    }

    @Override
    public void createClientTimeFrame(int clientId) {
        ClientTimeFrame newTimeFrame = new ClientTimeFrame(Instant.now(), 1);
        timeFrameMap.put(clientId, newTimeFrame);

        // register cleanup task
        Instant cleanUpAtTime = newTimeFrame.getStartTime().plusMillis(timeFrameDurationMS);
        cleanupScheduler.schedule(new CleanupTask(clientId, newTimeFrame), cleanUpAtTime);
    }

    @Override
    public void updateClientTimeFrame(int clientId, ClientTimeFrame updatedTimeFrame) {
        timeFrameMap.put(clientId, updatedTimeFrame);
    }


    private class CleanupTask implements Runnable {

        private final int clientId;
        private final ClientTimeFrame clientTimeFrame;

        private CleanupTask(int clientId, ClientTimeFrame clientTimeFrame) {
            this.clientId = clientId;
            this.clientTimeFrame = clientTimeFrame;
        }


        @Override
        public void run() {
            clientIdSyncExecutor.execute(clientId, () -> {
                ClientTimeFrame timeFrameInCache = timeFrameMap.get(clientId);
                /**
                 * Removing only if time frame in cache is the same time frame and not a new one.
                 * For edge cases like - on frame end time, before the cleanup is starts,
                 * cache is updated with new frame for the same client id. cleanup should not remove it then.
                 */
                if (timeFrameInCache.equals(clientTimeFrame)) {
                    timeFrameMap.remove(clientId);
                }
            });
        }
    }
}
