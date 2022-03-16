package no.cantara.stingray.httpclient.functionalinterfaces;

import java.util.function.Supplier;

@FunctionalInterface
public interface StingrayHttpExceptionalStringSupplier extends Supplier<String> {
    @Override
    default String get() {
        try {
            return getThrows();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    String getThrows() throws Exception;
}
