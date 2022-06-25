package com.ermetic.dosserver.services.local_dos_protection;

import com.ermetic.dosserver.config.DosServerConfig;
import com.ermetic.dosserver.sync.ClientIdSyncExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class LocalClientFrameTimeManager implements IClientFrameTimeManager {
    private static final Logger logger = LoggerFactory.getLogger(LocalClientFrameTimeManager.class);

    @Autowired
    private ClientIdSyncExecutor clientIdSyncExecutor;

    @Autowired
    private DosServerConfig serverConfig;

    private final Map<Integer, ClientTimeFrame> timeFrameMap = new ConcurrentHashMap<>();
    private final TaskScheduler cleanupScheduler;

    public LocalClientFrameTimeManager() {
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
        Instant cleanUpAtTime = newTimeFrame.getStartTime().plusMillis(serverConfig.getTimeFrameDurationMS());
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
                /*
                  Removing only if time frame in cache is the same time frame and not a new one.
                  For edge cases like - on frame end time, before the cleanup is starts,
                  cache is updated with new frame for the same client id. cleanup should not remove it then.
                 */
                if (timeFrameInCache.equals(clientTimeFrame)) {
                    logger.debug("Removing client {} time frame", clientId);
                    timeFrameMap.remove(clientId);
                }
            });
        }
    }
}
