package no.cantara.stingray.security.authentication.test;

import net.whydah.sso.application.mappers.ApplicationTagMapper;
import net.whydah.sso.application.types.Tag;
import no.cantara.stingray.security.authentication.DefaultStingrayApplicationAuthentication;
import no.cantara.stingray.security.authentication.DefaultStingrayAuthenticationResult;
import no.cantara.stingray.security.authentication.StingrayApplicationAuthentication;
import no.cantara.stingray.security.authentication.StingrayApplicationTag;
import no.cantara.stingray.security.authentication.StingrayApplicationTokenSession;
import no.cantara.stingray.security.authentication.StingrayAuthenticationManager;
import no.cantara.stingray.security.authentication.StingrayAuthenticationResult;
import no.cantara.stingray.security.authentication.StingrayCantaraUserAuthentication;
import no.cantara.stingray.security.authentication.StingrayUserAuthentication;
import no.cantara.stingray.security.authentication.UnauthorizedStingrayException;
import no.cantara.stingray.security.authentication.whydah.WhydahStingrayAuthenticationManagerFactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FakeStingrayAuthenticationManager implements StingrayAuthenticationManager {

    /**
     * This field is deprecated and will be removed in a future version.
     *
     * @deprecated Use {@link FakeStingrayAuthorization#BEARER_TOKEN_UNAUTHORIZED} instead.
     */
    @Deprecated
    public static final String BEARER_TOKEN_UNAUTHORIZED = "Bearer unauthorized";

    static final Pattern fakeUserTokenPattern = Pattern.compile("Bearer\\s+fake-sso-id:\\s*(?<ssoid>[^,]*),\\s*(?:fake-username:\\s*(?<username>[^,]*),)?\\s*(?:fake-usertoken-id:\\s*(?<usertokenid>[^,]*),)?\\s*fake-customer-ref:\\s*(?<customerref>[^,]*)(?:,\\s*fake-roles:\\s*(?<roles>.*))?");
    static final Pattern fakeApplicationTokenPattern = Pattern.compile("Bearer\\s+fake-application-id:\\s*(?<applicationid>[^,]*)(?:,\\s*fake-tags:\\s*(?<tags>.*))?");

    private final FakeStingrayApplicationTokenSession fakeApplicationTokenSession;
    private final StingrayUserAuthentication fakeUser;
    private final StingrayApplicationAuthentication fakeApplication;

    public FakeStingrayAuthenticationManager(String selfApplicationId, String defaultFakeUserId, String defaultFakeUsername, String defaultFakeUsertokenId, String defaultFakeCustomerRef, String defaultFakeApplicationId) {
        fakeApplicationTokenSession = new FakeStingrayApplicationTokenSession(selfApplicationId);
        fakeUser = new StingrayCantaraUserAuthentication(defaultFakeUserId, defaultFakeUsername, defaultFakeUsertokenId, defaultFakeCustomerRef, () -> String.format("fake-sso-id: %s, fake-customer-ref: %s", defaultFakeUserId, defaultFakeCustomerRef), () -> {
            Map<String, String> roles = new LinkedHashMap<>();
            return roles;
        }, WhydahStingrayAuthenticationManagerFactory.DEFAULT_AUTH_GROUP_USER_ROLE_NAME_FIX, () -> null);
        fakeApplication = new DefaultStingrayApplicationAuthentication(defaultFakeApplicationId, String.format("fake-application-id: %s", defaultFakeApplicationId), Collections::emptyList, WhydahStingrayAuthenticationManagerFactory.DEFAULT_AUTH_GROUP_APPLICATION_TAG_NAME);
    }

    @Override
    public StingrayUserAuthentication authenticateAsUser(String authorizationHeader) throws UnauthorizedStingrayException {
        final String authorization = authorizationHeader;
        if (authorization == null || authorization.isEmpty()) {
            return fakeUser; // use default fake user when token is missing
        }
        if (authorization.equals(FakeStingrayAuthorization.BEARER_TOKEN_UNAUTHORIZED)) {
            // special case where test intentionally attempts to set an illegal token
            throw new UnauthorizedStingrayException();
        }
        Matcher m = fakeUserTokenPattern.matcher(authorizationHeader);
        if (m.matches()) {
            return createUserFromMatch(m);
        }
        return fakeUser;
    }

    private StingrayCantaraUserAuthentication createUserFromMatch(Matcher m) {
        String ssoId = m.group("ssoid");
        String username = m.group("username");
        if (username == null) {
            username = ssoId;
        }
        String theUsername = username;
        String usertokenId = m.group("usertokenid");
        if (usertokenId == null) {
            usertokenId = UUID.randomUUID().toString();
        }
        String theUsertokenId = usertokenId;
        String customerRef = m.group("customerref");
        String fakeRoles = m.group("roles");
        Supplier<String> forwardingTokenGenerator = () -> {
            return String.format("fake-sso-id: %s, fake-username: %s, fake-usertoken-id: %s, fake-customer-ref: %s, fake-roles: %s", ssoId, theUsername, theUsertokenId, customerRef, fakeRoles != null ? fakeRoles : "");
        };
        Supplier<Map<String, String>> rolesSupplier = () -> {
            if (fakeRoles == null || fakeRoles.trim().isEmpty()) {
                return Collections.emptyMap();
            }
            Map<String, String> roleValueByName = new LinkedHashMap<>();
            for (String keyValuePair : fakeRoles.split(",")) {
                String[] keyAndValue = keyValuePair.split("=");
                if (!keyAndValue[0].trim().isEmpty()) {
                    roleValueByName.put(keyAndValue[0].trim(), keyAndValue[1].trim());
                }
            }
            return roleValueByName;
        };
        return new StingrayCantaraUserAuthentication(ssoId, username, usertokenId, customerRef, forwardingTokenGenerator, rolesSupplier, WhydahStingrayAuthenticationManagerFactory.DEFAULT_AUTH_GROUP_USER_ROLE_NAME_FIX, () -> null);
    }

    @Override
    public StingrayApplicationAuthentication authenticateAsApplication(String authorizationHeader) throws UnauthorizedStingrayException {
        final String authorization = authorizationHeader;
        if (authorization == null || authorization.isEmpty()) {
            return fakeApplication; // use default fake application when token is missing
        }
        if (authorization.equals(FakeStingrayAuthorization.BEARER_TOKEN_UNAUTHORIZED)) {
            // special case where test intentionally attempts to set an illegal token
            throw new UnauthorizedStingrayException();
        }
        Matcher m = fakeApplicationTokenPattern.matcher(authorizationHeader);
        if (m.matches()) {
            return createApplicationFromMatch(m);
        }
        return fakeApplication;
    }

    private DefaultStingrayApplicationAuthentication createApplicationFromMatch(Matcher m) {
        String applicationid = m.group("applicationid");
        String tags = m.group("tags");
        List<Tag> whydahTagList = ApplicationTagMapper.getTagList(tags);
        List<StingrayApplicationTag> tagList = whydahTagList.stream().map(tag -> new StingrayApplicationTag(tag.getName(), tag.getValue())).collect(Collectors.toList());
        return new DefaultStingrayApplicationAuthentication(applicationid, String.format("fake-application-id: %s", applicationid), () -> tagList, WhydahStingrayAuthenticationManagerFactory.DEFAULT_AUTH_GROUP_APPLICATION_TAG_NAME);
    }

    @Override
    public StingrayAuthenticationResult authenticate(String authorizationHeader) throws UnauthorizedStingrayException {
        final String authorization = authorizationHeader;
        if (authorization == null || authorization.isEmpty()) {
            return new DefaultStingrayAuthenticationResult(fakeApplication); // use default fake application when token is missing
        }
        if (authorization.equals(FakeStingrayAuthorization.BEARER_TOKEN_UNAUTHORIZED)) {
            // special case where test intentionally attempts to set an illegal token
            return new DefaultStingrayAuthenticationResult(null);
        }
        Matcher userMatcher = fakeUserTokenPattern.matcher(authorizationHeader);
        if (userMatcher.matches()) {
            return new DefaultStingrayAuthenticationResult(createUserFromMatch(userMatcher));
        }
        Matcher appMatcher = fakeApplicationTokenPattern.matcher(authorizationHeader);
        if (appMatcher.matches()) {
            return new DefaultStingrayAuthenticationResult(createApplicationFromMatch(appMatcher));
        }
        return new DefaultStingrayAuthenticationResult(fakeApplication);
    }

    @Override
    public StingrayApplicationTokenSession getApplicationTokenSession() {
        return fakeApplicationTokenSession;
    }
}
