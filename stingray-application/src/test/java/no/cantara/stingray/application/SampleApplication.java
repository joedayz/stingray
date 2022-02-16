package no.cantara.stingray.application;

import no.cantara.config.ApplicationProperties;

public class SampleApplication extends AbstractStingrayApplication<SampleApplication> {
    public SampleApplication(String alias, ApplicationProperties config) {
        super(alias, "dev-version-1.2.3", config);
    }

    @Override
    protected void doInit() {
        initBuiltinDefaults();
    }
}
