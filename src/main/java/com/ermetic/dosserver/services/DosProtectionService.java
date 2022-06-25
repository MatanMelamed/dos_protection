package com.ermetic.dosserver.services;

import com.ermetic.dosserver.sync.ClientIdSyncExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class DosProtectionService implements IDosProtectionService {
    private static final Logger logger = LoggerFactory.getLogger(DosProtectionService.class);

    @Autowired
    private ClientIdSyncExecutor syncExecutor;

    @Autowired
    private IClientFrameTimeManager frameTimeManager;

    int requestCountThreshold = 5;
    int clientTimeFrameDurationInMS = 5000;

    @Override
    public boolean isClientReachedMaxRequest(int clientId) {
        // sync method execution on client ID between threads
        return syncExecutor.evaluate(clientId, () -> this.isClientReachMaxRequestInternal(clientId));
    }

    private boolean isClientReachMaxRequestInternal(int clientId) {
        boolean result = true;

        ClientTimeFrame clientTimeFrame = frameTimeManager.getClientTimeFrame(clientId);
        logger.info("Processing request for client {} with time frame {}", clientId, clientTimeFrame);

        if (clientTimeFrame != null && isTimeFrameStillOpen(clientTimeFrame)) {
            logger.info("Time frame is open for client {}", clientId);
            if (!isTimeFrameRequestCountReached(clientTimeFrame)) {
                logger.info("Time frame did not reach max count");
                ClientTimeFrame updatedTimeFrame = clientTimeFrame.increaseCounter();
                frameTimeManager.updateClientTimeFrame(clientId, updatedTimeFrame);
                result = false;
            } else {
                logger.info("Time frame reached max count");
            }
        } else {
            logger.info("No time frame is open for client {}, creating a new one", clientId);
            frameTimeManager.createClientTimeFrame(clientId);
            result = false;
        }

        logger.info("Processing request for client {} finished with {}", clientId, result);
        return result;
    }

    private boolean isTimeFrameStillOpen(ClientTimeFrame timeFrame) {
        Instant timeFrameEndTime = timeFrame.getStartTime().plusMillis(clientTimeFrameDurationInMS);
        return timeFrameEndTime.isAfter(Instant.now());
    }

    private boolean isTimeFrameRequestCountReached(ClientTimeFrame timeFrame) {
        return timeFrame.getRequestsCount() >= requestCountThreshold;
    }
}
