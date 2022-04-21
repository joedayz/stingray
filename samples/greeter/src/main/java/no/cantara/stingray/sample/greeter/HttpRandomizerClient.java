package no.cantara.stingray.sample.greeter;

import no.cantara.stingray.httpclient.StingrayHttpClient;
import no.cantara.stingray.httpclient.StingrayHttpClientFactory;
import no.cantara.stingray.httpclient.StingrayHttpClients;

import java.time.Duration;

public class HttpRandomizerClient implements RandomizerClient {

    private final StingrayHttpClient httpClient;

    public HttpRandomizerClient(String baseUrl) {
        StingrayHttpClientFactory factory = StingrayHttpClients.factory();
        httpClient = factory.newClient()
                .useTarget(targetBuilder -> targetBuilder
                        .withUrl(baseUrl)
                        .build())
                .useConfiguration(configurationBuilder -> configurationBuilder
                        .connectTimeout(Duration.ofSeconds(5))
                        .socketTimeout(Duration.ofSeconds(10))
                        .build())
                .build();
    }

    @Override
    public String getRandomString(String token, int maxLength) {
        String result = httpClient.get()
                .path("/str/" + maxLength)
                .authorizationBearer(token)
                .execute()
                .hasStatusCode(200)
                .contentAsString();
        return result;
    }

    @Override
    public int getRandomInteger(String token, int upperBoundEx) {
        String resultAsString = httpClient.get()
                .path("/int/" + upperBoundEx)
                .authorizationBearer(token)
                .execute()
                .hasStatusCode(200)
                .contentAsString();
        int result = Integer.parseInt(resultAsString);
        return result;
    }

    @Override
    public void reseed(String token, long seed) {
        httpClient.put()
                .path("/seed/" + seed)
                .authorizationBearer(token)
                .execute()
                .hasStatusCode(200);
    }
}
