package com.ermetic.dosserver.services.time_frame;

public interface IFrameTimeService {
    TimeFrame getClientTimeFrame(int clientId);

    void createClientTimeFrame(int clientId);
    void updateClientTimeFrame(int clientId, TimeFrame updatedTimeFrame);
}
