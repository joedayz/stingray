package no.cantara.stingray.httpclient.apache;

import no.cantara.stingray.httpclient.StingrayHttpClientConfiguration;
import no.cantara.stingray.httpclient.StingrayHttpClientException;
import no.cantara.stingray.httpclient.StingrayHttpHeader;
import no.cantara.stingray.httpclient.StingrayHttpRequestBuilder;
import no.cantara.stingray.httpclient.StingrayHttpResponse;
import no.cantara.stingray.httpclient.functionalinterfaces.StingrayHttpExceptionalStreamSupplier;
import no.cantara.stingray.httpclient.functionalinterfaces.StingrayHttpExceptionalStringSupplier;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;

class ApacheStingrayHttpRequestBuilder implements StingrayHttpRequestBuilder {

    private final ApacheStingrayHttpClient apacheStingrayHttpClient;
    private final HttpMethod method;

    private final Map<String, StingrayHttpHeader> headers = new LinkedHashMap<>();

    private String path = "";
    private HttpEntity entity;
    private List<NameValuePair> queryParams = new LinkedList<>();
    private int connectTimeoutMs;
    private int socketTimeoutMs;

    ApacheStingrayHttpRequestBuilder(ApacheStingrayHttpClient apacheStingrayHttpClient, HttpMethod method) {
        this.apacheStingrayHttpClient = apacheStingrayHttpClient;
        StingrayHttpClientConfiguration configuration = apacheStingrayHttpClient.getConfiguration();
        this.connectTimeoutMs = (int) configuration.getConnectTimeout().toMillis();
        this.socketTimeoutMs = (int) configuration.getSocketTimeout().toMillis();
        this.method = method;
        this.headers.putAll(configuration.getDefaultHeaders());
    }

    @Override
    public ApacheStingrayHttpRequestBuilder connectTimeout(Duration duration) {
        long durationMs = duration.toMillis();
        if (durationMs < 0 || durationMs > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("duration out of range");
        }
        this.connectTimeoutMs = (int) durationMs;
        return this;
    }

    @Override
    public ApacheStingrayHttpRequestBuilder socketTimeout(Duration duration) {
        long durationMs = duration.toMillis();
        if (durationMs < 0 || durationMs > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("duration out of range");
        }
        this.socketTimeoutMs = (int) durationMs;
        return this;
    }

    @Override
    public ApacheStingrayHttpRequestBuilder path(String path) {
        this.path = path;
        return this;
    }

    @Override
    public ApacheStingrayHttpRequestBuilder query(String key, String value) {
        queryParams.add(new BasicNameValuePair(key, value));
        return this;
    }

    @Override
    public ApacheStingrayHttpRequestBuilder setHeader(String name, String value) {
        headers.put(name, new ApacheStingrayHttpHeader(name, value));
        return this;
    }

    @Override
    public ApacheStingrayHttpRequestBuilder bodyJson(String body) {
        setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
        return this;
    }

    @Override
    public ApacheStingrayHttpRequestBuilder body(String body) {
        setEntity(new StringEntity(body, ContentType.create("text/plain", StandardCharsets.UTF_8)));
        return this;
    }

    @Override
    public ApacheStingrayHttpRequestBuilder body(String body, String mimeType) {
        setEntity(new StringEntity(body, ContentType.create(mimeType)));
        return this;
    }

    @Override
    public ApacheStingrayHttpRequestBuilder body(String body, String mimeType, Charset charset) {
        setEntity(new StringEntity(body, ContentType.create(mimeType, charset)));
        return this;
    }

    @Override
    public ApacheStingrayHttpRequestBuilder bodyJson(InputStream body) {
        setEntity(new InputStreamEntity(body, ContentType.APPLICATION_JSON));
        return this;
    }

    @Override
    public ApacheStingrayHttpRequestBuilder body(InputStream body) {
        setEntity(new InputStreamEntity(body, ContentType.APPLICATION_OCTET_STREAM));
        return this;
    }

    @Override
    public ApacheStingrayHttpRequestBuilder body(InputStream body, String mimeType) {
        setEntity(new InputStreamEntity(body, ContentType.create(mimeType)));
        return this;
    }

    @Override
    public ApacheStingrayHttpRequestBuilder body(InputStream body, String mimeType, Charset charset) {
        setEntity(new InputStreamEntity(body, ContentType.create(mimeType, charset)));
        return this;
    }

    @Override
    public ApacheStingrayHttpRequestBuilder bodyJson(byte[] body) {
        setEntity(new ByteArrayEntity(body, ContentType.APPLICATION_JSON));
        return this;
    }

