package no.cantara.stingray.test;

import no.cantara.config.ApplicationProperties;

public interface StingrayBeforeCreationLifecycleListener {

    void beforeCreation(ApplicationProperties.Builder builder);

}
