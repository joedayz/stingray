package no.cantara.stingray.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StingrayDefaultTestClient implements StingrayTestClient {

    private static final Logger LOG = LoggerFactory.getLogger(StingrayDefaultTestClient.class);

    // TODO configure with more support (jsr310, etc.) and/or allow client configuration
    private static final ObjectMapper mapper = new ObjectMapper();

    public static final int CONNECT_TIMEOUT_MS = 3000;
    public static final int SOCKET_TIMEOUT_MS = 10000;

    private final Map<String, String> defaultHeaderByKey = new ConcurrentHashMap<>();
    private final String scheme;
    private final String host;
    private final int port;

    private StingrayDefaultTestClient(String scheme, String host, int port) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
    }

    public static StingrayDefaultTestClient newClient(String scheme, String host, int port) {
        return new StingrayDefaultTestClient(scheme, host, port);
    }

    @Override
    public StingrayDefaultTestClient useAuthorization(String authorization) {
        defaultHeaderByKey.put(HttpHeaders.AUTHORIZATION, authorization);
        return this;
    }

    public StingrayDefaultTestClient useHeader(String header, String value) {
        defaultHeaderByKey.put(header, value);
        return this;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public URI getBaseURI() {
        return URI.create(scheme + "://" + host + ":" + port);
    }

    URI toUri(String pathAndQuery) {
        return URI.create(scheme + "://" + host + ":" + port + pathAndQuery);
    }

    public DefaultRequestBuilder get() {
        return new DefaultRequestBuilder().method(HttpMethod.GET);
    }

    public DefaultRequestBuilder post() {
        return new DefaultRequestBuilder().method(HttpMethod.POST);
    }

    public DefaultRequestBuilder put() {
        return new DefaultRequestBuilder().method(HttpMethod.PUT);
    }

    public DefaultRequestBuilder options() {
        return new DefaultRequestBuilder().method(HttpMethod.OPTIONS);
    }

    public DefaultRequestBuilder head() {
        return new DefaultRequestBuilder().method(HttpMethod.HEAD);
    }

    public DefaultRequestBuilder delete() {
        return new DefaultRequestBuilder().method(HttpMethod.DELETE);
    }

    public DefaultRequestBuilder patch() {
        return new DefaultRequestBuilder().method(HttpMethod.PATCH);
    }

    public DefaultRequestBuilder trace() {
        return new DefaultRequestBuilder().method(HttpMethod.TRACE);
    }

    public class DefaultRequestBuilder implements RequestBuilder {
        private HttpMethod method;
        private String path;
        private HttpEntity entity;
        private Map<String, String> headers = new LinkedHashMap<>(defaultHeaderByKey);
        private List<NameValuePair> queryParams = new LinkedList<>();
        private int connectTimeout = CONNECT_TIMEOUT_MS;
        private int socketTimeout = SOCKET_TIMEOUT_MS;

        public DefaultRequestBuilder method(HttpMethod method) {
            this.method = method;
            return this;
        }

        public DefaultRequestBuilder connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public DefaultRequestBuilder socketTimeout(int socketTimeout) {
            this.socketTimeout = socketTimeout;
            return this;
        }

        public DefaultRequestBuilder authorization(String authorization) {
            return header(HttpHeaders.AUTHORIZATION, authorization);
        }

        public DefaultRequestBuilder authorizationBearer(String token) {
            return header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }

        public DefaultRequestBuilder path(String path) {
            this.path = path;
            return this;
        }

        public DefaultRequestBuilder query(String key, String value) {
            queryParams.add(new BasicNameValuePair(key, value));
            return this;
        }

        public DefaultRequestBuilder header(String name, String value) {
            headers.put(name, value);
            return this;
        }

        public DefaultRequestBuilder bodyJson(Object body) {
            String json;
            if (body instanceof String) {
                json = (String) body;
            } else {
                try {
                    json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(body);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
            entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            return this;
        }

        public DefaultRequestBuilder bodyJson(String body) {
            entity = new StringEntity(body, ContentType.APPLICATION_JSON);
            return this;
        }

        public DefaultRequestBuilder body(String body) {
            entity = new StringEntity(body, ContentType.create("text/plain", StandardCharsets.UTF_8));
            return this;
        }

        public DefaultRequestBuilder body(String body, String mimeType) {
            entity = new StringEntity(body, ContentType.create(mimeType));
            return this;
        }

        public DefaultRequestBuilder body(String body, String mimeType, Charset charset) {
            entity = new StringEntity(body, ContentType.create(mimeType, charset));
            return this;
        }

        public DefaultRequestBuilder bodyJson(InputStream body) {
            entity = new InputStreamEntity(body, ContentType.APPLICATION_JSON);
            return this;
        }

        public DefaultRequestBuilder body(InputStream body) {
            entity = new InputStreamEntity(body, ContentType.APPLICATION_OCTET_STREAM);
            return this;
        }

        public DefaultRequestBuilder body(InputStream body, String mimeType) {
            entity = new InputStreamEntity(body, ContentType.create(mimeType));
            return this;
        }

        public DefaultRequestBuilder body(InputStream body, String mimeType, Charset charset) {
            entity = new InputStreamEntity(body, ContentType.create(mimeType, charset));
            return this;
        }

        public DefaultRequestBuilder bodyJson(byte[] body) {
            entity = new ByteArrayEntity(body, ContentType.APPLICATION_JSON);
            return this;
        }

        public DefaultRequestBuilder body(byte[] body) {
            entity = new ByteArrayEntity(body, ContentType.APPLICATION_OCTET_STREAM);
            return this;
        }

        public DefaultRequestBuilder body(byte[] body, String mimeType) {
            entity = new ByteArrayEntity(body, ContentType.create(mimeType));
            return this;
        }

        public DefaultRequestBuilder body(byte[] body, String mimeType, Charset charset) {
            entity = new ByteArrayEntity(body, ContentType.create(mimeType, charset));
            return this;
        }

        public DefaultRequestBuilder bodyJson(File body) {
            entity = new FileEntity(body, ContentType.APPLICATION_JSON);
            return this;
        }

        public DefaultRequestBuilder body(File body) {
            entity = new FileEntity(body);
            return this;
        }

        public DefaultRequestBuilder body(File body, String mimeType) {
            entity = new FileEntity(body, ContentType.create(mimeType));
            return this;
        }

        public DefaultRequestBuilder body(File body, String mimeType, Charset charset) {
            entity = new FileEntity(body, ContentType.create(mimeType, charset));
            return this;
        }

        public DefaultFormBuilder bodyForm() {
            return new DefaultFormBuilder();
        }

        public class DefaultFormBuilder implements FormBuilder {

            Charset charset = StandardCharsets.UTF_8;
            final List<NameValuePair> pairs = new ArrayList<>();

            public DefaultFormBuilder charset(Charset charset) {
                this.charset = charset;
                return this;
            }

            public DefaultFormBuilder put(String name, String value) {
                pairs.add(new BasicNameValuePair(name, value));
                return this;
            }

            public DefaultRequestBuilder endForm() {
                entity = new UrlEncodedFormEntity(pairs, charset);
                return DefaultRequestBuilder.this;
            }
        }

        public StingrayResponseHelper execute() {
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
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    request.addHeader(header.getKey(), header.getValue());
                }
                if (entity != null) {
                    request.body(entity);
                }
                Response response = request
                        .execute();
                return new StingrayResponseHelper(response);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
