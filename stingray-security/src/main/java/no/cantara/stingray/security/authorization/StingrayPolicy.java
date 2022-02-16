package no.cantara.stingray.security.authorization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StingrayPolicy {

    final String policyId;
    final Map<StingrayServiceAction, StingrayPermission> permissionByAction;

    private StingrayPolicy(String policyId, Map<StingrayServiceAction, StingrayPermission> permissionByAction) {
        this.policyId = policyId;
        this.permissionByAction = new LinkedHashMap<>(permissionByAction);
    }

    public String getPolicyId() {
        return policyId;
    }

    public String toJson() {
        return toJackson().toPrettyString();
    }

    public ObjectNode toJackson() {
        ObjectNode policyNode = StingrayJackson.mapper.createObjectNode();
        policyNode.put("policyId", policyId);
        List<StingrayServiceAction> allowedActions = new ArrayList<>(permissionByAction.size());
        List<StingrayServiceAction> deniedActions = new ArrayList<>(permissionByAction.size());
        for (Map.Entry<StingrayServiceAction, StingrayPermission> entry : permissionByAction.entrySet()) {
            StingrayServiceAction action = entry.getKey();
            StingrayPermission permission = entry.getValue();
            if (permission == StingrayPermission.ALLOW) {
                allowedActions.add(action);
            } else {
                deniedActions.add(action);
            }
        }
        if (allowedActions.size() > 0) {
            ArrayNode allow = policyNode.putArray("allow");
            for (StingrayServiceAction action : allowedActions) {
                allow.add(action.toString());
            }
        }
        if (deniedActions.size() > 0) {
            ArrayNode deny = policyNode.putArray("deny");
            for (StingrayServiceAction action : deniedActions) {
                deny.add(action.toString());
            }
        }
        return policyNode;
    }

    public StingrayPermissionDecision decidePermission(StingrayServiceAction action) {
        if ("*".equals(action.getService()) || "*".equals(action.getAction())) {
            throw new IllegalArgumentException("Cannot check action: " + action);
        }

        StingrayPermission allActionsOnAllServicesPermission = permissionByAction.get(StingrayServiceAction.ALL_ACTIONS_ON_ALL_SERVICES);
        if (StingrayPermission.DENY == allActionsOnAllServicesPermission) {
            return StingrayPermissionDecision.EXPLICITLY_DENIED; // policy explicitly denies all actions on all services
        }
        StingrayServiceAction allActionsOnService = new StingrayServiceAction(action.getService(), "*");
        StingrayPermission allActionsOnServicePermission = permissionByAction.get(allActionsOnService);
        if (StingrayPermission.DENY == allActionsOnServicePermission) {
            return StingrayPermissionDecision.EXPLICITLY_DENIED; // policy explicitly denies all actions on service
        }
        StingrayPermission actionPermission = permissionByAction.get(action);
        if (StingrayPermission.DENY == actionPermission) {
            return StingrayPermissionDecision.EXPLICITLY_DENIED; // policy explicitly denies action on service
        }

        if (StingrayPermission.ALLOW == allActionsOnAllServicesPermission) {
            return StingrayPermissionDecision.ALLOWED; // policy allows all actions on all services
        }
        if (StingrayPermission.ALLOW == allActionsOnServicePermission) {
            return StingrayPermissionDecision.ALLOWED; // policy allows all actions on service
        }
        if (StingrayPermission.ALLOW == actionPermission) {
            return StingrayPermissionDecision.ALLOWED; // policy allows action on service
        }

        return StingrayPermissionDecision.IMPLICITLY_DENIED; // default if no other deny or allow permissions are found
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        String policyId;
        Map<StingrayServiceAction, StingrayPermission> permissionByAction = new LinkedHashMap<>();

        private Builder() {
        }

        public Builder policyId(String policyId) {
            this.policyId = policyId;
            return this;
        }

        public Builder json(String policyJson) {
            try {
                return jackson(StingrayJackson.mapper.readTree(policyJson));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        public Builder jackson(JsonNode policyNode) {
            if (!policyNode.has("policyId")) {
                policyId = UUID.randomUUID().toString();
            } else {
                JsonNode policyIdNode = policyNode.get("policyId");
                if (policyIdNode.isNull()) {
                    policyId = UUID.randomUUID().toString();
                } else {
                    policyId = policyIdNode.asText(UUID.randomUUID().toString());
                }
            }
            permissionByAction.clear();
            if (policyNode.has("allow")) {
                JsonNode allowNode = policyNode.get("allow");
                if (allowNode.isArray()) {
                    ArrayNode allowArrayNode = (ArrayNode) allowNode;
                    for (JsonNode actionNode : allowArrayNode) {
                        if (actionNode.isTextual()) {
                            StingrayServiceAction action = StingrayServiceAction.from(actionNode.textValue());
                            permissionByAction.put(action, StingrayPermission.ALLOW);
                        }
                    }
                }
            }
            if (policyNode.has("deny")) {
                JsonNode allowNode = policyNode.get("deny");
                if (allowNode.isArray()) {
                    ArrayNode allowArrayNode = (ArrayNode) allowNode;
                    for (JsonNode actionNode : allowArrayNode) {
                        if (actionNode.isTextual()) {
                            StingrayServiceAction action = StingrayServiceAction.from(actionNode.textValue());
                            permissionByAction.put(action, StingrayPermission.DENY);
                        }
                    }
                }
            }
            return this;
        }

        public Builder allow(Iterable<StingrayServiceAction> actions) {
            for (StingrayServiceAction action : actions) {
                permissionByAction.put(action, StingrayPermission.ALLOW);
            }
            return this;
        }

        public Builder allow(StingrayServiceAction action) {
            permissionByAction.put(action, StingrayPermission.ALLOW);
            return this;
        }

        public Builder allow(String action) {
            permissionByAction.put(StingrayServiceAction.from(action), StingrayPermission.ALLOW);
            return this;
        }

        public Builder deny(Iterable<StingrayServiceAction> deniedActions) {
            if (deniedActions != null) {
                for (StingrayServiceAction action : deniedActions) {
                    permissionByAction.put(action, StingrayPermission.DENY);
                }
            }
            return this;
        }

        public Builder deny(StingrayServiceAction action) {
            permissionByAction.put(action, StingrayPermission.DENY);
            return this;
        }

        public Builder deny(String action) {
            permissionByAction.put(StingrayServiceAction.from(action), StingrayPermission.DENY);
            return this;
        }

        public Builder allowAllActionsOnService(String service) {
            permissionByAction.put(new StingrayServiceAction(service, "*"), StingrayPermission.ALLOW);
            return this;
        }

        public Builder allowSomeActionsOnService(String service, String... someActions) {
            for (String someAction : someActions) {
                permissionByAction.put(new StingrayServiceAction(service, someAction), StingrayPermission.ALLOW);
            }
            return this;
        }

        public Builder denySomeActionsOnService(String service, String... someActions) {
            for (String someAction : someActions) {
                permissionByAction.put(new StingrayServiceAction(service, someAction), StingrayPermission.DENY);
            }
            return this;
        }

        public Builder denyAllActionsOnService(String service) {
            permissionByAction.put(new StingrayServiceAction(service, "*"), StingrayPermission.DENY);
            return this;
        }

        public Builder aggregate(StingrayPolicy... policies) {
            return aggregate(Arrays.asList(policies));
        }

        public Builder aggregate(Stream<StingrayPolicy> policies) {
            return aggregate(policies.collect(Collectors.toList()));
        }

        public Builder aggregate(Iterable<StingrayPolicy> policies) {
            for (StingrayPolicy policy : policies) {
                for (Map.Entry<StingrayServiceAction, StingrayPermission> entry : policy.permissionByAction.entrySet()) {
                    StingrayServiceAction action = entry.getKey();
                    StingrayPermission permission = entry.getValue();
                    StingrayPermission existingPermission = permissionByAction.get(action);
                    if (existingPermission == null || existingPermission == StingrayPermission.ALLOW) {
                        permissionByAction.put(action, permission);
                    }
                }
            }
            return this;
        }

        public StingrayPolicy build() {
            return new StingrayPolicy(policyId, permissionByAction);
        }
    }
}
