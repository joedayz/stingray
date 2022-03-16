package no.cantara.stingray.httpclient;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

public interface StingrayHttpClientConfiguration {

    Duration getConnectTimeout();

    Duration getSocketTimeout();

    URI getBaseUri();

    Map<String, StingrayHttpHeader> getDefaultHeaders();
}
