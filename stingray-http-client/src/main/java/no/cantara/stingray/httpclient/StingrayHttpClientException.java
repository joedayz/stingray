package no.cantara.stingray.httpclient;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;

public class StingrayHttpClientException extends RuntimeException {

    // request
    private final String url;
    private final Map<String, StingrayHttpHeader> requestHeaders;
    private final HttpEntity requestBody;

    //response
    private final int statusCode;
    private final Map<String, StingrayHttpHeader> responseHeaders;
    private final String responseBody;

    private StingrayHttpClientException(String url, Map<String, StingrayHttpHeader> requestHeaders, HttpEntity requestBody, int statusCode, Map<String, StingrayHttpHeader> responseHeaders, String responseBody) {
        this.url = url;
        this.requestHeaders = requestHeaders;
        this.requestBody = requestBody;
        this.statusCode = statusCode;
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, StingrayHttpHeader> getRequestHeaders() {
        return requestHeaders;
    }

    public String getRequestBody() {
        String body;
        try {
            body = EntityUtils.toString(requestBody);
        } catch (IOException e) {
            return null;
        }
        return body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, StingrayHttpHeader> getResponseHeaders() {
        return responseHeaders;
    }

    public String getResponseBody() {
        return responseBody;
    }

    static Builder builder() {
        return new Builder();
    }

    static class Builder {

        // request
        private String url;
        private Map<String, StingrayHttpHeader> requestHeaders;
        private HttpEntity requestBody;

        //response
        private int statusCode;
        private Map<String, StingrayHttpHeader> responseHeaders;
        private String responseBody;

        private Builder() {
        }

        Builder withUrl(String url) {
            this.url = url;
            return this;
        }

        Builder withRequestHeaders(Map<String, StingrayHttpHeader> requestHeaders) {
            this.requestHeaders = requestHeaders;
            return this;
        }

        Builder withRequestBody(HttpEntity requestBody) {
            this.requestBody = requestBody;
            return this;
        }

        Builder withStatusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        Builder withResponseHeaders(Map<String, StingrayHttpHeader> responseHeaders) {
            this.responseHeaders = responseHeaders;
            return this;
        }

        Builder withResponseBody(String responseBody) {
            this.responseBody = responseBody;
            return this;
        }

        StingrayHttpClientException build() {
            return new StingrayHttpClientException(url, requestHeaders, requestBody, statusCode, responseHeaders, responseBody);
        }
    }
}
