package no.cantara.stingray.httpclient.apache;

import no.cantara.stingray.httpclient.StingrayHttpHeader;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

class ApacheStingrayHttpHeader implements StingrayHttpHeader {

    private final String name;
    private final List<String> values;

    ApacheStingrayHttpHeader(String name, List<String> values) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(values);
        this.name = name;
        this.values = values;
    }

    ApacheStingrayHttpHeader(String name, String value) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);
        this.name = name;
        this.values = Collections.singletonList(value);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String first() {
        if (values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }

    @Override
    public List<String> all() {
        return values;
    }
}
