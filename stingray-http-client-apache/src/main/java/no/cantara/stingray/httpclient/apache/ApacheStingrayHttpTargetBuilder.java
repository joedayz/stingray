package no.cantara.stingray.httpclient.apache;

import no.cantara.stingray.httpclient.StingrayHttpTargetBuilder;

import java.net.URI;

class ApacheStingrayHttpTargetBuilder implements StingrayHttpTargetBuilder {

    private String scheme = "http";
    private String host = "localhost";
    private int port = 80;
    private String basePath;

    @Override
    public ApacheStingrayHttpTargetBuilder withScheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    @Override
    public ApacheStingrayHttpTargetBuilder withHost(String host) {
        this.host = host;
        return this;
    }

    @Override
    public ApacheStingrayHttpTargetBuilder withPort(int port) {
        this.port = port;
        return this;
    }

    @Override
    public ApacheStingrayHttpTargetBuilder withPath(String basePath) {
        this.basePath = basePath;
        return this;
    }

    @Override
    public ApacheStingrayHttpTargetBuilder withUrl(String baseUrl) {
        URI uri = URI.create(baseUrl);
        return withUri(uri);
    }

    @Override
    public ApacheStingrayHttpTargetBuilder withUri(URI uri) {
        return withScheme(uri.getScheme())
                .withHost(uri.getHost())
                .withPort(uri.getPort())
                .withPath(uri.getPath());
    }

    @Override
    public ApacheStingrayHttpTarget build() {
        URI uri = URI.create(scheme + "://" + host + (port > 0 ? ":" + port : "") + ApacheStingrayUtils.normalizeBasePath(basePath));
        return new ApacheStingrayHttpTarget(uri);
    }
}
