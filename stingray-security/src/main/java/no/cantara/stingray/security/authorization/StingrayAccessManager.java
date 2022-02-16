package no.cantara.stingray.security.authorization;

import no.cantara.stingray.security.authentication.StingrayAuthentication;

import java.util.List;

public interface StingrayAccessManager {

    boolean hasAccess(StingrayAuthentication authentication, String action);

    boolean userHasAccess(String userId, List<String> assignedGroups, String action);

    boolean applicationHasAccess(String applicationId, List<String> assignedGroups, String action);
}
