package no.cantara.stingray.httpclient;

import java.time.Duration;
import java.util.Map;

public interface StingrayHttpClientConfiguration {

    Duration getConnectTimeout();

    Duration getSocketTimeout();

    Map<String, StingrayHttpHeader> getDefaultHeaders();
}
