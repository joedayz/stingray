package no.cantara.stingray.application;

import no.cantara.config.ApplicationProperties;

public class SampleApplicationFactory implements StingrayApplicationFactory<SampleApplication> {
    @Override
    public Class<?> providerClass() {
        return SampleApplication.class;
    }

    @Override
    public String alias() {
        return "sample-application";
    }

    @Override
    public SampleApplication create(ApplicationProperties applicationProperties) {
        return new SampleApplication(alias(), applicationProperties);
    }
}
