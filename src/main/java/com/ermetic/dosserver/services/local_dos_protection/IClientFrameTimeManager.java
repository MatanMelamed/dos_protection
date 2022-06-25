package com.ermetic.dosserver.services.local_dos_protection;

public interface IClientFrameTimeManager {
    ClientTimeFrame getClientTimeFrame(int clientId);

    void createClientTimeFrame(int clientId);
    void updateClientTimeFrame(int clientId, ClientTimeFrame updatedTimeFrame);
}
