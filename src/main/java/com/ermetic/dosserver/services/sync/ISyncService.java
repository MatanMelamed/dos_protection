package com.ermetic.dosserver.services.sync;

import java.util.function.Supplier;

/**
 * A component that is used to execute synchronized blocks while synchronizing on a client ID
 */
public interface ISyncService {

    void execute(int clientId, Runnable runnable);

    <T> T evaluate(int clientId, Supplier<T> supplier);
}
