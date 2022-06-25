package com.ermetic.dosserver.sync;

import com.antkorwin.xsync.XSync;
import org.springframework.stereotype.Component;

/**
 * A component that is used to execute synchronized blocks while synchronizing on an integer.
 * Used for client id synchronization.
 */
@Component
public class ClientIdSyncExecutor extends XSync<Integer> {
}
