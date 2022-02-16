package no.cantara.stingray.security.authorization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StingrayGroup {

    private final String groupId;
    private final StingrayPolicy policy;
    private final List<String> roleIds;

    private StingrayGroup(String groupId, List<String> roleIds, StingrayPolicy policy) {
        this.groupId = groupId;
        this.policy = policy;
        this.roleIds = roleIds;
    }

    public String getGroupId() {
        return groupId;
    }

    public StingrayPolicy getPolicy() {
        return policy;
    }

    public List<String> getRoleIds() {
        return roleIds;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        String groupId;
        List<StingrayRole> roles = new ArrayList<>();
        List<StingrayPolicy> policies = new ArrayList<>();

        private Builder() {
        }

        public Builder groupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder json(String groupJson) {
            JsonNode groupNode;
            try {
                groupNode = StingrayJackson.mapper.readTree(groupJson);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return jackson(groupNode);
        }

        public Builder jackson(JsonNode groupNode) {
            if (!groupNode.has("groupId")) {
                groupId = UUID.randomUUID().toString();
            } else {
                JsonNode groupIdNode = groupNode.get("groupId");
                if (groupIdNode.isNull()) {
                    groupId = UUID.randomUUID().toString();
                } else {
                    groupId = groupIdNode.asText(UUID.randomUUID().toString());
                }
            }

            return this;
        }

        public Builder addRoles(Iterable<StingrayRole> roles) {
            for (StingrayRole role : roles) {
                this.roles.add(role);
            }
            return this;
        }

        public Builder addRole(StingrayRole role) {
            this.roles.add(role);
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

        public StingrayGroup build() {
            List<String> roleIds = roles.stream().map(StingrayRole::getRoleId).collect(Collectors.toList());
            StingrayPolicy policy = StingrayPolicy.builder()
                    .policyId("aggregate-policy-for-group-" + groupId)
                    .aggregate(Stream.concat(roles.stream().map(StingrayRole::getPolicy), policies.stream()))
                    .build();
            return new StingrayGroup(
                    groupId,
                    roleIds,
                    policy
            );

        }
    }
}
