package no.cantara.stingray.sample.randomizer;

import no.cantara.config.ApplicationProperties;
import no.cantara.stingray.application.AbstractStingrayApplication;
import no.cantara.stingray.application.health.StingrayHealthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class RandomizerApplication extends AbstractStingrayApplication<RandomizerApplication> {

    private static final Logger log = LoggerFactory.getLogger(RandomizerApplication.class);

    public static void main(String[] args) {
        ApplicationProperties config = new RandomizerApplicationFactory()
                .conventions(ApplicationProperties.builder())
                .build();
        new RandomizerApplication(config).init().start();
    }

    public RandomizerApplication(ApplicationProperties config) {
        super("randomizer",
                readMetaInfMavenPomVersion("no.cantara.stingray", "randomizer"),
                config
        );
    }

    @Override
    public void doInit() {
        initBuiltinDefaults();
        init(Random.class, this::createRandom);
        RandomizerResource randomizerResource = initAndRegisterJaxRsWsComponent(RandomizerResource.class, this::createRandomizerResource);
        get(StingrayHealthService.class).registerHealthProbe("randomizer.request.count", randomizerResource::getRequestCount);
    }

    private Random createRandom() {
        return new Random(System.currentTimeMillis());
    }

    private RandomizerResource createRandomizerResource() {
        Random random = get(Random.class);
        return new RandomizerResource(random);
    }
}
