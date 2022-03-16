package no.cantara.stingray.httpclient;

import java.net.URI;

public interface StingrayHttpTargetBuilder {

    StingrayHttpTargetBuilder withScheme(String scheme);

    StingrayHttpTargetBuilder withHost(String host);

    StingrayHttpTargetBuilder withPort(int port);

    StingrayHttpTargetBuilder withPath(String basePath);

    StingrayHttpTargetBuilder withUrl(String baseUrl);

    StingrayHttpTargetBuilder withUri(URI baseUri);

    StingrayHttpTarget build();
}
