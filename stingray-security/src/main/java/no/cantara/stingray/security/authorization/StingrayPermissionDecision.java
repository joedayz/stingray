package no.cantara.stingray.security.authorization;

public enum StingrayPermissionDecision {

    /**
     * Permission allowed.
     */
    ALLOWED(true),

    /**
     * Permission denied by default. Implicit deny means that no permission was specified on matching action.
     */
    IMPLICITLY_DENIED(false),

    /**
     * Permission denied. Explicit deny means that deny permission was specified on matching action. Explicit deny
     * overrides any other permissions on the same action.
     */
    EXPLICITLY_DENIED(false);

    private final boolean allowed;

    StingrayPermissionDecision(boolean allowed) {
        this.allowed = allowed;
    }

    public boolean isAllowed() {
        return allowed;
    }
}