    @Override
    public ApacheStingrayHttpRequestBuilder body(byte[] body) {
        setEntity(new ByteArrayEntity(body, ContentType.APPLICATION_OCTET_STREAM));
        return this;
    }

    @Override
    public ApacheStingrayHttpRequestBuilder body(byte[] body, String mimeType) {
        setEntity(new ByteArrayEntity(body, ContentType.create(mimeType)));
        return this;
    }

    @Override
    public ApacheStingrayHttpRequestBuilder body(byte[] body, String mimeType, Charset charset) {
        setEntity(new ByteArrayEntity(body, ContentType.create(mimeType, charset)));
        return this;
    }

    @Override
    public ApacheStingrayHttpRequestBuilder bodyJson(File body) {
        setEntity(new FileEntity(body, ContentType.APPLICATION_JSON));
        return this;
    }

    @Override
    public ApacheStingrayHttpRequestBuilder body(File body) {
        setEntity(new FileEntity(body));
        return this;
    }

    @Override
    public ApacheStingrayHttpRequestBuilder body(File body, String mimeType) {
        setEntity(new FileEntity(body, ContentType.create(mimeType)));
        return this;
    }

    @Override
    public ApacheStingrayHttpRequestBuilder body(File body, String mimeType, Charset charset) {
        setEntity(new FileEntity(body, ContentType.create(mimeType, charset)));
        return this;
    }

    private void setEntity(HttpEntity entity) {
        this.entity = entity;
        /*
        // TODO Resolve these headers for debug-purposes only on exception and/or state dump
        if (entity != null) {
            Header contentType = entity.getContentType();
            if (contentType != null) {
                headers.put(contentType.getName(), new ApacheStingrayHttpHeader(contentType.getName(), contentType.getValue()));
            }
            Header contentEncoding = entity.getContentEncoding();
            if (contentEncoding != null) {
                headers.put(contentEncoding.getName(), new ApacheStingrayHttpHeader(contentEncoding.getName(), contentEncoding.getValue()));
            }
            long contentLength = entity.getContentLength();
            if (contentLength >= 0) {
                headers.put(HttpHeaders.CONTENT_LENGTH, new ApacheStingrayHttpHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength)));
            }
        }
        */
    }

    @Override
    public ApacheStingrayHttpRequestBuilder bodyJson(StingrayHttpExceptionalStringSupplier jsonSupplier) throws StingrayHttpClientException {
        try {
            return bodyJson(jsonSupplier.get());
        } catch (RuntimeException e) { //JsonProcessingException?
            throw StingrayHttpClientException.builder()
                    .withMessage("While attempting to resolve request body")
                    .withCause(e)
                    .withMethod(method.name())
                    .withUrl(toUri().toString())
                    .withRequestHeaders(headers)
                    .build();
        }
    }

    @Override
    public ApacheStingrayHttpRequestBuilder body(StingrayHttpExceptionalStringSupplier jsonSupplier) throws StingrayHttpClientException {
        try {
            String body = jsonSupplier.get();
            return body(body);
        } catch (RuntimeException e) { //JsonProcessingException?
            throw StingrayHttpClientException.builder()
                    .withCause(e)
                    .withMessage("While resolving request body supplier")
                    .withMethod(method.name())
                    .withUrl(toUri().toString())
                    .withRequestHeaders(headers)
                    .build();
        }
    }

    @Override
    public ApacheStingrayHttpRequestBuilder body(StingrayHttpExceptionalStringSupplier jsonSupplier, String mimeType) throws StingrayHttpClientException {
        try {
            return body(jsonSupplier.get(), mimeType);
        } catch (RuntimeException e) { //JsonProcessingException?
            throw StingrayHttpClientException.builder()
                    .withCause(e)
                    .withMethod(method.name())
                    .withUrl(toUri().toString())
                    .withRequestHeaders(headers)
                    .build();
        }
    }

    @Override
    public ApacheStingrayHttpRequestBuilder body(StingrayHttpExceptionalStringSupplier jsonSupplier, String mimeType, Charset charset) throws StingrayHttpClientException {
        try {
            return body(jsonSupplier.get(), mimeType, charset);
        } catch (RuntimeException e) { //JsonProcessingException?
            throw StingrayHttpClientException.builder()
                    .withCause(e)
                    .withMethod(method.name())
                    .withUrl(toUri().toString())
                    .withRequestHeaders(headers)
                    .build();
        }
    }

