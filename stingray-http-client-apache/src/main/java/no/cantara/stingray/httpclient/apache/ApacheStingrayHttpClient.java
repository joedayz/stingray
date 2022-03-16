package no.cantara.stingray.httpclient.apache;

import no.cantara.stingray.httpclient.StingrayHttpClient;
import no.cantara.stingray.httpclient.StingrayHttpClientConfiguration;
import no.cantara.stingray.httpclient.StingrayHttpRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

class ApacheStingrayHttpClient implements StingrayHttpClient {

    private static final Logger LOG = LoggerFactory.getLogger(ApacheStingrayHttpClient.class);

    private final ApacheStingrayHttpClientConfiguration configuration;
    private final ApacheStingrayHttpTarget target;

    ApacheStingrayHttpClient(ApacheStingrayHttpClientConfiguration configuration, ApacheStingrayHttpTarget target) {
        Objects.requireNonNull(configuration);
        this.configuration = configuration;
        this.target = target;
    }

    StingrayHttpClientConfiguration getConfiguration() {
        return configuration;
    }

    ApacheStingrayHttpTarget getTarget() {
        return target;
    }

    @Override
    public ApacheStingrayHttpRequestBuilder get() {
        return new ApacheStingrayHttpRequestBuilder(this, HttpMethod.GET);
    }


    @Override
    public ApacheStingrayHttpRequestBuilder post() {
        return new ApacheStingrayHttpRequestBuilder(this, HttpMethod.POST);
    }

    @Override
    public ApacheStingrayHttpRequestBuilder put() {
        return new ApacheStingrayHttpRequestBuilder(this, HttpMethod.PUT);
    }

    @Override
    public ApacheStingrayHttpRequestBuilder options() {
        return new ApacheStingrayHttpRequestBuilder(this, HttpMethod.OPTIONS);
    }

    @Override
    public ApacheStingrayHttpRequestBuilder head() {
        return new ApacheStingrayHttpRequestBuilder(this, HttpMethod.HEAD);
    }

    @Override
    public ApacheStingrayHttpRequestBuilder delete() {
        return new ApacheStingrayHttpRequestBuilder(this, HttpMethod.DELETE);
    }

    @Override
    public ApacheStingrayHttpRequestBuilder patch() {
        return new ApacheStingrayHttpRequestBuilder(this, HttpMethod.PATCH);
    }

    @Override
    public StingrayHttpRequestBuilder trace() {
        return new ApacheStingrayHttpRequestBuilder(this, HttpMethod.TRACE);
    }
}
