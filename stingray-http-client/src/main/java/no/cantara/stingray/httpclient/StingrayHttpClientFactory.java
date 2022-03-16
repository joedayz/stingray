package no.cantara.stingray.httpclient;

public interface StingrayHttpClientFactory {

    StingrayHttpClientBuilder newClient();

    StingrayHttpClientConfigurationBuilder newConfiguration();
}
