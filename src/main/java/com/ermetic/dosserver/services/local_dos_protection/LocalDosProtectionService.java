package com.ermetic.dosserver.services.local_dos_protection;

import com.ermetic.dosserver.config.DosServerConfig;
import com.ermetic.dosserver.services.IDosProtectionService;
import com.ermetic.dosserver.sync.ClientIdSyncExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class LocalDosProtectionService implements IDosProtectionService {
    private static final Logger logger = LoggerFactory.getLogger(LocalDosProtectionService.class);

    @Autowired
    private ClientIdSyncExecutor syncExecutor;

    @Autowired
    private IClientFrameTimeManager frameTimeManager;

    @Autowired
    private DosServerConfig serverConfig;

    @Override
    public boolean isClientReachedMaxRequest(int clientId) {
        // run method as synchronized block on client id
        return syncExecutor.evaluate(clientId, () -> this.isClientReachMaxRequestInternal(clientId));
    }

    private boolean isClientReachMaxRequestInternal(int clientId) {
        boolean result = true;

        ClientTimeFrame clientTimeFrame = frameTimeManager.getClientTimeFrame(clientId);
        logger.info("Processing request for client {} with time frame {}", clientId, clientTimeFrame);

        if (clientTimeFrame != null && isTimeFrameStillOpen(clientTimeFrame)) {
            logger.debug("Time frame is open for client {}", clientId);
            if (!isTimeFrameRequestCountReached(clientTimeFrame)) {
                logger.debug("Time frame did not reach max count");
                ClientTimeFrame updatedTimeFrame = clientTimeFrame.increaseCounter();
                frameTimeManager.updateClientTimeFrame(clientId, updatedTimeFrame);
                result = false;
            } else {
                logger.debug("Time frame reached max count");
            }
        } else {
            logger.debug("No time frame is open for client {}, creating a new one", clientId);
            frameTimeManager.createClientTimeFrame(clientId);
            result = false;
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        logger.info("Processing request for client {} finished with {}", clientId, result);
        return result;
    }

    private boolean isTimeFrameStillOpen(ClientTimeFrame timeFrame) {
        Instant timeFrameEndTime = timeFrame.getStartTime().plusMillis(serverConfig.getTimeFrameDurationMS());
        return timeFrameEndTime.isAfter(Instant.now());
    }

    private boolean isTimeFrameRequestCountReached(ClientTimeFrame timeFrame) {
        return timeFrame.getRequestsCount() >= serverConfig.getTimeFrameMaxRequest();
    }
}
