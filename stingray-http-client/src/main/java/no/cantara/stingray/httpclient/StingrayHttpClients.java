package no.cantara.stingray.httpclient;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicReference;

public class StingrayHttpClients {

    private static final AtomicReference<StingrayHttpClientFactory> factoryRef = new AtomicReference<>();

    public static StingrayHttpClientFactory factory() {
        if (factoryRef.get() == null) {
            synchronized (factoryRef) {
                if (factoryRef.get() == null) {
                    StingrayHttpClientFactory factory = loadFactory();
                    factoryRef.set(factory);
                }
            }
        }
        return factoryRef.get();
    }

    private static StingrayHttpClientFactory loadFactory() {
        ServiceLoader<StingrayHttpClientFactory> serviceLoader = ServiceLoader.load(StingrayHttpClientFactory.class);
        Iterator<StingrayHttpClientFactory> iterator = serviceLoader.iterator();
        if (!iterator.hasNext()) {
            throw new IllegalStateException("No providers found for interface: " + StingrayHttpClientFactory.class.getName());
        }
        StingrayHttpClientFactory factory = iterator.next();
        if (factory == null) {
            throw new IllegalStateException("No providers found for interface: " + StingrayHttpClientFactory.class.getName());
        }
        return factory;
    }
}
