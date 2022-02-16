package no.cantara.stingray.security.authorization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StingrayConfiguredUserPermissions {

    private final String userId;
    private final List<StingrayGroup> groups;
    private final List<StingrayRole> roles;
    private final List<StingrayPolicy> policies;
    private final StingrayPolicy effectivePolicy;

    public StingrayConfiguredUserPermissions(String userId, Collection<StingrayGroup> groups, Collection<StingrayRole> roles, Collection<StingrayPolicy> policies) {
        this.userId = userId;
        this.groups = new ArrayList<>(groups);
        this.roles = new ArrayList<>(roles);
        this.policies = new ArrayList<>(policies);
        this.effectivePolicy = StingrayPolicy.builder()
                .policyId("effective-policy-for-user-" + userId)
                .aggregate(groups.stream().map(StingrayGroup::getPolicy))
                .aggregate(roles.stream().map(StingrayRole::getPolicy))
                .aggregate(policies.stream())
                .build();
    }

    public StingrayPolicy getEffectivePolicy() {
        return effectivePolicy;
    }
}
