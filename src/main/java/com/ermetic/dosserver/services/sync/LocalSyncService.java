package com.ermetic.dosserver.services.sync;

import com.antkorwin.xsync.XSync;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class LocalSyncService extends XSync<Integer> implements ISyncService {
    @Override
    public void execute(int clientId, Runnable runnable) {
        super.execute(clientId, runnable);
    }

    @Override
    public <T> T evaluate(int clientId, Supplier<T> supplier) {
        return super.evaluate(clientId, supplier);
    }
}
