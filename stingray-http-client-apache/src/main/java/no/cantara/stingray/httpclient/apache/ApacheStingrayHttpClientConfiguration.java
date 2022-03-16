package no.cantara.stingray.httpclient.apache;

import no.cantara.stingray.httpclient.StingrayHttpClientConfiguration;
import no.cantara.stingray.httpclient.StingrayHttpHeader;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

class ApacheStingrayHttpClientConfiguration implements StingrayHttpClientConfiguration {

    private final int connectTimeoutMs;
    private final int socketTimeoutMs;
    private final URI baseUri;
    private final Map<String, StingrayHttpHeader> defaultHeaders;

    ApacheStingrayHttpClientConfiguration(URI baseUri, Map<String, StingrayHttpHeader> defaultHeaders, int connectTimeoutMs, int socketTimeoutMs) {
        this.baseUri = baseUri;
        this.defaultHeaders = defaultHeaders;
        this.connectTimeoutMs = connectTimeoutMs;
        this.socketTimeoutMs = socketTimeoutMs;
    }

    @Override
    public Duration getConnectTimeout() {
        return Duration.ofMillis(connectTimeoutMs);
    }

    @Override
    public Duration getSocketTimeout() {
        return Duration.ofMillis(socketTimeoutMs);
    }

    public URI getBaseUri() {
        return baseUri;
    }

    public Map<String, StingrayHttpHeader> getDefaultHeaders() {
        return defaultHeaders;
    }
}
