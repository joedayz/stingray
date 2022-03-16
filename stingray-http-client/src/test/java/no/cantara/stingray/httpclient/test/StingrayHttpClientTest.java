package no.cantara.stingray.httpclient.test;

import no.cantara.stingray.httpclient.StingrayHttpClient;
import no.cantara.stingray.httpclient.StingrayHttpClients;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

public class StingrayHttpClientTest {

    @Test
    public void thatExceptionIsThrowsWhenMissingProviders() {
        try {
            StingrayHttpClient client = StingrayHttpClients.defaultClient();
            fail();
        } catch (RuntimeException e) {
        }
    }
}
