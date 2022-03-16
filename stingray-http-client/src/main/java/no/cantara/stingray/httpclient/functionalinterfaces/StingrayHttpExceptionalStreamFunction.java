package no.cantara.stingray.httpclient.functionalinterfaces;

import java.io.InputStream;
import java.util.function.Function;

@FunctionalInterface
public interface StingrayHttpExceptionalStreamFunction<R> extends Function<InputStream,R> {
    @Override
    default R apply(InputStream t){
        try{
            return applyThrows(t);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    R applyThrows(InputStream t) throws Exception;
}
