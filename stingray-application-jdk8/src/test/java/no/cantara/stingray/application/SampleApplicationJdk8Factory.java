package no.cantara.stingray.application;

import no.cantara.config.ApplicationProperties;

public class SampleApplicationJdk8Factory implements StingrayApplicationFactory<SampleApplicationJdk8> {
    @Override
    public Class<?> providerClass() {
        return SampleApplicationJdk8.class;
    }

    @Override
    public String alias() {
        return "sample-application-jdk8";
    }

    @Override
    public SampleApplicationJdk8 create(ApplicationProperties applicationProperties) {
        return new SampleApplicationJdk8(alias(), applicationProperties);
    }
}
