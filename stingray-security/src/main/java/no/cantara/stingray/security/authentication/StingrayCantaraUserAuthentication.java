package no.cantara.stingray.security.authentication;

import net.whydah.sso.user.types.UserToken;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class StingrayCantaraUserAuthentication implements StingrayUserAuthentication {

    private final String userId;
    private final String username;
    private final String usertokenId;
    private final String customerRefId;
    private final Supplier<String> forwardingTokenGenerator;
    private final Supplier<Map<String, String>> rolesSupplier;
    private final AtomicReference<Map<String, String>> rolesRef = new AtomicReference<>();
    private final String accessGroupRoleNameFix;
    private final Supplier<UserToken> lazyUserTokenForAdditionalFieldsSupplier;

    public StingrayCantaraUserAuthentication(String userId, String username, String usertokenId, String customerRefId, Supplier<String> forwardingTokenGenerator, Supplier<Map<String, String>> rolesSupplier, String accessGroupRoleNameFix, Supplier<UserToken> lazyUserTokenForAdditionalFieldsSupplier) {
        this.userId = userId;
        this.username = username;
        this.usertokenId = usertokenId;
        this.customerRefId = customerRefId;
        this.forwardingTokenGenerator = forwardingTokenGenerator;
        this.rolesSupplier = rolesSupplier;
        this.accessGroupRoleNameFix = accessGroupRoleNameFix;
        this.lazyUserTokenForAdditionalFieldsSupplier = lazyUserTokenForAdditionalFieldsSupplier;
    }

    @Override
    public String ssoId() {
        if (userId == null) {
            // typical case if we are using JWT as it does not contain UID directly
            UserToken userToken = lazyUserTokenForAdditionalFieldsSupplier.get();
            if (userToken == null) {
                return null;
            }
            return userToken.getUid();
        }
        return userId;
    }

    @Override
    public Instant expires() {
        return null;
    }

    public String customerRef() {
        return customerRefId;
    }

    @Override
    public String username() {
        return username;
    }

    @Override
    public String usertokenId() {
        return usertokenId;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", StingrayCantaraUserAuthentication.class.getSimpleName() + "[", "]")
                .add("userId='" + userId + "'")
                .add("customerRefId='" + customerRefId + "'")
                .toString();
    }

    @Override
    public String forwardingToken() {
        return forwardingTokenGenerator.get();
    }

    @Override
    public List<String> groups() {
        List<String> groups = new ArrayList<>();
        for (Map.Entry<String, String> entry : roles().entrySet()) {
            String roleName = entry.getKey();
            if (roleName.toLowerCase().startsWith(accessGroupRoleNameFix.toLowerCase())
                    || roleName.toLowerCase().endsWith(accessGroupRoleNameFix.toLowerCase())) {
                String accessGroups = entry.getValue();
                if (accessGroups != null) {
                    String[] parts = accessGroups.split("[ ,;:]+");
                    for (String part : parts) {
                        groups.add(part.trim());
                    }
                }
            }
        }
        return groups;
    }

    @Override
    public Map<String, String> roles() {
        Map<String, String> roles = rolesRef.get();
        if (roles == null) {
            roles = rolesSupplier.get();
            rolesRef.set(roles);
        }
        return roles;
    }

    @Override
    public String firstName() {
        UserToken userToken = lazyUserTokenForAdditionalFieldsSupplier.get();
        if (userToken == null) {
            return null;
        }
        return userToken.getFirstName();
    }

    @Override
    public String lastName() {
        UserToken userToken = lazyUserTokenForAdditionalFieldsSupplier.get();
        if (userToken == null) {
            return null;
        }
        return userToken.getLastName();
    }

    @Override
    public String fullName() {
        UserToken userToken = lazyUserTokenForAdditionalFieldsSupplier.get();
        if (userToken == null) {
            return null;
        }
        return userToken.getFirstName() + " " + userToken.getLastName();
    }

    @Override
    public String email() {
        UserToken userToken = lazyUserTokenForAdditionalFieldsSupplier.get();
        if (userToken == null) {
            return null;
        }
        return userToken.getEmail();
    }

    @Override
    public String cellPhone() {
        UserToken userToken = lazyUserTokenForAdditionalFieldsSupplier.get();
        if (userToken == null) {
            return null;
        }
        return userToken.getCellPhone();
    }

    @Override
    public int securityLevel() {
        UserToken userToken = lazyUserTokenForAdditionalFieldsSupplier.get();
        if (userToken == null) {
            return -1; // unknown security-level
        }
        return Integer.parseInt(userToken.getSecurityLevel());
    }

    @Override
    public Instant tokenExpiry() {
        UserToken userToken = lazyUserTokenForAdditionalFieldsSupplier.get();
        if (userToken == null) {
            return null; // unknown
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String tokenTimestampEpochStr = userToken.getTimestamp();
        if (tokenTimestampEpochStr == null) {
            return null;
        }
        long tokenTimestampEpochMillis = Long.parseLong(tokenTimestampEpochStr, 10);
        Instant tokenTimestamp = Instant.ofEpochMilli(tokenTimestampEpochMillis); // TODO check assumption that this marks the start of lifespan
        String lifespanInSeconds = userToken.getLifespanInSeconds();
        if (lifespanInSeconds == null) {
            return null;
        }
        Instant expiry = tokenTimestamp.plus(Long.parseLong(lifespanInSeconds), ChronoUnit.SECONDS);
        return expiry;
    }
}
