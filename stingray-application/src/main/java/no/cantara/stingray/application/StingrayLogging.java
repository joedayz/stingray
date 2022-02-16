package no.cantara.stingray.application;

import org.slf4j.bridge.SLF4JBridgeHandler;

public class StingrayLogging {

    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    public static void init() {
    }
}
