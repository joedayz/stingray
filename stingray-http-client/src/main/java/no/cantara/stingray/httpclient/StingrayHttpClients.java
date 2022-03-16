package no.cantara.stingray.httpclient;

import java.time.Duration;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicReference;

public class StingrayHttpClients {

    private static final AtomicReference<StingrayHttpClientFactory> factoryRef = new AtomicReference<>();

    private static StingrayHttpClientFactory factory() {
        if (factoryRef.get() == null) {
            synchronized (factoryRef) {
                if (factoryRef.get() == null) {
                    ServiceLoader<StingrayHttpClientFactory> serviceLoader = ServiceLoader.load(StingrayHttpClientFactory.class);
                    Iterator<StingrayHttpClientFactory> iterator = serviceLoader.iterator();
                    if (!iterator.hasNext()) {
                        throw new IllegalStateException("No providers found for interface: " + StingrayHttpClientFactory.class.getName());
                    }
                    StingrayHttpClientFactory factory = iterator.next();
                    if (factory == null) {
                        throw new IllegalStateException("No providers found for interface: " + StingrayHttpClientFactory.class.getName());
                    }
                    factoryRef.set(factory);
                }
            }
        }
        return factoryRef.get();
    }

    public static StingrayHttpClientBuilder customClient() {
        return factory()
                .client();
    }

    public static StingrayHttpClient defaultClient() {
        return factory()
                .client()
                .useConfiguration(config -> config
                        .connectTimeout(Duration.ofSeconds(3))
                        .socketTimeout(Duration.ofSeconds(10))
                        .build())
                .build();
    }

    public static StingrayHttpClientConfigurationBuilder configuration() {
        return factory()
                .configuration();
    }
}
