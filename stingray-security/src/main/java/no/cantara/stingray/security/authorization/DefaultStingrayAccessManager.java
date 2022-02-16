package no.cantara.stingray.security.authorization;

import no.cantara.config.ApplicationProperties;
import no.cantara.stingray.security.authentication.StingrayApplicationAuthentication;
import no.cantara.stingray.security.authentication.StingrayAuthentication;
import no.cantara.stingray.security.authentication.StingrayUserAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DefaultStingrayAccessManager implements StingrayAccessManager {

    private static final Logger log = LoggerFactory.getLogger(DefaultStingrayAccessManager.class);

    final String actionService;

    final Map<String, StingrayRole> roleById;
    final Map<String, StingrayPolicy> policyById;
    final Map<String, StingrayGroup> groupById;
    final Map<String, StingrayServiceAction> actionByDefintion;

    final Map<String, StingrayConfiguredUserPermissions> configuredUserPermissionsByUserId;
    final Map<String, StingrayConfiguredApplicationPermissions> configuredApplicationPermissionsByApplicationId;

    final StingrayConfiguredUserPermissions defaultUserPermissions;
    final StingrayConfiguredApplicationPermissions defaultApplicationPermissions;

    /**
     * @param actionService       the service part of actions. e.g. if "abc" is passed, any {@link StingrayServiceAction} instances made as
     *                            part of initializing this access-manager will get the service field as "abc".
     * @param authorizationConfig authorization configuration.
     */
    public DefaultStingrayAccessManager(String actionService, ApplicationProperties authorizationConfig) {
        this.actionService = actionService;
        this.actionByDefintion = initActions(authorizationConfig);
        this.policyById = initPolicies(authorizationConfig);
        this.roleById = initRoles(authorizationConfig);
        this.groupById = initGroups(authorizationConfig);
        this.configuredUserPermissionsByUserId = initUserPermissions(authorizationConfig);
        this.defaultUserPermissions = initDefaultUserPermissions(authorizationConfig);
        this.configuredApplicationPermissionsByApplicationId = initApplicationPermissions(authorizationConfig);
        this.defaultApplicationPermissions = initDefaultApplicationPermissions(authorizationConfig);
    }

    Map<String, StingrayServiceAction> initActions(ApplicationProperties authorizationConfig) {
        Map<String, StingrayServiceAction> actionByDefinition = new LinkedHashMap<>();
        String[] actionValues = authorizationConfig.get("actions", "").split(",");
        for (String actionValue : actionValues) {
            actionValue = actionValue.trim();
            if (!actionValue.isEmpty()) {
                StingrayServiceAction action = new StingrayServiceAction(actionService, actionValue);
                actionByDefinition.put(action.toString(), action);
            }
        }
        log.info("Added actions for service '{}': '{}'", actionService, actionByDefinition.values().stream().map(StingrayServiceAction::getAction).collect(Collectors.joining(", ")));
        return actionByDefinition;
    }

    private void initSystemPolicies(Map<String, StingrayPolicy> policyById) {
        StingrayPolicy fullaccessPolicy = StingrayPolicy.builder().policyId("fullaccess").allow(StingrayServiceAction.ALL_ACTIONS_ON_ALL_SERVICES).build();
        policyById.put(fullaccessPolicy.getPolicyId(), fullaccessPolicy);
        logPolicyAdded(fullaccessPolicy, true);
        StingrayPolicy noaccessPolicy = StingrayPolicy.builder().policyId("noaccess").deny(StingrayServiceAction.ALL_ACTIONS_ON_ALL_SERVICES).build();
        policyById.put(noaccessPolicy.getPolicyId(), noaccessPolicy);
        logPolicyAdded(noaccessPolicy, true);
        final StingrayServiceAction ALL_ACTIONS_ON_SERVICE = new StingrayServiceAction(actionService, "*");
        StingrayPolicy serviceaccessPolicy = StingrayPolicy.builder().policyId("serviceaccess").allow(ALL_ACTIONS_ON_SERVICE).build();
        policyById.put(serviceaccessPolicy.getPolicyId(), serviceaccessPolicy);
        logPolicyAdded(serviceaccessPolicy, true);
        StingrayPolicy noserviceaccessPolicy = StingrayPolicy.builder().policyId("noserviceaccess").deny(ALL_ACTIONS_ON_SERVICE).build();
        policyById.put(noserviceaccessPolicy.getPolicyId(), noserviceaccessPolicy);
        logPolicyAdded(noserviceaccessPolicy, true);
    }

    Map<String, StingrayPolicy> initPolicies(ApplicationProperties authorizationConfig) {
        Map<String, StingrayPolicy> policyById = new LinkedHashMap<>();
        initSystemPolicies(policyById);
        ApplicationProperties policyConfig = authorizationConfig.subTree("policy");
        Set<String> policyKeys = policyConfig.map().keySet();
        Set<String> policyIds = new LinkedHashSet<>();
        for (String policyKey : policyKeys) {
            int dotIndex = policyKey.indexOf('.');
            if (dotIndex > 0) {
                String policyId = policyKey.substring(0, dotIndex);
                policyIds.add(policyId);
            }
        }
        for (String policyId : policyIds) {
            ApplicationProperties policyProperties = policyConfig.subTree(policyId);
            StingrayPolicy.Builder policyBuilder = StingrayPolicy.builder()
                    .policyId(policyId);
            {
                String allowStr = policyProperties.get("allow");
                if (allowStr != null) {
                    String[] parts = allowStr.split(",");
                    for (String action : parts) {
                        action = action.trim();
                        if (!action.isEmpty()) {
                            policyBuilder.allow(new StingrayServiceAction(actionService, action));
                        }
                    }
                }
            }
            {
                String denyStr = policyProperties.get("deny");
                if (denyStr != null) {
                    String[] parts = denyStr.split(",");
                    for (String action : parts) {
                        action = action.trim();
                        if (!action.isEmpty()) {
                            policyBuilder.deny(new StingrayServiceAction(actionService, action));
                        }
                    }
                }
            }
            StingrayPolicy policy = policyBuilder.build();
            policyById.put(policy.getPolicyId(), policy);
            logPolicyAdded(policy, false);
        }

        return policyById;
    }

    private void logPolicyAdded(StingrayPolicy policy, boolean system) {
        log.info("Added {}policy '{}'. allow: '{}', deny: '{}'", system ? "system " : "", policy.getPolicyId(),
                policy.permissionByAction.entrySet().stream().filter(e -> e.getValue() == StingrayPermission.ALLOW).map(Map.Entry::getKey).map(StingrayServiceAction::toString).collect(Collectors.joining(",")),
                policy.permissionByAction.entrySet().stream().filter(e -> e.getValue() == StingrayPermission.DENY).map(Map.Entry::getKey).map(StingrayServiceAction::toString).collect(Collectors.joining(",")));
    }

    private void initSystemRoles(Map<String, StingrayRole> roleById) {
        StingrayRole.Builder superUserRoleBuilder = StingrayRole.builder();
        StingrayRole superuserRole = superUserRoleBuilder
                .roleId("superuser")
                .addPolicy(policyById.get("fullaccess"))
                .build();
        roleById.put(superuserRole.getRoleId(), superuserRole);
        logRoleAdded(superUserRoleBuilder, true);
    }

    Map<String, StingrayRole> initRoles(ApplicationProperties authorizationConfig) {
        Map<String, StingrayRole> roleById = new LinkedHashMap<>();
        initSystemRoles(roleById);
        Map<String, StingrayRole.Builder> roleBuilderById = new LinkedHashMap<>();
        Map<String, List<String>> roleChildrenByRoleId = new LinkedHashMap<>();

        ApplicationProperties roleConfig = authorizationConfig.subTree("role");
        Set<String> roleKeys = roleConfig.map().keySet();
        Set<String> roleIds = new LinkedHashSet<>();
        for (String roleKey : roleKeys) {
            int dotIndex = roleKey.indexOf('.');
            if (dotIndex > 0) {
                String roleId = roleKey.substring(0, dotIndex);
                roleIds.add(roleId);
            }
        }

        for (String roleId : roleIds) {
            ApplicationProperties roleProperties = roleConfig.subTree(roleId);
            StingrayRole.Builder roleBuilder = StingrayRole.builder()
                    .roleId(roleId);
            roleBuilderById.put(roleId, roleBuilder);
            {
                String policiesStr = roleProperties.get("policies");
                if (policiesStr != null) {
                    String[] parts = policiesStr.split(",");
                    for (String policyId : parts) {
                        policyId = policyId.trim();
                        if (!policyId.isEmpty()) {
                            StingrayPolicy policy = policyById.get(policyId);
                            if (policy == null) {
                                throw new IllegalArgumentException(
                                        String.format("Role mis-configuration. Property 'role.%s.policies=%s' refers to policyId '%s' which must be configured with either 'policy.%s.allow=actions...' and/or 'policy.%s.deny=actions...'",
                                                roleId, policiesStr, policyId, policyId, policyId));
                            }
                            roleBuilder.addPolicy(policy);
                        }
                    }
                }
            }
            {
                List<String> childrenRoleIds = new ArrayList<>();
                String childrenStr = roleProperties.get("children");
                if (childrenStr != null) {
                    String[] parts = childrenStr.split(",");
                    for (String childRoleId : parts) {
                        childRoleId = childRoleId.trim();
                        if (!childRoleId.isEmpty()) {
                            childrenRoleIds.add(childRoleId);
                        }
                    }
                }
                roleChildrenByRoleId.put(roleId, childrenRoleIds);
            }
        }

        Set<String> parents = new LinkedHashSet<>();
        for (String roleId : roleBuilderById.keySet()) {
            StingrayRole role = roleById.get(roleId);
            if (role == null) {
                role = recursiveBuildAndAddRole(parents, roleId, roleById, roleBuilderById, roleChildrenByRoleId);
            }
        }

        return roleById;
    }

    StingrayRole recursiveBuildAndAddRole(Set<String> parents, String roleId, Map<String, StingrayRole> roleById, Map<String, StingrayRole.Builder> roleBuilderById, Map<String, List<String>> roleChildrenByRoleId) {
        if (parents.contains(roleId)) {
            throw new IllegalArgumentException(String.format("Circular role hierarchy. role '%s' refers to itself through chain: %s->%s", roleId, String.join("->", parents), roleId));
        }
        StingrayRole role = roleById.get(roleId);
        if (role != null) {
            return role;
        }

        StingrayRole.Builder roleBuilder = roleBuilderById.get(roleId);

        if (roleBuilder == null) {
            return null;
        }

        List<String> childrenIds = roleChildrenByRoleId.get(roleId);
        if (childrenIds != null) {
            parents.add(roleId);
            try {
                for (String childRoleId : childrenIds) {
                    StingrayRole childRole = roleById.get(childRoleId);
                    if (childRole == null) {
                        childRole = recursiveBuildAndAddRole(parents, childRoleId, roleById, roleBuilderById, roleChildrenByRoleId);
                    }
                    if (childRole == null) {
                        throw new IllegalArgumentException(
                                String.format("Role mis-configuration. Property 'role.%s.children=%s' refers to roleId '%s' which must be configured with either 'role.%s.policies=policyId...' and/or 'role.%s.children=childRoleId...'",
                                        roleId, String.join(",", childrenIds), childRoleId, childRoleId, childRoleId));
                    }
                    roleBuilder.addChild(childRole);
                }
            } finally {
                parents.remove(roleId);
            }
        }

        role = roleBuilder.build();
        roleById.put(roleId, role);
        logRoleAdded(roleBuilder, false);

        return role;
    }

    private void logRoleAdded(StingrayRole.Builder roleBuilder, boolean system) {
        log.info("Added {}role '{}'. children: '{}', policies: '{}'", system ? "system " : "", roleBuilder.roleId,
                roleBuilder.children.stream().map(StingrayRole::getRoleId).collect(Collectors.joining(",")),
                roleBuilder.policies.stream().map(StingrayPolicy::getPolicyId).collect(Collectors.joining(",")));
    }

    private void initSystemGroups(Map<String, StingrayGroup> groupById) {
        StingrayGroup entraOsAdminsGroup = StingrayGroup.builder()
                .groupId("entraosadmins")
                .addRole(roleById.get("superuser"))
                .build();
        groupById.put(entraOsAdminsGroup.getGroupId(), entraOsAdminsGroup);
        logGroupAdded(entraOsAdminsGroup.getGroupId(), Collections.emptyList(), Collections.singletonList("superuser"), true);
        StingrayGroup blacklistGroup = StingrayGroup.builder()
                .groupId("blacklist")
                .addPolicy(policyById.get("noaccess"))
                .build();
        groupById.put(blacklistGroup.getGroupId(), blacklistGroup);
        logGroupAdded(blacklistGroup.getGroupId(), Collections.singletonList("noaccess"), Collections.emptyList(), true);
    }

    private Map<String, StingrayGroup> initGroups(ApplicationProperties authorizationConfig) {
        Map<String, StingrayGroup> groupById = new LinkedHashMap<>();
        initSystemGroups(groupById);

        ApplicationProperties groupConfig = authorizationConfig.subTree("group");
        Set<String> groupKeys = groupConfig.map().keySet();
        Set<String> groupIds = new LinkedHashSet<>();
        for (String groupKey : groupKeys) {
            int dotIndex = groupKey.indexOf('.');
            if (dotIndex > 0) {
                String roleId = groupKey.substring(0, dotIndex);
                groupIds.add(roleId);
            }
        }

        for (String groupId : groupIds) {
            ApplicationProperties groupProperties = groupConfig.subTree(groupId);
            StingrayGroup.Builder groupBuilder = StingrayGroup.builder()
                    .groupId(groupId);
            List<String> policyIds = new ArrayList<>();
            {
                String policiesStr = groupProperties.get("policies");
                if (policiesStr != null) {
                    String[] parts = policiesStr.split(",");
                    for (String policyId : parts) {
                        policyId = policyId.trim();
                        if (!policyId.isEmpty()) {
                            StingrayPolicy policy = policyById.get(policyId);
                            if (policy == null) {
                                throw new IllegalArgumentException(
                                        String.format("Group mis-configuration. Property 'group.%s.policies=%s' refers to policyId '%s' which must be configured with either 'policy.%s.allow=actions...' and/or 'policy.%s.deny=actions...'",
                                                groupId, policiesStr, policyId, policyId, policyId));
                            }
                            groupBuilder.addPolicy(policy);
                            policyIds.add(policyId);
                        }
                    }
                }
            }
            List<String> roleIds = new ArrayList<>();
            {
                String rolesStr = groupProperties.get("roles");
                if (rolesStr != null) {
                    String[] parts = rolesStr.split(",");
                    for (String roleId : parts) {
                        roleId = roleId.trim();
                        if (!roleId.isEmpty()) {
                            StingrayRole role = roleById.get(roleId);
                            if (role == null) {
                                throw new IllegalArgumentException(
                                        String.format("Group mis-configuration. Property 'group.%s.roles=%s' refers to roleId '%s' which must be configured with either 'role.%s.policies=policyId...' and/or 'role.%s.children=childRoleId...'",
                                                groupId, rolesStr, roleId, roleId, roleId));
                            }
                            groupBuilder.addRole(role);
                            roleIds.add(roleId);
                        }
                    }
                }
            }
            StingrayGroup group = groupBuilder.build();
            groupById.put(groupId, group);
            logGroupAdded(groupId, policyIds, roleIds, false);
        }

        return groupById;
    }

    private void logGroupAdded(String groupId, Iterable<String> policyIds, Iterable<String> roleIds, boolean system) {
        log.info("Added {}group '{}'. roles: '{}', policies: '{}'", system ? "system " : "", groupId,
                String.join(",", roleIds),
                String.join(",", policyIds));
    }

    Map<String, StingrayConfiguredUserPermissions> initUserPermissions(ApplicationProperties authorizationConfig) {
        Map<String, StingrayConfiguredUserPermissions> configuredUserPermissionsByUserId = new ConcurrentHashMap<>();

        ApplicationProperties userConfig = authorizationConfig.subTree("user");
        Set<String> userKeys = userConfig.map().keySet();
        Set<String> userIds = new LinkedHashSet<>();
        for (String userKey : userKeys) {
            int dotIndex = userKey.indexOf('.');
            if (dotIndex > 0) {
                String userId = userKey.substring(0, dotIndex);
                userIds.add(userId);
            }
        }
        for (String userId : userIds) {
            ApplicationProperties userProperties = userConfig.subTree(userId);
            List<StingrayGroup> groups = new ArrayList<>();
            List<StingrayRole> roles = new ArrayList<>();
            List<StingrayPolicy> policies = new ArrayList<>();

            {
                String groupsStr = userProperties.get("groups");
                if (groupsStr != null) {
                    String[] parts = groupsStr.split(",");
                    for (String groupId : parts) {
                        groupId = groupId.trim();
                        if (!groupId.isEmpty()) {
                            StingrayGroup group = groupById.get(groupId);
                            if (group != null) {
                                groups.add(group);
                            } else {
                                log.warn("Configured property 'user.{}.groups' assigned non-existing group '{}'", userId, groupId);
                            }
                        }
                    }
                }
            }
            {
                String rolesStr = userProperties.get("roles");
                if (rolesStr != null) {
                    String[] parts = rolesStr.split(",");
                    for (String roleId : parts) {
                        roleId = roleId.trim();
                        if (!roleId.isEmpty()) {
                            StingrayRole role = roleById.get(roleId);
                            if (role != null) {
                                roles.add(role);
                            } else {
                                log.warn("Configured property 'user.{}.roles' assigned non-existing role '{}'", userId, roleId);
                            }
                        }
                    }
                }
            }
            {
                String policiesStr = userProperties.get("policies");
                if (policiesStr != null) {
                    String[] parts = policiesStr.split(",");
                    for (String policyId : parts) {
                        policyId = policyId.trim();
                        if (!policyId.isEmpty()) {
                            StingrayPolicy policy = policyById.get(policyId);
                            if (policy != null) {
                                policies.add(policy);
                            } else {
                                log.warn("Configured property 'user.{}.policies' assigned non-existing policy '{}'", userId, policyId);
                            }
                        }
                    }
                }
            }

            configuredUserPermissionsByUserId.put(userId, new StingrayConfiguredUserPermissions(userId, groups, roles, policies));
            log.info("Assigned user '{}' to groups: '{}', roles: '{}', policies: '{}'", userId,
                    groups.stream().map(StingrayGroup::getGroupId).collect(Collectors.joining(",")),
                    roles.stream().map(StingrayRole::getRoleId).collect(Collectors.joining(",")),
                    policies.stream().map(StingrayPolicy::getPolicyId).collect(Collectors.joining(",")));
        }

        return configuredUserPermissionsByUserId;
    }

    StingrayConfiguredUserPermissions initDefaultUserPermissions(ApplicationProperties authorizationConfig) {
        String defaultUserGroupId = authorizationConfig.get("default.user-group");
        List<StingrayGroup> defaultGroups = new LinkedList<>();
        if (defaultUserGroupId != null) {
            StingrayGroup defaultUserGroup = groupById.get(defaultUserGroupId);
            if (defaultUserGroup != null) {
                defaultGroups.add(defaultUserGroup);
            }
        }
        StingrayConfiguredUserPermissions defaultConfiguredUserPermissions = new StingrayConfiguredUserPermissions("default-user", defaultGroups, Collections.emptyList(), Collections.emptyList());
        log.info("Assigned default users to groups: '{}'",
                defaultGroups.stream().map(StingrayGroup::getGroupId).collect(Collectors.joining(","))
        );
        return defaultConfiguredUserPermissions;
    }

    Map<String, StingrayConfiguredApplicationPermissions> initApplicationPermissions(ApplicationProperties authorizationConfig) {
        Map<String, StingrayConfiguredApplicationPermissions> configuredApplicationPermissionsByApplicationId = new ConcurrentHashMap<>();

        ApplicationProperties applicationConfig = authorizationConfig.subTree("application");
        Set<String> applicationKeys = applicationConfig.map().keySet();
        Set<String> applicationIds = new LinkedHashSet<>();
        for (String applicationKey : applicationKeys) {
            int dotIndex = applicationKey.indexOf('.');
            if (dotIndex > 0) {
                String applicationId = applicationKey.substring(0, dotIndex);
                applicationIds.add(applicationId);
            }
        }
        for (String applicationId : applicationIds) {
            ApplicationProperties applicationProperties = applicationConfig.subTree(applicationId);
            List<StingrayGroup> groups = new ArrayList<>();
            List<StingrayRole> roles = new ArrayList<>();
            List<StingrayPolicy> policies = new ArrayList<>();
            {
                String groupsStr = applicationProperties.get("groups");
                if (groupsStr != null) {
                    String[] parts = groupsStr.split(",");
                    for (String groupId : parts) {
                        groupId = groupId.trim();
                        if (!groupId.isEmpty()) {
                            StingrayGroup group = groupById.get(groupId);
                            if (group != null) {
                                groups.add(group);
                            } else {
                                log.warn("Configured property 'application.{}.groups' assigned non-existing group '{}'", applicationId, groupId);
                            }
                        }
                    }
                }
            }
            {
                String rolesStr = applicationProperties.get("roles");
                if (rolesStr != null) {
                    String[] parts = rolesStr.split(",");
                    for (String roleId : parts) {
                        roleId = roleId.trim();
                        if (!roleId.isEmpty()) {
                            StingrayRole role = roleById.get(roleId);
                            if (role != null) {
                                roles.add(role);
                            } else {
                                log.warn("Configured property 'application.{}.roles' assigned non-existing role '{}'", applicationId, roleId);
                            }
                        }
                    }
                }
            }
            {
                String policiesStr = applicationProperties.get("policies");
                if (policiesStr != null) {
                    String[] parts = policiesStr.split(",");
                    for (String policyId : parts) {
                        policyId = policyId.trim();
                        if (!policyId.isEmpty()) {
                            StingrayPolicy policy = policyById.get(policyId);
                            if (policy != null) {
                                policies.add(policy);
                            } else {
                                log.warn("Configured property 'application.{}.policies' assigned non-existing policy '{}'", applicationId, policyId);
                            }
                        }
                    }
                }
            }

            configuredApplicationPermissionsByApplicationId.put(applicationId, new StingrayConfiguredApplicationPermissions(applicationId, groups, roles, policies));
            log.info("Assigned application '{}' to groups: '{}', roles: '{}', policies: '{}'", applicationId,
                    groups.stream().map(StingrayGroup::getGroupId).collect(Collectors.joining(",")),
                    roles.stream().map(StingrayRole::getRoleId).collect(Collectors.joining(",")),
                    policies.stream().map(StingrayPolicy::getPolicyId).collect(Collectors.joining(",")));
        }

        return configuredApplicationPermissionsByApplicationId;
    }

    StingrayConfiguredApplicationPermissions initDefaultApplicationPermissions(ApplicationProperties authorizationConfig) {
        String defaultApplicationGroupId = authorizationConfig.get("default.application-group");
        List<StingrayGroup> defaultGroups = new LinkedList<>();
        if (defaultApplicationGroupId != null) {
            StingrayGroup defaultApplicationGroup = groupById.get(defaultApplicationGroupId);
            if (defaultApplicationGroup != null) {
                defaultGroups.add(defaultApplicationGroup);
            }
        }
        StingrayConfiguredApplicationPermissions defaultConfiguredApplicationPermissions = new StingrayConfiguredApplicationPermissions("default-application", defaultGroups, Collections.emptyList(), Collections.emptyList());
        log.info("Assigned default applications to groups: '{}'",
                defaultGroups.stream().map(StingrayGroup::getGroupId).collect(Collectors.joining(","))
        );
        return defaultConfiguredApplicationPermissions;
    }

    @Override
    public boolean hasAccess(StingrayAuthentication authentication, String action) {
        Objects.requireNonNull(authentication);
        if (authentication instanceof StingrayUserAuthentication) {
            StingrayUserAuthentication userAuthentication = (StingrayUserAuthentication) authentication;
            return userHasAccess(userAuthentication.ssoId(), authentication.groups(), action);
        }
        if (authentication instanceof StingrayApplicationAuthentication) {
            StingrayApplicationAuthentication applicationAuthentication = (StingrayApplicationAuthentication) authentication;
            return applicationHasAccess(applicationAuthentication.ssoId(), authentication.groups(), action);
        }
        return false;
    }

    @Override
    public boolean userHasAccess(String userId, List<String> assignedGroups, String actionValue) {
        Objects.requireNonNull(userId);
        validateAction(actionValue);
        StingrayConfiguredUserPermissions cup = configuredUserPermissionsByUserId.get(userId);
        StingrayPolicy.Builder effectivePolicyBuilder = StingrayPolicy.builder()
                .policyId("effective-dynamic-policy-for-user-" + userId);
        if (cup != null) {
            log.trace("Adding pre-configured permissions to user '{}'", userId);
            effectivePolicyBuilder.aggregate(cup.getEffectivePolicy());
        } else {
            log.trace("Adding default permissions to user '{}'", userId);
            effectivePolicyBuilder.aggregate(defaultUserPermissions.getEffectivePolicy());
        }
        if (assignedGroups != null && assignedGroups.size() > 0) {
            log.trace("Adding authenticated access-groups to user '{}': {}", userId, String.join(",", assignedGroups));
            effectivePolicyBuilder.aggregate(assignedGroups.stream()
                    .map(groupById::get)
                    .filter(Objects::nonNull)
                    .map(StingrayGroup::getPolicy));
        }
        StingrayPolicy policy = effectivePolicyBuilder.build();
        log.trace("Using effective user '{}' policy: {}", userId, policy.toJson());
        StingrayServiceAction action = new StingrayServiceAction(actionService, actionValue.trim());
        StingrayPermissionDecision permissionDecision = policy.decidePermission(action);
        return permissionDecision.isAllowed();
    }

    @Override
    public boolean applicationHasAccess(String applicationId, List<String> assignedGroups, String actionValue) {
        Objects.requireNonNull(applicationId);
        validateAction(actionValue);
        StingrayConfiguredApplicationPermissions cap = configuredApplicationPermissionsByApplicationId.get(applicationId);
        StingrayPolicy.Builder effectivePolicyBuilder = StingrayPolicy.builder()
                .policyId("effective-dynamic-policy-for-application-" + applicationId);
        if (cap != null) {
            log.trace("Adding pre-configured permissions to application '{}'", applicationId);
            effectivePolicyBuilder.aggregate(cap.getEffectivePolicy());
        } else {
            log.trace("Adding default permissions to application '{}'", applicationId);
            effectivePolicyBuilder.aggregate(defaultApplicationPermissions.getEffectivePolicy());
        }
        if (assignedGroups != null && assignedGroups.size() > 0) {
            log.trace("Adding authenticated access-groups to application '{}': {}", applicationId, String.join(",", assignedGroups));
            effectivePolicyBuilder.aggregate(assignedGroups.stream()
                    .map(groupById::get)
                    .filter(Objects::nonNull)
                    .map(StingrayGroup::getPolicy));
        }
        StingrayPolicy policy = effectivePolicyBuilder.build();
        log.trace("Using effective application '{}' policy: {}", applicationId, policy.toJson());
        StingrayServiceAction action = new StingrayServiceAction(actionService, actionValue.trim());
        StingrayPermissionDecision permissionDecision = policy.decidePermission(action);
        return permissionDecision.isAllowed();
    }

    private void validateAction(String action) {
        Objects.requireNonNull(action);
        action = action.trim();
        if (action.isEmpty()) {
            throw new IllegalArgumentException("Empty action not allowed.");
        }
        if ("*".equals(action)) {
            throw new IllegalArgumentException("Action '*' not allowed.");
        }
    }
}
