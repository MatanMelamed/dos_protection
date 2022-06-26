package com.ermetic.dosserver.services.dos_protection;


public interface IDosProtectionService {
    boolean isClientReachedMaxRequest(int clientId);
}
