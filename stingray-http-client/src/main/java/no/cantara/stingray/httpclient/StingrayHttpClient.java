package no.cantara.stingray.httpclient;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;

public interface StingrayHttpClient {

    RequestBuilder get();

    RequestBuilder post();

    RequestBuilder put();

    RequestBuilder options();

    RequestBuilder head();

    RequestBuilder delete();

    RequestBuilder patch();

    RequestBuilder trace();

    interface RequestBuilder {

        RequestBuilder connectTimeout(int connectTimeoutMs);

        RequestBuilder socketTimeout(int socketTimeoutMs);

        default RequestBuilder authorization(String authorization) {
            return setHeader("Authorization", authorization);
        }

        default RequestBuilder authorizationBearer(String token) {
            return authorization("Bearer " + token);
        }

        RequestBuilder path(String path);

        RequestBuilder query(String key, String value);

        RequestBuilder setHeader(String key, String value);

        RequestBuilder bodyJson(String json);

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

        StingrayHttpResponse execute();
    }
}
