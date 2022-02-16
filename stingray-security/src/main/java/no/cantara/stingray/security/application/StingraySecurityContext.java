package no.cantara.stingray.security.application;

import jakarta.ws.rs.core.SecurityContext;
import no.cantara.stingray.security.authentication.StingrayAuthentication;

import java.security.Principal;

public class StingraySecurityContext implements SecurityContext {

    private final StingrayAuthentication authentication;

    public StingraySecurityContext(StingrayAuthentication authentication) {
        this.authentication = authentication;
    }

    public StingrayAuthentication getAuthentication() {
        return authentication;
    }

    @Override
    public Principal getUserPrincipal() {
        return new StingrayPrincipal(authentication);
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public boolean isSecure() {
        return true;
    }

    @Override
    public String getAuthenticationScheme() {
        return "Bearer";
    }
}
