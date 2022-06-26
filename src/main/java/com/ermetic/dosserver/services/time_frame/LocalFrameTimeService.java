package com.ermetic.dosserver.services.time_frame;

import com.ermetic.dosserver.config.DosServerConfig;
import com.ermetic.dosserver.services.sync.ISyncService;
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
public class LocalFrameTimeService implements IFrameTimeService {
    private static final Logger logger = LoggerFactory.getLogger(LocalFrameTimeService.class);

    @Autowired
    private ISyncService syncService;

    @Autowired
    private DosServerConfig serverConfig;

    private final Map<Integer, TimeFrame> timeFrameMap;
    private final TaskScheduler cleanupScheduler;

    public LocalFrameTimeService() {
        timeFrameMap = new ConcurrentHashMap<>();
        ScheduledExecutorService localExecutor = Executors.newSingleThreadScheduledExecutor();
        cleanupScheduler = new ConcurrentTaskScheduler(localExecutor);
    }

    @Override
    public TimeFrame getClientTimeFrame(int clientId) {
        return timeFrameMap.get(clientId);
    }

    @Override
    public void createClientTimeFrame(int clientId) {
        TimeFrame newTimeFrame = new TimeFrame(Instant.now(), 1);
        timeFrameMap.put(clientId, newTimeFrame);

        // register cleanup task
        Instant cleanUpAtTime = newTimeFrame.getStartTime().plusMillis(serverConfig.getTimeFrameDurationMS());
        cleanupScheduler.schedule(new CleanupTask(clientId, newTimeFrame), cleanUpAtTime);
    }

    @Override
    public void updateClientTimeFrame(int clientId, TimeFrame updatedTimeFrame) {
        timeFrameMap.put(clientId, updatedTimeFrame);
    }


    private class CleanupTask implements Runnable {

        private final int clientId;
        private final TimeFrame timeFrame;

        private CleanupTask(int clientId, TimeFrame timeFrame) {
            this.clientId = clientId;
            this.timeFrame = timeFrame;
        }


        @Override
        public void run() {
            syncService.execute(clientId, () -> {
                TimeFrame timeFrameInCache = timeFrameMap.get(clientId);
                /*
                  Removing only if time frame in cache is the same time frame and not a new one.
                  For edge cases like - on frame end time, before the cleanup is starts,
                  cache is updated with new frame for the same client id. cleanup should not remove it then.
                 */
                if (timeFrameInCache.equals(timeFrame)) {
                    logger.info("Removing client {} time frame", clientId);
                    timeFrameMap.remove(clientId);
                }
            });
        }
    }
}
