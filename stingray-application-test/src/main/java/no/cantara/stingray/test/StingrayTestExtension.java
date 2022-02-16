package no.cantara.stingray.test;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import no.cantara.config.ApplicationProperties;
import no.cantara.config.ProviderLoader;
import no.cantara.stingray.application.StingrayApplication;
import no.cantara.stingray.application.StingrayApplicationFactory;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Optional.ofNullable;

public class StingrayTestExtension implements BeforeEachCallback, BeforeAllCallback, AfterAllCallback {

    final Map<String, StingrayApplication> applicationByAlias = new ConcurrentHashMap<>();
    final Map<String, StingrayTestClient> clientByAlias = new ConcurrentHashMap<>();
    final Set<String> applicationAliases = new CopyOnWriteArraySet<>();
    final AtomicInteger nextAppInitThreadId = new AtomicInteger(1);
    final ExecutorService appInitExecutor = Executors.newCachedThreadPool(runnable -> new Thread(runnable, "app-init-" + nextAppInitThreadId.getAndIncrement()));

    StingrayNode root;

    @Override
    public void beforeAll(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();

        StingrayApplicationProvider stingrayApplicationProvider = testClass.getDeclaredAnnotation(StingrayApplicationProvider.class);

        if (stingrayApplicationProvider == null || stingrayApplicationProvider.value().length <= 1) {

            /*
             * Single application
             */

            String providerAlias = null;
            if (stingrayApplicationProvider == null || stingrayApplicationProvider.value().length == 0) {
                // pick first available application
                ServiceLoader<StingrayApplicationFactory> loader = ServiceLoader.load(StingrayApplicationFactory.class);
                for (StingrayApplicationFactory factory : loader) {
                    providerAlias = factory.alias();
                    break;
                }
                if (providerAlias == null) {
                    throw new IllegalArgumentException("Error, Provide a service-provider in META-INF/services folder that implements the interface " + StingrayApplicationFactory.class.getName());
                }
            } else {
                // guaranteed (stingrayApplicationProvider.value().length == 1)
                providerAlias = stingrayApplicationProvider.value()[0];
            }

            applicationAliases.add(providerAlias);

        } else {

            /*
             * Multi application
             */

            for (String providerAlias : stingrayApplicationProvider.value()) {
                applicationAliases.add(providerAlias);
            }
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();
        Object testInstance = context.getRequiredTestInstance();

        if (root == null) {
            root = buildDependencyGraph(testClass, applicationAliases);
            Set<StingrayNode> ancestors = new LinkedHashSet<>();
            StingrayNodeTraversals.depthFirstPostOrderWithConcurrentSiblingExecutions(appInitExecutor, ancestors, root, node -> {
                if (node == root) {
                    return;
                }

                String providerAlias = node.getId();
                ApplicationProperties.Builder configBuilder = node.get("config-builder");
                configBuilder.enableSystemProperties()
                        .enableEnvironmentVariables();

                if (testInstance instanceof StingrayBeforeCreationLifecycleListener) {
                    StingrayBeforeCreationLifecycleListener lifecycleListener = (StingrayBeforeCreationLifecycleListener) testInstance;
                    lifecycleListener.beforeCreation(configBuilder);
                }

                ApplicationProperties config = configBuilder.build();

                /*
                 * Initialize application
                 */
                StingrayApplication application = (StingrayApplication) ProviderLoader.configure(config, providerAlias, StingrayApplicationFactory.class);
                applicationByAlias.put(providerAlias, application);
                initTestApplication(application, testInstance, providerAlias);

                if (node.getParents().get(0).equals(root)) {
                    return; // only meta-node root depends on this application
                }
                for (StingrayNode parent : node.getParents()) {
                    String parentConfigKey = parent.getAttribute("${" + providerAlias + ".port}");
                    ApplicationProperties.Builder parentConfigBuilder = parent.get("config-builder");

                    /*
                     * Override expression with value of bound port
                     */
                    parentConfigBuilder.values().put(parentConfigKey, application.getBoundPort()).end();
                }
            });
        }

        for (Map.Entry<String, StingrayApplication> entry : applicationByAlias.entrySet()) {
            String key = entry.getKey();
            StingrayApplication application = entry.getValue();
            if (!application.isInitialized()) {
                ApplicationProperties config = application.get(ApplicationProperties.class);
            }
        }

        Object test = context.getRequiredTestInstance();
        Field[] fields = test.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(Inject.class)) {
                continue;
            }
            String fieldNamed = null;
            if (field.isAnnotationPresent(Named.class)) {
                Named named = field.getDeclaredAnnotation(Named.class);
                fieldNamed = named.value();
            }
            // application
            if (StingrayApplication.class.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    if (field.get(test) == null) {
                        if (fieldNamed != null) {
                            field.set(test, applicationByAlias.get(fieldNamed));
                        } else {
                            field.set(test, applicationByAlias.values().iterator().next());
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            if (StingrayTestClient.class.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    if (field.get(test) == null) {
                        if (fieldNamed != null) {
                            field.set(test, clientByAlias.get(fieldNamed));
                        } else {
                            field.set(test, clientByAlias.values().iterator().next());
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private StingrayNode buildDependencyGraph(Class<?> testClass, Set<String> providerAliases) {
        final StingrayNode root = new StingrayNode(null, "root");
        final Map<String, StingrayNode> nodeById = new LinkedHashMap<>();
        final Set<String> patterns = new LinkedHashSet<>();
        final List<StingrayNode> nodes = new LinkedList<>();
        for (String providerAlias : providerAliases) {
            StingrayNode node = new StingrayNode(root, providerAlias);
            nodeById.put(providerAlias, node);
            root.addChild(node);
            patterns.add("${" + providerAlias + ".port}");
            nodes.add(node);
        }

        // Build dependency graph
        final Pattern pattern = Pattern.compile("\\$\\{([^.]+)[.]port}");
        for (StingrayNode node : nodes) {
            ApplicationProperties.Builder configBuilder = resolveConfiguration(testClass, node.getId());
            node.put("config-builder", configBuilder);
            ApplicationProperties config = configBuilder.build();
            for (Map.Entry<String, String> entry : config.map().entrySet()) {
                String value = entry.getValue();
                if (patterns.contains(value)) {
                    Matcher m = pattern.matcher(value);
                    if (!m.matches()) {
                        throw new RuntimeException("Pattern does not match, wi");
                    }
                    node.putAttribute(value, entry.getKey());
                    String dependOnApplication = m.group(1);
                    StingrayNode dependOnNode = nodeById.get(dependOnApplication);
                    if (dependOnNode == null) {
                        throw new RuntimeException("Configuration expression points to an application that is not configured. Expression: " + value);
                    }
                    node.addChild(dependOnNode);
                    dependOnNode.getParents().add(node);
                    dependOnNode.getParents().remove(root);
                    root.getChildren().remove(dependOnNode);
                }
            }
        }
        return root;
    }

    private ApplicationProperties.Builder resolveConfiguration(Class<?> testClass, String providerAlias) {
        String profile = ofNullable(System.getProperty("config.profile"))
                .orElseGet(() -> ofNullable(System.getenv("CONFIG_PROFILE"))
                        .orElse(providerAlias) // default
                );
        ApplicationProperties.Builder configBuilder = ApplicationProperties.builder();
        StingrayConfigTestUtils.conventions(configBuilder, profile);
        {
            StingrayConfigOverride configOverride = testClass.getDeclaredAnnotation(StingrayConfigOverride.class);
            overrideConfig(configBuilder, configOverride, providerAlias);
        }
        StingrayConfigOverrides configOverrides = testClass.getDeclaredAnnotation(StingrayConfigOverrides.class);
        if (configOverrides != null) {
            for (StingrayConfigOverride configOverride : configOverrides.value()) {
                overrideConfig(configBuilder, configOverride, providerAlias);
            }
        }
        return configBuilder;
    }

    private void overrideConfig(ApplicationProperties.Builder configBuilder, StingrayConfigOverride configOverride, String providerAlias) {
        if (configOverride == null) {
            return;
        }
        if (!configOverride.application().isEmpty() && !configOverride.application().equals(providerAlias)) {
            return; // configuration override not mapped to this provider-alias
        }
        String[] overrideArray = configOverride.value();
        Map<String, String> configOverrideMap = new LinkedHashMap<>();
        for (int i = 0; i < overrideArray.length; i += 2) {
            configOverrideMap.put(overrideArray[i], overrideArray[i + 1]);
        }
        configBuilder.map(configOverrideMap);
    }

    private StingrayApplication initTestApplication(StingrayApplication application, Object testInstance, String providerAlias) {

        if (testInstance instanceof StingrayBeforeInitLifecycleListener) {
            StingrayBeforeInitLifecycleListener lifecycleListener = (StingrayBeforeInitLifecycleListener) testInstance;
            lifecycleListener.beforeInit(application);
        }

        application.init();

        if (testInstance instanceof StingrayAfterInitLifecycleListener) {
            StingrayAfterInitLifecycleListener lifecycleListener = (StingrayAfterInitLifecycleListener) testInstance;
            lifecycleListener.afterInit(application);
        }

        application.start(false);

        if (testInstance instanceof StingrayAfterStartLifecycleListener) {
            StingrayAfterStartLifecycleListener lifecycleListener = (StingrayAfterStartLifecycleListener) testInstance;
            lifecycleListener.afterStart(application);
        }

        application.postInit();

        if (testInstance instanceof StingrayAfterPostInitLifecycleListener) {
            StingrayAfterPostInitLifecycleListener lifecycleListener = (StingrayAfterPostInitLifecycleListener) testInstance;
            lifecycleListener.afterPostInit(application);
        }

        int boundPort = application.getBoundPort();
        clientByAlias.put(providerAlias, DefaultStingrayTestClient.newClient("http", "localhost", boundPort));

        return application;
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        for (Map.Entry<String, StingrayApplication> entry : applicationByAlias.entrySet()) {
            StingrayApplication application = entry.getValue();
            application.stop();
        }
    }
}
