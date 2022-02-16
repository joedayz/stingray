package no.cantara.security.authentication.whydah;

import no.cantara.config.ApplicationProperties;
import no.cantara.stingray.security.authentication.DefaultStingrayApplicationAuthentication;
import no.cantara.stingray.security.authentication.StingrayApplicationAuthentication;
import no.cantara.stingray.security.authentication.StingrayApplicationTag;
import no.cantara.stingray.security.authentication.StingrayCantaraUserAuthentication;
import no.cantara.stingray.security.authentication.StingrayUserAuthentication;
import no.cantara.stingray.security.authorization.DefaultStingrayAccessManager;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultStingrayAccessManagerTest {

    @Test
    public void thatUserAccessWorks() {
        DefaultStingrayAccessManager accessManager = new DefaultStingrayAccessManager("test", ApplicationProperties.builder()
                .values()
                .put("actions", "read,write")
                .put("policy.read-only.allow", "read")
                .put("role.reader.policies", "read-only")
                .put("group.readers.roles", "reader")
                .put("group.admins.roles", "superuser")
                .end()
                .build());

        StingrayUserAuthentication authenticatedJohn = new StingrayCantaraUserAuthentication("john-uid", "john", "utid-john", "custref-john", () -> "", () -> {
            Map<String, String> roleValueByName = new LinkedHashMap<>();
            roleValueByName.put("access-groups-for-unit-testing", "readers");
            return roleValueByName;
        }, "access-groups");
        assertTrue(accessManager.hasAccess(authenticatedJohn, "read"));
        assertFalse(accessManager.hasAccess(authenticatedJohn, "write"));

        StingrayUserAuthentication authenticatedSuperuser = new StingrayCantaraUserAuthentication("super-uid", "super", "utid-super", "custref-super", () -> "", () -> {
            Map<String, String> roleValueByName = new LinkedHashMap<>();
            roleValueByName.put("access-groups-for-unit-testing", "admins");
            return roleValueByName;
        }, "access-groups");
        assertTrue(accessManager.hasAccess(authenticatedSuperuser, "read"));
        assertTrue(accessManager.hasAccess(authenticatedSuperuser, "write"));

        StingrayApplicationAuthentication authenticatedMyApp = new DefaultStingrayApplicationAuthentication("myapp", "dummy-token", () -> {
            List<StingrayApplicationTag> tags = new LinkedList<>();
            tags.add(new StingrayApplicationTag("access-groups", "readers"));
            return tags;
        }, "access-groups");
        assertTrue(accessManager.hasAccess(authenticatedMyApp, "read"));
        assertFalse(accessManager.hasAccess(authenticatedMyApp, "write"));

        StingrayApplicationAuthentication authenticatedSuperApp = new DefaultStingrayApplicationAuthentication("superapp", "dummy-token", () -> {
            List<StingrayApplicationTag> tags = new LinkedList<>();
            tags.add(new StingrayApplicationTag("access-groups", "admins"));
            return tags;
        }, "access-groups");
        assertTrue(accessManager.hasAccess(authenticatedSuperApp, "read"));
        assertTrue(accessManager.hasAccess(authenticatedSuperApp, "write"));
    }

    @Test
    public void thatAccessWorksWhenUserAssignedGroupIsNotFound() {
        DefaultStingrayAccessManager accessManager = new DefaultStingrayAccessManager("test", ApplicationProperties.builder()
                .values()
                .put("actions", "read,write")
                .put("policy.read-only.allow", "read")
                .put("role.reader.policies", "read-only")
                .put("group.readers.roles", "reader")
                .put("group.admins.roles", "superuser")
                .end()
                .build());
        assertFalse(accessManager.userHasAccess("someuser", Collections.singletonList("badgroup"), "read"));
    }

    @Test
    public void thatAccessWorksWhenApplicationAssignedGroupIsNotFound() {
        DefaultStingrayAccessManager accessManager = new DefaultStingrayAccessManager("test", ApplicationProperties.builder()
                .values()
                .put("actions", "read,write")
                .put("policy.read-only.allow", "read")
                .put("role.reader.policies", "read-only")
                .put("group.readers.roles", "reader")
                .put("group.admins.roles", "superuser")
                .end()
                .build());
        assertFalse(accessManager.applicationHasAccess("someapplication", Collections.singletonList("badgroup"), "read"));
    }
}
