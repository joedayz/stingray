package no.cantara.stingray.security.authorization;

import no.cantara.config.ApplicationProperties;

public class StingrayDefaultAccessManagerFactory implements StingrayAccessManagerFactory {
    @Override
    public Class<?> providerClass() {
        return DefaultStingrayAccessManager.class;
    }

    @Override
    public String alias() {
        return "default";
    }

    @Override
    public DefaultStingrayAccessManager create(ApplicationProperties applicationProperties) {
        String actionService = applicationProperties.get("service", "local");
        return new DefaultStingrayAccessManager(actionService, applicationProperties);
    }
}
