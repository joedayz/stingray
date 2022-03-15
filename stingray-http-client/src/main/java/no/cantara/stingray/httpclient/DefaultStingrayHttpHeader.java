package no.cantara.stingray.httpclient;

import java.util.List;
import java.util.Objects;

class DefaultStingrayHttpHeader implements StingrayHttpHeader {

    private final String name;
    private final List<String> values;

    DefaultStingrayHttpHeader(String name, List<String> values) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(values);
        this.name = name;
        this.values = values;
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
