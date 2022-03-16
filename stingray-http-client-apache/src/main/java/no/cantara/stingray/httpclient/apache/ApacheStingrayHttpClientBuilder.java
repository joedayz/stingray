package no.cantara.stingray.httpclient.apache;

import no.cantara.stingray.httpclient.StingrayHttpClientBuilder;
import no.cantara.stingray.httpclient.StingrayHttpClientConfiguration;
import no.cantara.stingray.httpclient.StingrayHttpClientConfigurationBuilder;
import no.cantara.stingray.httpclient.StingrayHttpTarget;
import no.cantara.stingray.httpclient.StingrayHttpTargetBuilder;

import java.util.Objects;
import java.util.function.Consumer;

public class ApacheStingrayHttpClientBuilder implements StingrayHttpClientBuilder {

    private ApacheStingrayHttpClientConfiguration configuration = new ApacheStingrayHttpClientConfigurationBuilder()
            .build();

    private ApacheStingrayHttpTarget target;

    @Override
    public ApacheStingrayHttpClientBuilder useConfiguration(Consumer<StingrayHttpClientConfigurationBuilder> configurationBuilderConsumer) {
        ApacheStingrayHttpClientConfigurationBuilder configurationBuilder = new ApacheStingrayHttpClientConfigurationBuilder();
        configurationBuilderConsumer.accept(configurationBuilder);
        configuration = configurationBuilder.build();
        return this;
    }

    @Override
    public ApacheStingrayHttpClientBuilder withConfiguration(StingrayHttpClientConfiguration configuration) {
        Objects.requireNonNull(configuration);
        this.configuration = (ApacheStingrayHttpClientConfiguration) configuration;
        return this;
    }

    @Override
    public StingrayHttpClientBuilder useTarget(Consumer<StingrayHttpTargetBuilder> targetBuilderConsumer) {
        ApacheStingrayHttpTargetBuilder targetBuilder = new ApacheStingrayHttpTargetBuilder();
        targetBuilderConsumer.accept(targetBuilder);
        target = targetBuilder.build();
        return this;
    }

    @Override
    public StingrayHttpClientBuilder withTarget(StingrayHttpTarget target) {
        this.target = (ApacheStingrayHttpTarget) target;
        return this;
    }

    @Override
    public ApacheStingrayHttpClient build() {
        return new ApacheStingrayHttpClient(configuration, target);
    }
}
