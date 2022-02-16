package no.cantara.stingray.sample.randomizer;

import no.cantara.config.ApplicationProperties;
import no.cantara.stingray.application.StingrayApplicationFactory;

public class RandomizerApplicationFactory implements StingrayApplicationFactory<RandomizerApplication> {

    @Override
    public Class<?> providerClass() {
        return RandomizerApplication.class;
    }

    @Override
    public String alias() {
        return "randomizer";
    }

    @Override
    public RandomizerApplication create(ApplicationProperties config) {
        return new RandomizerApplication(config);
    }
}
