package no.cantara.stingray.httpclient.apache;

import no.cantara.stingray.httpclient.StingrayHttpClientBuilder;
import no.cantara.stingray.httpclient.StingrayHttpClientConfiguration;
import no.cantara.stingray.httpclient.StingrayHttpClientConfigurationBuilder;

import java.util.function.Consumer;

public class ApacheStingrayHttpClientBuilder implements StingrayHttpClientBuilder {

    ApacheStingrayHttpClientConfiguration configuration;

    @Override
    public ApacheStingrayHttpClientBuilder useConfiguration(Consumer<StingrayHttpClientConfigurationBuilder> configurationBuilderConsumer) {
        ApacheStingrayHttpClientConfigurationBuilder configurationBuilder = new ApacheStingrayHttpClientConfigurationBuilder();
        configurationBuilderConsumer.accept(configurationBuilder);
        configuration = configurationBuilder.build();
        return this;
    }

    @Override
    public ApacheStingrayHttpClientBuilder withConfiguration(StingrayHttpClientConfiguration configuration) {
        this.configuration = (ApacheStingrayHttpClientConfiguration) configuration;
        return this;
    }

    @Override
    public ApacheStingrayHttpClient build() {
        return new ApacheStingrayHttpClient(configuration);
    }
}
