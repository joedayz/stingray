package no.cantara.stingray.httpclient.apache;

import no.cantara.stingray.httpclient.StingrayHttpClient;
import no.cantara.stingray.httpclient.StingrayHttpClients;
import no.cantara.stingray.httpclient.StingrayHttpResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StingrayHttpClientTest {

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
    public void thatBasicEchoWithDefaultsWork() {
        StingrayHttpClient client = StingrayHttpClients.customClient()
                .useConfiguration(config -> config
                        .withScheme("http")
                        .withHost("localhost")
                        .withPort(port)
                        .build())
                .build();

        String responseJson = client.post()
                .bodyJson("{\"prop1\":\"val1\"}")
                .execute()
                .isSuccessful()
                .contentAsString();

        assertEquals("{\"prop1\":\"val1\"}", responseJson);

        System.out.printf("Echo: %n%s%n", responseJson);
    }

    @Test
    public void norwegianCharacters() {
        StingrayHttpClient client = StingrayHttpClients.customClient()
                .useConfiguration(config -> config
                        .withScheme("http")
                        .withHost("localhost")
                        .withPort(port)
                        .build())
                .build();

        StingrayHttpResponse response = client.post()
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
