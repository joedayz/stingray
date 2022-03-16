package no.cantara.stingray.httpclient.apache;

import no.cantara.stingray.httpclient.StingrayHttpClientFactory;

public class ApacheStingrayHttpClientFactory implements StingrayHttpClientFactory {

    @Override
    public ApacheStingrayHttpClientBuilder client() {
        return new ApacheStingrayHttpClientBuilder();
    }

    @Override
    public ApacheStingrayHttpClientConfigurationBuilder configuration() {
        return new ApacheStingrayHttpClientConfigurationBuilder();
    }
}
