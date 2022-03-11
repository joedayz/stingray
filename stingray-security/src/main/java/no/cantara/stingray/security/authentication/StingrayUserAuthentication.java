package no.cantara.stingray.security.authentication;

import java.time.Instant;
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

    String firstName();

    String lastName();

    String fullName();

    String email();

    String cellPhone();

    /**
     * Valid range is [0..5]
     *
     * @return
     */
    int securityLevel();

    Instant tokenExpiry();
}
