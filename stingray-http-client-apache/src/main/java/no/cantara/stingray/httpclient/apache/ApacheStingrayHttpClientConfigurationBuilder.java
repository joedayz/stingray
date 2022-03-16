package no.cantara.stingray.httpclient.apache;

import no.cantara.stingray.httpclient.StingrayHttpClientConfigurationBuilder;
import no.cantara.stingray.httpclient.StingrayHttpHeader;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

class ApacheStingrayHttpClientConfigurationBuilder implements StingrayHttpClientConfigurationBuilder {

    public static final int DEFAULT_CONNECT_TIMEOUT_MS = 3000;
    public static final int DEFAULT_SOCKET_TIMEOUT_MS = 10000;

    private int connectTimeoutMs = DEFAULT_CONNECT_TIMEOUT_MS;
    private int socketTimeoutMs = DEFAULT_SOCKET_TIMEOUT_MS;
    private String scheme = "http";
    private String host = "localhost";
    private int port = 80;
    private String basePath;
    private final Map<String, StingrayHttpHeader> defaultHeaderByKey = new LinkedHashMap<>();

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
    public ApacheStingrayHttpClientConfigurationBuilder withScheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    @Override
    public ApacheStingrayHttpClientConfigurationBuilder withHost(String host) {
        this.host = host;
        return this;
    }

    @Override
    public ApacheStingrayHttpClientConfigurationBuilder withPort(int port) {
        this.port = port;
        return this;
    }

    @Override
    public ApacheStingrayHttpClientConfigurationBuilder withBasePath(String basePath) {
        this.basePath = basePath;
        return this;
    }

    @Override
    public ApacheStingrayHttpClientConfigurationBuilder withBaseUrl(String baseUrl) {
        URI uri = URI.create(baseUrl);
        return withBaseUri(uri);
    }

    @Override
    public ApacheStingrayHttpClientConfigurationBuilder withBaseUri(URI uri) {
        return withScheme(uri.getScheme())
                .withHost(uri.getHost())
                .withPort(uri.getPort())
                .withBasePath(basePath);
    }

    @Override
    public ApacheStingrayHttpClientConfigurationBuilder withDefaultHeader(String name, String value) {
        defaultHeaderByKey.put(name, new ApacheStingrayHttpHeader(name, Collections.singletonList(value)));
        return this;
    }

    @Override
    public ApacheStingrayHttpClientConfiguration build() {
        URI baseUri = URI.create(scheme + "://" + host + ":" + port + normalizeBasePath(basePath));
        return new ApacheStingrayHttpClientConfiguration(baseUri, defaultHeaderByKey, connectTimeoutMs, socketTimeoutMs);
    }

    private static String normalizeBasePath(String input) {
        if (input == null) {
            return "";
        }
        String result = input.trim();
        // strip all leading slashes
        while (result.startsWith("/")) {
            result = result.substring(1);
        }
        // strip all trailing slashes
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        result = result.trim();
        if (result.length() > 0) {
            // when base-path is not empty, prefix with one slash
            result = "/" + result;
        }
        return result;
    }
}
