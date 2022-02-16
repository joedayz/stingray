package no.cantara.stingray.application.health;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import no.cantara.stingray.application.security.StingraySecurityOverride;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;

@Path("/health")
public class StingrayHealthResource {
    private static final Logger log = LoggerFactory.getLogger(StingrayHealthResource.class);

    private final StingrayHealthService healthService;
    private final ObjectMapper mapper = new ObjectMapper();

    public StingrayHealthResource(StingrayHealthService healthService) {
        this.healthService = healthService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @StingraySecurityOverride(logAccess = false) // disable authorization requirement in security-filter and do not trace-log access to this endpoint
    public Response getHealth() {
        try {
            String currentHealthJsonWithoutTimestamp = healthService.getCurrentHealthJson();
            ObjectNode health = (ObjectNode) mapper.readTree(currentHealthJsonWithoutTimestamp);
            long healthComputeTimeMs = healthService.getHealthComputeTimeMs();
            boolean activelyUpdatingCurrentHealth = healthService.isActivelyUpdatingCurrentHealth();
            if (!activelyUpdatingCurrentHealth) {
                health.put("Status", "FAIL");
                health.put("errorMessage", "health-updater-thread is dead.");
            }
            health.put("now", Instant.now().toString());
            health.put("health-compute-time-ms", String.valueOf(healthComputeTimeMs));
            health.put("health-updater-thread-alive", String.valueOf(activelyUpdatingCurrentHealth));
            return Response.status(Response.Status.OK).entity(health).build();
        } catch (Throwable t) {
            log.error("While getting health", t);
            ObjectNode health = mapper.createObjectNode();
            health.put("Status", "FAIL");
            health.put("errorMessage", "While getting health");
            StringWriter strWriter = new StringWriter();
            t.printStackTrace(new PrintWriter(strWriter));
            health.put("errorCause", strWriter.toString());
            return Response.status(Response.Status.OK).entity(health).build();
        }
    }
}

