package no.cantara.stingray.httpclient.functionalinterfaces;

import java.util.function.Function;

@FunctionalInterface
public interface StingrayHttpExceptionalStringFunction<R> extends Function<String,R> {
    @Override
    default R apply(String t){
        try{
            return applyThrows(t);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    R applyThrows(String t) throws Exception;
}
