package com.ermetic.dosserver.services;


public interface IDosProtectionService {
    boolean isClientReachedMaxRequest(int clientId);
}
