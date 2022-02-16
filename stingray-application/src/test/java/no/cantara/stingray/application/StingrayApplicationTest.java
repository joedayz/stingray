package no.cantara.stingray.application;

import no.cantara.config.ApplicationProperties;
import no.cantara.config.ProviderLoader;
import org.junit.jupiter.api.Test;

public class StingrayApplicationTest {

    @Test
    public void thatStackCanBeStartedAndStoppedWithoutIssue() {
        ApplicationProperties config = ApplicationProperties.builder().testDefaults().build();
        SampleApplication application = (SampleApplication) ProviderLoader.configure(config, "sample-application", StingrayApplicationFactory.class);
        application.init();
        application.start();
        application.stop();
    }
}
