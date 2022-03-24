package no.cantara.stingray.httpclient.apache;

import no.cantara.stingray.httpclient.StingrayHttpClientConfigurationBuilder;
import no.cantara.stingray.httpclient.StingrayHttpHeader;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

class ApacheStingrayHttpClientConfigurationBuilder implements StingrayHttpClientConfigurationBuilder {

    public static final int DEFAULT_CONNECT_TIMEOUT_MS = 3000;
    public static final int DEFAULT_SOCKET_TIMEOUT_MS = 10000;

    private int connectTimeoutMs = DEFAULT_CONNECT_TIMEOUT_MS;
    private int socketTimeoutMs = DEFAULT_SOCKET_TIMEOUT_MS;

    private final Map<String, StingrayHttpHeader> defaultHeaderByKey = new LinkedHashMap<>();

    ApacheStingrayHttpClientConfigurationBuilder() {
    }

    @Override
    public ApacheStingrayHttpClientConfigurationBuilder connectTimeout(Duration duration) {
        long durationMs = duration.toMillis();
        if (durationMs < 0 || durationMs > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("duration out of range");
        }
        this.connectTimeoutMs = (int) durationMs;
        return this;
    }

    @Override
    public ApacheStingrayHttpClientConfigurationBuilder socketTimeout(Duration duration) {
        long durationMs = duration.toMillis();
        if (durationMs < 0 || durationMs > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("duration out of range");
        }
        this.socketTimeoutMs = (int) durationMs;
        return this;
    }

    @Override
    public ApacheStingrayHttpClientConfigurationBuilder withDefaultHeader(String name, String value) {
        defaultHeaderByKey.put(name, new ApacheStingrayHttpHeader(name, value));
        return this;
    }

    @Override
    public ApacheStingrayHttpClientConfiguration build() {
        return new ApacheStingrayHttpClientConfiguration(defaultHeaderByKey, connectTimeoutMs, socketTimeoutMs);
    }
}
