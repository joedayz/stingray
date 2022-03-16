package no.cantara.stingray.httpclient;

import java.util.function.Consumer;

public interface StingrayHttpClientBuilder {

    StingrayHttpClientBuilder useConfiguration(Consumer<StingrayHttpClientConfigurationBuilder> configurationBuilderConsumer);

    StingrayHttpClientBuilder withConfiguration(StingrayHttpClientConfiguration configuration);

    StingrayHttpClient build();
}
