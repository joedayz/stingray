package no.cantara.stingray.application;

import no.cantara.config.ApplicationProperties;

public class SampleApplicationJdk8 extends StingrayServletApplication<SampleApplicationJdk8> {
    public SampleApplicationJdk8(String alias, ApplicationProperties config) {
        super(alias, "test-version", config);
    }

    @Override
    protected void doInit() {
        initBuiltinDefaults();
    }
}
