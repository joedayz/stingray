package no.cantara.stingray.httpclient;

import no.cantara.stingray.httpclient.functionalinterfaces.StingrayHttpExceptionalStreamSupplier;
import no.cantara.stingray.httpclient.functionalinterfaces.StingrayHttpExceptionalStringSupplier;

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

        RequestBuilder bodyJson(StingrayHttpExceptionalStringSupplier jsonSupplier) throws StingrayHttpClientException;

        RequestBuilder body(String body);

        RequestBuilder body(StingrayHttpExceptionalStringSupplier jsonSupplier) throws StingrayHttpClientException;

        RequestBuilder body(String body, String mimeType);

        RequestBuilder body(StingrayHttpExceptionalStringSupplier jsonSupplier, String mimeType) throws StingrayHttpClientException;

        RequestBuilder body(String body, String mimeType, Charset charset);

        RequestBuilder body(StingrayHttpExceptionalStringSupplier jsonSupplier, String mimeType, Charset charset) throws StingrayHttpClientException;

        RequestBuilder bodyJson(InputStream body);

        RequestBuilder bodyJson(StingrayHttpExceptionalStreamSupplier jsonSupplier) throws StingrayHttpClientException;

        RequestBuilder body(InputStream body);

        RequestBuilder body(StingrayHttpExceptionalStreamSupplier jsonSupplier) throws StingrayHttpClientException;

        RequestBuilder body(InputStream body, String mimeType);

        RequestBuilder body(StingrayHttpExceptionalStreamSupplier jsonSupplier, String mimeType) throws StingrayHttpClientException;

        RequestBuilder body(InputStream body, String mimeType, Charset charset);

        RequestBuilder body(StingrayHttpExceptionalStreamSupplier jsonSupplier, String mimeType, Charset charset) throws StingrayHttpClientException;

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
