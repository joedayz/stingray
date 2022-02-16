package no.cantara.stingray.security.authentication;

import java.util.Map;

public interface StingrayUserAuthentication extends StingrayAuthentication {

    @Override
    default boolean isUser() {
        return true;
    }

    String customerRef();

    String username();

    String usertokenId();

    Map<String, String> roles();
}
