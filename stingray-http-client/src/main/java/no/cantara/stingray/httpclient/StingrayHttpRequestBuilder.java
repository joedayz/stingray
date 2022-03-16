package no.cantara.stingray.httpclient;

import no.cantara.stingray.httpclient.functionalinterfaces.StingrayHttpExceptionalStreamSupplier;
import no.cantara.stingray.httpclient.functionalinterfaces.StingrayHttpExceptionalStringSupplier;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.Duration;

public interface StingrayHttpRequestBuilder {

    StingrayHttpRequestBuilder connectTimeout(Duration duration);

    StingrayHttpRequestBuilder socketTimeout(Duration duration);

    default StingrayHttpRequestBuilder authorization(String authorization) {
        return setHeader("Authorization", authorization);
    }

    default StingrayHttpRequestBuilder authorizationBearer(String token) {
        return authorization("Bearer " + token);
    }

    StingrayHttpRequestBuilder path(String path);

    StingrayHttpRequestBuilder query(String key, String value);

    StingrayHttpRequestBuilder setHeader(String key, String value);

    StingrayHttpRequestBuilder bodyJson(String json);

    StingrayHttpRequestBuilder body(String body);

    StingrayHttpRequestBuilder body(String body, String mimeType);

    StingrayHttpRequestBuilder body(String body, String mimeType, Charset charset);

    StingrayHttpRequestBuilder bodyJson(InputStream body);

    StingrayHttpRequestBuilder body(InputStream body);

    StingrayHttpRequestBuilder body(InputStream body, String mimeType);

    StingrayHttpRequestBuilder body(InputStream body, String mimeType, Charset charset);

    StingrayHttpRequestBuilder bodyJson(byte[] body);

    StingrayHttpRequestBuilder body(byte[] body);

    StingrayHttpRequestBuilder body(byte[] body, String mimeType);

    StingrayHttpRequestBuilder body(byte[] body, String mimeType, Charset charset);

    StingrayHttpRequestBuilder bodyJson(File body);

    StingrayHttpRequestBuilder body(File body);

    StingrayHttpRequestBuilder body(File body, String mimeType);

    StingrayHttpRequestBuilder body(File body, String mimeType, Charset charset);

    StingrayHttpRequestBuilder bodyJson(StingrayHttpExceptionalStringSupplier jsonSupplier) throws StingrayHttpClientException;

    StingrayHttpRequestBuilder body(StingrayHttpExceptionalStringSupplier jsonSupplier) throws StingrayHttpClientException;

    StingrayHttpRequestBuilder body(StingrayHttpExceptionalStringSupplier jsonSupplier, String mimeType) throws StingrayHttpClientException;

    StingrayHttpRequestBuilder body(StingrayHttpExceptionalStringSupplier jsonSupplier, String mimeType, Charset charset) throws StingrayHttpClientException;

    StingrayHttpRequestBuilder bodyJson(StingrayHttpExceptionalStreamSupplier jsonSupplier) throws StingrayHttpClientException;

    StingrayHttpRequestBuilder body(StingrayHttpExceptionalStreamSupplier jsonSupplier) throws StingrayHttpClientException;

    StingrayHttpRequestBuilder body(StingrayHttpExceptionalStreamSupplier jsonSupplier, String mimeType) throws StingrayHttpClientException;

    StingrayHttpRequestBuilder body(StingrayHttpExceptionalStreamSupplier jsonSupplier, String mimeType, Charset charset) throws StingrayHttpClientException;

    FormBuilder bodyForm();

    interface FormBuilder {

        FormBuilder charset(Charset charset);

        FormBuilder put(String name, String value);

        StingrayHttpRequestBuilder endForm();
    }

    StingrayHttpResponse execute();
}
