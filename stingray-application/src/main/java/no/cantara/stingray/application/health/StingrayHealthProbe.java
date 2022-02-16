package no.cantara.stingray.application.health;

import java.util.function.Supplier;

public class StingrayHealthProbe {
    final String key;
    final Supplier<Object> probe;

    public StingrayHealthProbe(String key, Supplier<Object> probe) {
        this.key = key;
        this.probe = probe;
    }

    public String getKey() {
        return key;
    }

    public Supplier<Object> getProbe() {
        return probe;
    }
}
