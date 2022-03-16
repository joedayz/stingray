package no.cantara.stingray.httpclient;

import java.time.Duration;

public interface StingrayHttpClientConfigurationBuilder {

    StingrayHttpClientConfigurationBuilder connectTimeout(Duration duration);

    StingrayHttpClientConfigurationBuilder socketTimeout(Duration duration);

    StingrayHttpClientConfigurationBuilder withDefaultHeader(String name, String value);

    StingrayHttpClientConfiguration build();
}