    @Override
    public ApacheStingrayHttpRequestBuilder bodyJson(StingrayHttpExceptionalStreamSupplier jsonSupplier) throws StingrayHttpClientException {
        try {
            return bodyJson(jsonSupplier.get());
        } catch (RuntimeException e) { //JsonProcessingException?
            throw StingrayHttpClientException.builder()
                    .withCause(e)
                    .withMethod(method.name())
                    .withUrl(toUri().toString())
                    .withRequestHeaders(headers)
                    .build();
        }
    }


    @Override
    public ApacheStingrayHttpRequestBuilder body(StingrayHttpExceptionalStreamSupplier jsonSupplier) throws StingrayHttpClientException {
        try {
            return body(jsonSupplier.get());
        } catch (RuntimeException e) { //JsonProcessingException?
            throw StingrayHttpClientException.builder()
                    .withCause(e)
                    .withMethod(method.name())
                    .withUrl(toUri().toString())
                    .withRequestHeaders(headers)
                    .build();
        }
    }

    @Override
    public ApacheStingrayHttpRequestBuilder body(StingrayHttpExceptionalStreamSupplier jsonSupplier, String mimeType) throws StingrayHttpClientException {
        try {
            return body(jsonSupplier.get(), mimeType);
        } catch (RuntimeException e) { //JsonProcessingException?
            throw StingrayHttpClientException.builder()
                    .withCause(e)
                    .withMethod(method.name())
                    .withUrl(toUri().toString())
                    .withRequestHeaders(headers)
                    .build();
        }
    }

    @Override
    public ApacheStingrayHttpRequestBuilder body(StingrayHttpExceptionalStreamSupplier jsonSupplier, String mimeType, Charset charset) throws StingrayHttpClientException {
        try {
            return body(jsonSupplier.get(), mimeType, charset);
        } catch (RuntimeException e) { //JsonProcessingException?
            throw StingrayHttpClientException.builder()
                    .withCause(e)
                    .withMethod(method.name())
                    .withUrl(toUri().toString())
                    .withRequestHeaders(headers)
                    .build();
        }
    }

    @Override
    public DefaultFormBuilder bodyForm() {
        return new DefaultFormBuilder();
    }

    public class DefaultFormBuilder implements FormBuilder {

        Charset charset = StandardCharsets.UTF_8;
        final List<NameValuePair> pairs = new ArrayList<>();

        @Override
        public DefaultFormBuilder charset(Charset charset) {
            this.charset = charset;
            return this;
        }

        @Override
        public DefaultFormBuilder put(String name, String value) {
            pairs.add(new BasicNameValuePair(name, value));
            return this;
        }

        @Override
        public ApacheStingrayHttpRequestBuilder endForm() {
            entity = new UrlEncodedFormEntity(pairs, charset);
            return ApacheStingrayHttpRequestBuilder.this;
        }
    }

    URI toUri() {
        String queryString = URLEncodedUtils.format(queryParams, StandardCharsets.UTF_8);
        String pathAndQuery = path + (queryString.isEmpty() ? "" : (path.contains("?") ? "&" : "?") + queryString);
        String baseUrl = ofNullable(apacheStingrayHttpClient.getTarget())
                .map(ApacheStingrayHttpTarget::getUri)
                .map(URI::toString)
                .orElse("");
        return URI.create(baseUrl + pathAndQuery);
    }

    @Override
    public StingrayHttpResponse execute() {
        try {
            URI uri = toUri();
            // TODO consider fail-fast when target or path is not specified?
            Request request;
            switch (method) {
                case GET:
                    request = Request.Get(uri);
                    break;
                case POST:
                    request = Request.Post(uri);
                    break;
                case PUT:
                    request = Request.Put(uri);
                    break;
                case OPTIONS:
                    request = Request.Options(uri);
                    break;
                case HEAD:
                    request = Request.Head(uri);
                    break;
                case DELETE:
                    request = Request.Delete(uri);
                    break;
                case PATCH:
                    request = Request.Patch(uri);
                    break;
                case TRACE:
                    request = Request.Trace(uri);
                    break;
                default:
                    throw new IllegalArgumentException("HttpMethod not supported: " + method);
            }
            request.connectTimeout(connectTimeoutMs);
            request.socketTimeout(socketTimeoutMs);
            for (Map.Entry<String, StingrayHttpHeader> header : headers.entrySet()) {
                List<String> all = header.getValue().all();
                if (all != null) {
                    for (String headerValue : all) {
                        request.addHeader(header.getKey(), headerValue);
                    }
                }
            }
            if (entity != null) {
                request.body(entity);
            }
            Response response = request.execute();
            return new ApacheStingrayHttpResponse(method.name(), uri.toString(), headers, entity, response);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
