package no.cantara.stingray.security.authorization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StingrayConfiguredApplicationPermissions {

    private final String applicationId;
    private final List<StingrayGroup> groups;
    private final List<StingrayRole> roles;
    private final List<StingrayPolicy> policies;
    private final StingrayPolicy effectivePolicy;

    public StingrayConfiguredApplicationPermissions(String applicationId, Collection<StingrayGroup> groups, Collection<StingrayRole> roles, Collection<StingrayPolicy> policies) {
        this.applicationId = applicationId;
        this.groups = new ArrayList<>(groups);
        this.roles = new ArrayList<>(roles);
        this.policies = new ArrayList<>(policies);
        this.effectivePolicy = StingrayPolicy.builder()
                .policyId("effective-policy-for-application-" + applicationId)
                .aggregate(groups.stream().map(StingrayGroup::getPolicy))
                .aggregate(roles.stream().map(StingrayRole::getPolicy))
                .aggregate(policies.stream())
                .build();
    }

    public StingrayPolicy getEffectivePolicy() {
        return effectivePolicy;
    }
}
