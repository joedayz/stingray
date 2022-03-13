package no.cantara.stingray.security.authentication.test;

import net.whydah.sso.application.mappers.ApplicationTagMapper;
import net.whydah.sso.application.types.Tag;
import no.cantara.stingray.security.authentication.DefaultStingrayApplicationAuthentication;
import no.cantara.stingray.security.authentication.StingrayApplicationAuthentication;
import no.cantara.stingray.security.authentication.StingrayCantaraUserAuthentication;
import no.cantara.stingray.security.authentication.StingrayUserAuthentication;
import no.cantara.stingray.security.authentication.whydah.WhydahStingrayAuthenticationManagerFactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FakeStingrayAuthorization {

    /**
     * Put the value of this constant in Authorization header when using fake authentication manager to force
     * applications to respond with 401 Unauthorized.
     */
    public static final String BEARER_TOKEN_UNAUTHORIZED = "Bearer unauthorized";

    public static FakeStingrayApplicationAuthorizationBuilder application() {
        return new FakeStingrayApplicationAuthorizationBuilder();
    }

    public static FakeStingrayUserAuthorizationBuilder user() {
        return new FakeStingrayUserAuthorizationBuilder();
    }

    public static class FakeStingrayApplicationAuthorizationBuilder {
        private String applicationId;
        private List<Tag> tags = new LinkedList<>();
        private List<String> authGroups = new LinkedList<>();

        private FakeStingrayApplicationAuthorizationBuilder() {
        }

        public FakeStingrayApplicationAuthorizationBuilder applicationId(String applicationId) {
            this.applicationId = applicationId;
            return this;
        }

        public FakeStingrayApplicationAuthorizationBuilder addTag(String tagName, String tagValue) {
            tags.add(new Tag(tagName, tagValue));
            return this;
        }

        public FakeStingrayApplicationAuthorizationBuilder addTag(String tagValue) {
            tags.add(new Tag(Tag.DEFAULTNAME, tagValue));
            return this;
        }

        public FakeStingrayApplicationAuthorizationBuilder addAccessGroup(String group) {
            authGroups.add(group);
            return this;
        }

        public String build() {
            if (authGroups.size() > 0) {
                String accessGroups = String.join(" ", authGroups);
                tags.add(new Tag(WhydahStingrayAuthenticationManagerFactory.DEFAULT_AUTH_GROUP_APPLICATION_TAG_NAME, accessGroups));
            }
            StringBuilder sb = new StringBuilder("Bearer fake-application-id: ").append(applicationId);
            if (tags.size() > 0) {
                sb.append(", fake-tags: ").append(ApplicationTagMapper.toApplicationTagString(tags));
            }
            return sb.toString();
        }

        public StingrayApplicationAuthentication buildAuth() {
            String forwardingToken = build();
            return new DefaultStingrayApplicationAuthentication(
                    applicationId,
                    forwardingToken,
                    Collections::emptyList,
                    WhydahStingrayAuthenticationManagerFactory.DEFAULT_AUTH_GROUP_APPLICATION_TAG_NAME
            );
        }
    }

    public static class FakeStingrayUserAuthorizationBuilder {
        private String userId;
        private String username;
        private String usertokenId;
        private String customerRef;
        private final Map<String, String> roles = new LinkedHashMap<>();
        private final List<String> authGroups = new LinkedList<>();

        private FakeStingrayUserAuthorizationBuilder() {
        }

        public FakeStingrayUserAuthorizationBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public FakeStingrayUserAuthorizationBuilder username(String username) {
            this.username = username;
            return this;
        }

        public FakeStingrayUserAuthorizationBuilder usertokenId(String usertokenId) {
            this.usertokenId = usertokenId;
            return this;
        }

        public FakeStingrayUserAuthorizationBuilder customerRef(String customerRef) {
            this.customerRef = customerRef;
            return this;
        }

        public FakeStingrayUserAuthorizationBuilder addRole(String name, String value) {
            this.roles.put(name, value);
            return this;
        }

        public FakeStingrayUserAuthorizationBuilder addAccessGroup(String group) {
            this.authGroups.add(group);
            return this;
        }

        public String build() {
            if (userId == null) {
                throw new IllegalArgumentException("userId cannot be null");
            }
            if (customerRef == null) {
                throw new IllegalArgumentException("customerRef cannot be null");
            }
            if (authGroups.size() > 0) {
                String accessGroups = String.join(" ", authGroups);
                roles.put(WhydahStingrayAuthenticationManagerFactory.DEFAULT_AUTH_GROUP_USER_ROLE_NAME_FIX, accessGroups);
            }
            final StringBuilder sb = new StringBuilder();
            sb.append("Bearer ");
            sb.append("fake-sso-id: ").append(userId);
            if (username != null) {
                sb.append(", fake-username: ").append(username);
            }
            if (usertokenId != null) {
                sb.append(", fake-usertoken-id: ").append(usertokenId);
            }
            sb.append(", fake-customer-ref: ").append(customerRef);
            {
                sb.append(", fake-roles: ");
                String delim = "";
                for (Map.Entry<String, String> role : roles.entrySet()) {
                    sb.append(delim).append(role.getKey()).append("=").append(role.getValue());
                    delim = ",";
                }
            }
            return sb.toString();
        }

        public StingrayUserAuthentication buildAuth() {
            String forwardingToken = build();
            return new StingrayCantaraUserAuthentication(
                    userId,
                    username,
                    usertokenId,
                    customerRef,
                    () -> forwardingToken,
                    Collections::emptyMap,
                    WhydahStingrayAuthenticationManagerFactory.DEFAULT_AUTH_GROUP_USER_ROLE_NAME_FIX,
                    () -> null
            );
        }
    }
}
