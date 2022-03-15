package no.cantara.stingray.httpclient;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DefaultStingrayHttpClient implements StingrayHttpClient {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultStingrayHttpClient.class);

    public static final int CONNECT_TIMEOUT_MS = 3000;
    public static final int SOCKET_TIMEOUT_MS = 10000;

    private final String scheme;
    private final String host;
    private final int port;
    private final String basePath;
    private final Map<String, StingrayHttpHeader> defaultHeaderByKey;

    private DefaultStingrayHttpClient(String scheme, String host, int port, String basePath, Map<String, StingrayHttpHeader> defaultHeaderByKey) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.basePath = basePath;
        this.defaultHeaderByKey = defaultHeaderByKey;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String scheme;
        private String host;
        private int port;
        private String basePath = "";
        private final Map<String, StingrayHttpHeader> defaultHeaderByKey = new LinkedHashMap<>();

        private Builder() {
        }

        public Builder withScheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        public Builder withHost(String host) {
            this.host = host;
            return this;
        }

        public Builder withPort(int port) {
            this.port = port;
            return this;
        }

        public Builder withBasePath(String basePath) {
            this.basePath = basePath == null ? "" : basePath;
            return this;
        }

        public Builder withBaseUri(String baseUrl) {
            URI uri = URI.create(baseUrl);
            return withScheme(uri.getScheme())
                    .withHost(uri.getHost())
                    .withPort(uri.getPort())
                    .withBasePath(basePath);
        }

        public Builder withDefaultHeader(String name, String value) {
            defaultHeaderByKey.put(name, new DefaultStingrayHttpHeader(name, Collections.singletonList(value)));
            return this;
        }

        public DefaultStingrayHttpClient build() {
            return new DefaultStingrayHttpClient(scheme, host, port, basePath, defaultHeaderByKey);
        }
    }

    @Override
    public DefaultRequestBuilder get() {
        return new DefaultRequestBuilder(HttpMethod.GET);
    }

    @Override
    public DefaultRequestBuilder post() {
        return new DefaultRequestBuilder(HttpMethod.POST);
    }

    @Override
    public DefaultRequestBuilder put() {
        return new DefaultRequestBuilder(HttpMethod.PUT);
    }

    @Override
    public DefaultRequestBuilder options() {
        return new DefaultRequestBuilder(HttpMethod.OPTIONS);
    }

    @Override
    public DefaultRequestBuilder head() {
        return new DefaultRequestBuilder(HttpMethod.HEAD);
    }

    @Override
    public DefaultRequestBuilder delete() {
        return new DefaultRequestBuilder(HttpMethod.DELETE);
    }

    @Override
    public DefaultRequestBuilder patch() {
        return new DefaultRequestBuilder(HttpMethod.PATCH);
    }

    @Override
    public DefaultRequestBuilder trace() {
        return new DefaultRequestBuilder(HttpMethod.TRACE);
    }

    private enum HttpMethod {
        GET, POST, PUT, OPTIONS, HEAD, DELETE, PATCH, TRACE;
    }

    private class DefaultRequestBuilder implements RequestBuilder {
        private HttpMethod method;
        private String path;
        private HttpEntity entity;
        private Map<String, StingrayHttpHeader> headers = new LinkedHashMap<>(defaultHeaderByKey);
        private List<NameValuePair> queryParams = new LinkedList<>();
        private int connectTimeout = CONNECT_TIMEOUT_MS;
        private int socketTimeout = SOCKET_TIMEOUT_MS;

        private DefaultRequestBuilder(HttpMethod method) {
            this.method = method;
        }

        @Override
        public DefaultRequestBuilder connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        @Override
        public DefaultRequestBuilder socketTimeout(int socketTimeout) {
            this.socketTimeout = socketTimeout;
            return this;
        }

        @Override
        public DefaultRequestBuilder path(String path) {
            this.path = path;
            return this;
        }

        @Override
        public DefaultRequestBuilder query(String key, String value) {
            queryParams.add(new BasicNameValuePair(key, value));
            return this;
        }

        @Override
        public DefaultRequestBuilder setHeader(String name, String value) {
            headers.put(name, new DefaultStingrayHttpHeader(name, Collections.singletonList(value)));
            return this;
        }

        @Override
        public DefaultRequestBuilder bodyJson(String body) {
            entity = new StringEntity(body, ContentType.APPLICATION_JSON);
            return this;
        }

        @Override
        public DefaultRequestBuilder body(String body) {
            entity = new StringEntity(body, ContentType.create("text/plain", StandardCharsets.UTF_8));
            return this;
        }

        @Override
        public DefaultRequestBuilder body(String body, String mimeType) {
            entity = new StringEntity(body, ContentType.create(mimeType));
            return this;
        }

        @Override
        public DefaultRequestBuilder body(String body, String mimeType, Charset charset) {
            entity = new StringEntity(body, ContentType.create(mimeType, charset));
            return this;
        }

        @Override
        public DefaultRequestBuilder bodyJson(InputStream body) {
            entity = new InputStreamEntity(body, ContentType.APPLICATION_JSON);
            return this;
        }

        @Override
        public DefaultRequestBuilder body(InputStream body) {
            entity = new InputStreamEntity(body, ContentType.APPLICATION_OCTET_STREAM);
            return this;
        }

        @Override
        public DefaultRequestBuilder body(InputStream body, String mimeType) {
            entity = new InputStreamEntity(body, ContentType.create(mimeType));
            return this;
        }

        @Override
        public DefaultRequestBuilder body(InputStream body, String mimeType, Charset charset) {
            entity = new InputStreamEntity(body, ContentType.create(mimeType, charset));
            return this;
        }

        @Override
        public DefaultRequestBuilder bodyJson(byte[] body) {
            entity = new ByteArrayEntity(body, ContentType.APPLICATION_JSON);
            return this;
        }

        @Override
        public DefaultRequestBuilder body(byte[] body) {
            entity = new ByteArrayEntity(body, ContentType.APPLICATION_OCTET_STREAM);
            return this;
        }

        @Override
        public DefaultRequestBuilder body(byte[] body, String mimeType) {
            entity = new ByteArrayEntity(body, ContentType.create(mimeType));
            return this;
        }

        @Override
        public DefaultRequestBuilder body(byte[] body, String mimeType, Charset charset) {
            entity = new ByteArrayEntity(body, ContentType.create(mimeType, charset));
            return this;
        }

        @Override
        public DefaultRequestBuilder bodyJson(File body) {
            entity = new FileEntity(body, ContentType.APPLICATION_JSON);
            return this;
        }

        @Override
        public DefaultRequestBuilder body(File body) {
            entity = new FileEntity(body);
            return this;
        }

        @Override
        public DefaultRequestBuilder body(File body, String mimeType) {
            entity = new FileEntity(body, ContentType.create(mimeType));
            return this;
        }

        @Override
        public DefaultRequestBuilder body(File body, String mimeType, Charset charset) {
            entity = new FileEntity(body, ContentType.create(mimeType, charset));
            return this;
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
            public DefaultRequestBuilder endForm() {
                entity = new UrlEncodedFormEntity(pairs, charset);
                return DefaultRequestBuilder.this;
            }
        }

        URI toUri(String pathAndQuery) {
            return URI.create(scheme + "://" + host + ":" + port + basePath + pathAndQuery);
        }

        @Override
        public StingrayHttpResponse execute() {
            try {
                String queryString = URLEncodedUtils.format(queryParams, StandardCharsets.UTF_8);
                String pathAndQuery = path + (queryString.isEmpty() ? "" : (path.contains("?") ? "&" : "?") + queryString);
                URI uri = toUri(pathAndQuery);
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
                request.connectTimeout(connectTimeout);
                request.socketTimeout(socketTimeout);
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
                return new DefaultStingrayHttpResponse(uri.toString(), headers, entity, response);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
