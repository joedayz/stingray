package no.cantara.stingray.httpclient.apache;

import no.cantara.stingray.httpclient.StingrayHttpTarget;

import java.net.URI;

class ApacheStingrayHttpTarget implements StingrayHttpTarget {

    private final URI uri;

    ApacheStingrayHttpTarget(URI uri) {
        this.uri = uri;
    }

    @Override
    public URI getUri() {
        return uri;
    }
}
