package no.cantara.stingray.httpclient.apache;

import no.cantara.stingray.httpclient.StingrayHttpClientFactory;

public class ApacheStingrayHttpClientFactory implements StingrayHttpClientFactory {

    @Override
    public ApacheStingrayHttpClientBuilder newClient() {
        return new ApacheStingrayHttpClientBuilder();
    }

    @Override
    public ApacheStingrayHttpClientConfigurationBuilder newConfiguration() {
        return new ApacheStingrayHttpClientConfigurationBuilder();
    }
}
