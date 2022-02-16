package no.cantara.stingray.security.authentication;

public class DefaultStingrayAuthenticationResult implements StingrayAuthenticationResult {

    private final StingrayAuthentication authentication;

    public DefaultStingrayAuthenticationResult(StingrayAuthentication authentication) {
        this.authentication = authentication;
    }

    public boolean isValid() {
        return authentication != null;
    }

    public boolean isUser() {
        return isValid() && authentication instanceof StingrayUserAuthentication;
    }

    public boolean isApplication() {
        return isValid() && authentication instanceof StingrayApplicationAuthentication;
    }

    public StingrayAuthentication authentication() {
        return authentication;
    }
}
