package no.cantara.stingray.httpclient.functionalinterfaces;

import java.io.InputStream;
import java.util.function.Supplier;

@FunctionalInterface
public interface StingrayHttpExceptionalStreamSupplier extends Supplier<InputStream> {
    @Override
    default InputStream get() {
        try {
            return getThrows();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    InputStream getThrows() throws Exception;
}
