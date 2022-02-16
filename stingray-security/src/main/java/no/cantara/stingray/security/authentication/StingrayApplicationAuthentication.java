package no.cantara.stingray.security.authentication;

import java.util.List;

public interface StingrayApplicationAuthentication extends StingrayAuthentication {

    @Override
    default boolean isApplication() {
        return true;
    }

    List<StingrayApplicationTag> tags();
}
