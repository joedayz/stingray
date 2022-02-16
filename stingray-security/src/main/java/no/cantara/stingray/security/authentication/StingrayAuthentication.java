package no.cantara.stingray.security.authentication;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface StingrayAuthentication {

    String ssoId();

    Instant expires();

    String forwardingToken();

    List<String> groups();

    default boolean isApplication() {
        return false;
    }

    default boolean isUser() {
        return false;
    }

    default Optional<StingrayApplicationAuthentication> application() {
        if (isApplication()) {
            return Optional.of((StingrayApplicationAuthentication) this);
        }
        return Optional.empty();
    }

    default Optional<StingrayUserAuthentication> user() {
        if (isUser()) {
            return Optional.of((StingrayUserAuthentication) this);
        }
        return Optional.empty();
    }
}
