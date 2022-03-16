package no.cantara.stingray.httpclient;

public interface StingrayHttpClientFactory {

    StingrayHttpClientBuilder client();

    StingrayHttpClientConfigurationBuilder configuration();
}
