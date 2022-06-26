package com.ermetic.dosserver.services.dos_protection;

import com.ermetic.dosserver.config.DosServerConfig;
import com.ermetic.dosserver.services.time_frame.IFrameTimeService;
import com.ermetic.dosserver.services.time_frame.TimeFrame;
import com.ermetic.dosserver.services.sync.ISyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class DosProtectionService implements IDosProtectionService {
    private static final Logger logger = LoggerFactory.getLogger(DosProtectionService.class);

    @Autowired
    private ISyncService syncService;

    @Autowired
    private IFrameTimeService frameTimeService;

    @Autowired
    private DosServerConfig serverConfig;

    @Override
    public boolean isClientReachedMaxRequest(int clientId) {
        // run method as synchronized block on client id
        return syncService.evaluate(clientId, () -> this.isClientReachMaxRequestInternal(clientId));
    }

    private boolean isClientReachMaxRequestInternal(int clientId) {
        boolean result = true;

        TimeFrame timeFrame = frameTimeService.getClientTimeFrame(clientId);
        logger.info("Processing request for client {} with time frame {}", clientId, timeFrame);

        if (timeFrame != null && isTimeFrameStillOpen(timeFrame)) {
            logger.info("Time frame is open for client {}", clientId);
            if (!isTimeFrameRequestCountReached(timeFrame)) {
                logger.info("Time frame did not reach max count");
                TimeFrame updatedTimeFrame = timeFrame.increaseCounter();
                frameTimeService.updateClientTimeFrame(clientId, updatedTimeFrame);
                result = false;
            } else {
                logger.info("Time frame reached max count");
            }
        } else {
            logger.debug("No time frame is open for client {}, creating a new one", clientId);
            frameTimeService.createClientTimeFrame(clientId);
            result = false;
        }

        logger.info("Processing request for client {} finished with {}", clientId, result);
        return result;
    }

    private boolean isTimeFrameStillOpen(TimeFrame timeFrame) {
        Instant timeFrameEndTime = timeFrame.getStartTime().plusMillis(serverConfig.getTimeFrameDurationMS());
        return timeFrameEndTime.isAfter(Instant.now());
    }

    private boolean isTimeFrameRequestCountReached(TimeFrame timeFrame) {
        return timeFrame.getRequestsCount() >= serverConfig.getTimeFrameMaxRequest();
    }
}
