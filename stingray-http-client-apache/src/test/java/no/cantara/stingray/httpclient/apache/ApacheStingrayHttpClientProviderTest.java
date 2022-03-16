package no.cantara.stingray.httpclient.apache;

import no.cantara.stingray.httpclient.StingrayHttpClient;
import no.cantara.stingray.httpclient.StingrayHttpClientException;
import no.cantara.stingray.httpclient.StingrayHttpClientFactory;
import no.cantara.stingray.httpclient.StingrayHttpClients;
import no.cantara.stingray.httpclient.StingrayHttpResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ApacheStingrayHttpClientProviderTest {

    static int port;
    static Server server;

    @BeforeAll
    public static void setup() throws Exception {
        server = new Server(0);
        server.setHandler(new EchoHandler());
        server.start();
        port = ((ServerConnector) server.getConnectors()[0]).getLocalPort();
    }

    @AfterAll
    public static void teardown() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void clientWithBaseTarget() {
        StingrayHttpClientFactory clientFactory = StingrayHttpClients.factory();
        StingrayHttpClient client = clientFactory.newClient()
                .useTarget(target -> target
                        .withScheme("http")
                        .withHost("localhost")
                        .withPort(port)
                        .build())
                .build();

        String responseJson = client.post()
                .path("/echo")
                .bodyJson("{\"prop1\":\"val1\"}")
                .execute()
                .isSuccessful()
                .contentAsString();

        assertEquals("{\"prop1\":\"val1\"}", responseJson);

        System.out.printf("Echo: %n%s%n", responseJson);
    }

    @Test
    public void clientWithoutBaseTarget() {
        StingrayHttpClientFactory clientFactory = StingrayHttpClients.factory();
        StingrayHttpClient client = clientFactory.newClient()
                .build();

        String responseJson = client.post()
                .path("http://localhost:" + port + "/echo")
                .bodyJson("{\"prop1\":\"val1\"}")
                .execute()
                .isSuccessful()
                .contentAsString();

        assertEquals("{\"prop1\":\"val1\"}", responseJson);

        System.out.printf("Echo: %n%s%n", responseJson);
    }

    @Test
    public void clientWithNonEchoTargetShouldRespondWith400() {
        StingrayHttpClientFactory clientFactory = StingrayHttpClients.factory();
        StingrayHttpClient client = clientFactory.newClient()
                .build();

        client.post()
                .path("http://localhost:" + port + "/bad")
                .bodyJson("{\"prop1\":\"val1\"}")
                .execute()
                .hasStatusCode(400);
    }

    @Test
    public void clientWithNonEchoTargetShouldNotBeSuccessful() {
        StingrayHttpClientFactory clientFactory = StingrayHttpClients.factory();
        StingrayHttpClient client = clientFactory.newClient()
                .build();

        try {
            client.post()
                    .path("http://localhost:" + port + "/bad")
                    .bodyJson("{\"prop1\":\"val1\"}")
                    .execute()
                    .isSuccessful();
            fail();
        } catch (StingrayHttpClientException e) {
        }
    }

    @Test
    public void norwegianCharacters() {
        StingrayHttpClientFactory clientFactory = StingrayHttpClients.factory();
        StingrayHttpClient client = clientFactory.newClient()
                .useTarget(target -> target
                        .withScheme("http")
                        .withHost("localhost")
                        .withPort(port)
                        .build())
                .build();

        StingrayHttpResponse response = client.post()
                .path("/echo")
                .bodyJson("{\"prop1\":\"æøåÆØÅ\"}")
                .execute();
        System.out.printf("RESPONSE HEADERS:%n");
        for (String headerName : response.headerNames()) {
            String headerValue = response.firstHeader(headerName);
            System.out.printf("%s: %s%n", headerName, headerValue);
        }
        System.out.println();
        String responseJson = response
                .isSuccessful()
                .contentAsString();

        assertEquals("{\"prop1\":\"æøåÆØÅ\"}", responseJson);

        System.out.printf("Echo: %n%s%n", responseJson);
    }
}
