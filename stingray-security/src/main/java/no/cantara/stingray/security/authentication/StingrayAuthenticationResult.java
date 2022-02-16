package no.cantara.stingray.security.authentication;

import java.util.Optional;

public interface StingrayAuthenticationResult {

    /**
     * Whether the authentication attempt succeeded.
     *
     * @return true if the authentication attempt succeeded, false otherwise.
     */
    boolean isValid();

    /**
     * Whether the authenticated client is a user.
     *
     * @return true if authentication is valid and the authenticated client is a user.
     */
    boolean isUser();

    /**
     * Whether the authenticated client is an application.
     *
     * @return true if authentication is valid and the authenticated client is an application.
     */
    boolean isApplication();

    /**
     * The authenticated client, either an application or a user.
     *
     * @return the authenticated client or null if not authenticated.
     */
    StingrayAuthentication authentication();

    /**
     * Get the authenticated application client.
     *
     * @return an optional with the authenticated application or empty if the authentication is not valid or is a user.
     */
    default Optional<StingrayApplicationAuthentication> application() {
        if (isApplication()) {
            return Optional.of((StingrayApplicationAuthentication) authentication());
        }
        return Optional.empty();
    }

    /**
     * Get the authenticated user client.
     *
     * @return an optional with the authenticated user or empty if the authentication is not valid or is an application.
     */
    default Optional<StingrayUserAuthentication> user() {
        if (isUser()) {
            return Optional.of((StingrayUserAuthentication) authentication());
        }
        return Optional.empty();
    }
}
