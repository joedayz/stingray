package no.cantara.stingray.security.authorization;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StingrayRole {

    private final String roleId;
    private final StingrayPolicy policy;
    private final Set<StingrayRole> children;

    private StingrayRole(String roleId, StingrayPolicy policy, Set<StingrayRole> children) {
        this.roleId = roleId;
        this.policy = policy;
        this.children = children;
    }

    public String getRoleId() {
        return roleId;
    }

    public StingrayPolicy getPolicy() {
        return policy;
    }

    public Set<StingrayRole> getChildren() {
        return children;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        String roleId;
        List<StingrayPolicy> policies = new ArrayList<>();
        Set<StingrayRole> children = new LinkedHashSet<>();

        private Builder() {
        }

        public Builder roleId(String roleId) {
            this.roleId = roleId;
            return this;
        }

        public Builder addPolicies(Iterable<StingrayPolicy> policies) {
            for (StingrayPolicy policy : policies) {
                this.policies.add(policy);
            }
            return this;
        }

        public Builder addPolicy(StingrayPolicy policy) {
            this.policies.add(policy);
            return this;
        }

        public Builder addChildren(Iterable<StingrayRole> children) {
            for (StingrayRole child : children) {
                this.children.add(child);
            }
            return this;
        }

        public Builder addChild(StingrayRole child) {
            this.children.add(child);
            return this;
        }

        public StingrayRole build() {
            StingrayPolicy policy = StingrayPolicy.builder()
                    .policyId(roleId)
                    .aggregate(Stream.concat(policies.stream(), children.stream().map(StingrayRole::getPolicy))
                            .collect(Collectors.toList()))
                    .build();
            return new StingrayRole(roleId, policy, children);
        }
    }
}
