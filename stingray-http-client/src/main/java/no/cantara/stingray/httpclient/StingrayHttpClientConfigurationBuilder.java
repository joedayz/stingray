package no.cantara.stingray.httpclient;

import java.net.URI;
import java.time.Duration;

public interface StingrayHttpClientConfigurationBuilder {

    StingrayHttpClientConfigurationBuilder connectTimeout(Duration duration);

    StingrayHttpClientConfigurationBuilder socketTimeout(Duration duration);

    StingrayHttpClientConfigurationBuilder withScheme(String scheme);

    StingrayHttpClientConfigurationBuilder withHost(String host);

    StingrayHttpClientConfigurationBuilder withPort(int port);

    StingrayHttpClientConfigurationBuilder withBasePath(String basePath);

    StingrayHttpClientConfigurationBuilder withBaseUrl(String baseUrl);

    StingrayHttpClientConfigurationBuilder withBaseUri(URI baseUri);

    StingrayHttpClientConfigurationBuilder withDefaultHeader(String name, String value);

    StingrayHttpClientConfiguration build();
}
