package no.cantara.stingray.application.security;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import no.cantara.stingray.security.authentication.StingrayAuthentication;
import no.cantara.stingray.security.authentication.StingrayAuthenticationManager;
import no.cantara.stingray.security.authentication.StingrayAuthenticationResult;
import no.cantara.stingray.security.authorization.StingrayAccessManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Function;

public class StingraySecurityFilter implements ContainerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(StingraySecurityFilter.class);

    private final StingrayAuthenticationManager authenticationManager;
    private final StingrayAccessManager accessManager;
    private final Function<ContainerRequestContext, Method> jaxRsEndpointResolver;

    public StingraySecurityFilter(StingrayAuthenticationManager authenticationManager, StingrayAccessManager accessManager, Function<ContainerRequestContext, Method> jaxRsEndpointResolver) {
        Objects.requireNonNull(authenticationManager);
        Objects.requireNonNull(accessManager);
        Objects.requireNonNull(jaxRsEndpointResolver);
        this.authenticationManager = authenticationManager;
        this.accessManager = accessManager;
        this.jaxRsEndpointResolver = jaxRsEndpointResolver;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        Method resourceMethod = jaxRsEndpointResolver.apply(requestContext);
        StingraySecurityOverride securityOverride = resourceMethod.getDeclaredAnnotation(StingraySecurityOverride.class);
        if (securityOverride != null) {
            if (securityOverride.logAccess()) {
                log.trace("Access granted through security-override to: {} /{}", requestContext.getMethod(), requestContext.getUriInfo().getPath());
            }
            return; // access granted, no authentication or access-check needed
        }
        String authorizationHeader = requestContext.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        StingrayAuthenticationResult authenticationResult = authenticationManager.authenticate(authorizationHeader);
        if (!authenticationResult.isValid()) {
            // authentication failed
            log.trace("Access unauthorized (401) due to invalid authorization header to: {} /{}", requestContext.getMethod(), requestContext.getUriInfo().getPath());
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }
        final StingrayAuthentication authentication = authenticationResult.authentication();
        requestContext.setProperty(StingrayAuthentication.class.getName(), authentication);
        StingrayAction secureAction = resourceMethod.getDeclaredAnnotation(StingrayAction.class);
        if (secureAction == null) {
            // forbid access to endpoint without secure-action annotation, i.e. secure-by-default
            log.trace("Access forbidden (403) due to missing @SecureAction annotation on {}.{}() to: {} /{}", resourceMethod.getDeclaringClass().getName(), resourceMethod.getName(), requestContext.getMethod(), requestContext.getUriInfo().getPath());
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
            return;
        }
        String action = secureAction.value();
        boolean hasAccess = accessManager.hasAccess(authentication, action);
        if (!hasAccess) {
            // access not allowed according to rules
            log.trace("Access forbidden (403) because {} '{}' is not allowed to perform action '{}' on on {}.{}() needed to: {} /{}", authentication.isApplication() ? "application" : "user", authentication.ssoId(), action, resourceMethod.getDeclaringClass().getName(), resourceMethod.getName(), requestContext.getMethod(), requestContext.getUriInfo().getPath());
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
            return;
        }
        // access granted
        log.trace("Access allowed for {} '{}' on action '{}' to: {} /{}", authentication.isApplication() ? "application" : "user", authentication.ssoId(), action, requestContext.getMethod(), requestContext.getUriInfo().getPath());
        requestContext.setSecurityContext(new StingraySecurityContext(authentication));
    }
}
