package no.cantara.stingray.security.authentication;

public class UnauthorizedStingrayException extends RuntimeException {
    public UnauthorizedStingrayException() {
    }

    public UnauthorizedStingrayException(Throwable cause) {
        super(cause);
    }
}
