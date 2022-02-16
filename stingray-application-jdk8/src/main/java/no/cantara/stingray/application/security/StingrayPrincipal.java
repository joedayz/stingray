package no.cantara.stingray.application.security;

import no.cantara.stingray.security.authentication.StingrayAuthentication;

import java.security.Principal;

public class StingrayPrincipal implements Principal {

    private final StingrayAuthentication authentication;

    public StingrayPrincipal(StingrayAuthentication authentication) {
        this.authentication = authentication;
    }

    public StingrayAuthentication getAuthentication() {
        return authentication;
    }

    @Override
    public String getName() {
        return authentication.ssoId();
    }
}
