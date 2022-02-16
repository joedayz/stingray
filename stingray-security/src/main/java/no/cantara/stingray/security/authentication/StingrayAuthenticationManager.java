package no.cantara.stingray.security.authentication;

public interface StingrayAuthenticationManager {

    StingrayUserAuthentication authenticateAsUser(String authorizationHeader) throws UnauthorizedStingrayException;

    StingrayApplicationAuthentication authenticateAsApplication(String authorizationHeader) throws UnauthorizedStingrayException;

    StingrayAuthenticationResult authenticate(String authorizationHeader);

    StingrayApplicationTokenSession getApplicationTokenSession();
}
