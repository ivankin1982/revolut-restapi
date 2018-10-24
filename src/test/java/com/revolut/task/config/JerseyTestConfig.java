package com.revolut.task.config;

import com.revolut.task.service.AccountRestService;
import com.revolut.task.service.TransferRestService;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.logging.Handler;
import java.util.logging.Level;

/**
 * Created by Alexey on 21.10.2018.
 */
public class JerseyTestConfig {
    private static ResourceConfig config = new ResourceConfig()
            .register(AccountRestService.class)
            .register(TransferRestService.class);

    public static ResourceConfig getConfig() {
        return config;
    }

    static {
        Handler[] handlers =
                java.util.logging.Logger.getLogger("").getHandlers();
        for (Handler handler: handlers) {
            handler.setLevel(Level.WARNING);
        }
    }

    private JerseyTestConfig() {
    }
}
