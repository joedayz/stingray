package no.cantara.stingray.sample.greeter;

import no.cantara.config.ApplicationProperties;
import no.cantara.stingray.application.StingrayApplicationFactory;

public class GreeterApplicationFactory implements StingrayApplicationFactory<GreeterApplication> {

    @Override
    public Class<?> providerClass() {
        return GreeterApplication.class;
    }

    @Override
    public String alias() {
        return "greeter";
    }

    @Override
    public GreeterApplication create(ApplicationProperties config) {
        return new GreeterApplication(config);
    }
}
