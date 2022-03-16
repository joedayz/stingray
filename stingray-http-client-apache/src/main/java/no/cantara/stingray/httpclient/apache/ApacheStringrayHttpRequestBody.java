package no.cantara.stingray.httpclient.apache;

import no.cantara.stingray.httpclient.StingrayRequestBody;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UncheckedIOException;

public class ApacheStringrayHttpRequestBody implements StingrayRequestBody {

    private final HttpEntity entity;

    public ApacheStringrayHttpRequestBody(HttpEntity entity) {
        this.entity = entity;
    }

    @Override
    public String asString() {
        try {
            return EntityUtils.toString(entity);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
