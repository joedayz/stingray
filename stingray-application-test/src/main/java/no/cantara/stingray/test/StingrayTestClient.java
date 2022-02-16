package no.cantara.stingray.test;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;

public interface StingrayTestClient {

    StingrayTestClient useAuthorization(String authorization);

    StingrayTestClient useHeader(String header, String value);

    String getHost();

    int getPort();

    URI getBaseURI();

    RequestBuilder get();

    RequestBuilder post();

    RequestBuilder put();

    RequestBuilder options();

    RequestBuilder head();

    RequestBuilder delete();

    RequestBuilder patch();

    RequestBuilder trace();

    enum HttpMethod {
        GET, POST, PUT, OPTIONS, HEAD, DELETE, PATCH, TRACE;
    }

    interface RequestBuilder {

        RequestBuilder method(HttpMethod method);

        RequestBuilder connectTimeout(int connectTimeout);

        RequestBuilder socketTimeout(int socketTimeout);

        RequestBuilder authorization(String authorization);

        RequestBuilder authorizationBearer(String token);

        RequestBuilder path(String path);

        RequestBuilder query(String key, String value);

        RequestBuilder header(String name, String value);

        RequestBuilder bodyJson(Object body);

        RequestBuilder bodyJson(String body);

        RequestBuilder body(String body);

        RequestBuilder body(String body, String mimeType);

        RequestBuilder body(String body, String mimeType, Charset charset);

        RequestBuilder bodyJson(InputStream body);

        RequestBuilder body(InputStream body);

        RequestBuilder body(InputStream body, String mimeType);

        RequestBuilder body(InputStream body, String mimeType, Charset charset);

        RequestBuilder bodyJson(byte[] body);

        RequestBuilder body(byte[] body);

        RequestBuilder body(byte[] body, String mimeType);

        RequestBuilder body(byte[] body, String mimeType, Charset charset);

        RequestBuilder bodyJson(File body);

        RequestBuilder body(File body);

        RequestBuilder body(File body, String mimeType);

        RequestBuilder body(File body, String mimeType, Charset charset);

        FormBuilder bodyForm();

        interface FormBuilder {

            FormBuilder charset(Charset charset);

            FormBuilder put(String name, String value);

            RequestBuilder endForm();
        }

        StingrayResponseHelper execute();
    }
}
