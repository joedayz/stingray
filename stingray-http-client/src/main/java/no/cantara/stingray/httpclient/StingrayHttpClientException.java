package no.cantara.stingray.httpclient;

import java.util.Map;

public class StingrayHttpClientException extends RuntimeException {

    // request
    private final String url;
    private final Map<String, StingrayHttpHeader> requestHeaders;
    private final StingrayRequestBody requestBody;

    //response
    private final int statusCode;
    private final Map<String, StingrayHttpHeader> responseHeaders;
    private final String responseBody;

    private StingrayHttpClientException(String url, Map<String, StingrayHttpHeader> requestHeaders, StingrayRequestBody requestBody, int statusCode, Map<String, StingrayHttpHeader> responseHeaders, String responseBody) {
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
        return requestBody.asString();
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

    @Override
    public String toString() {
        return "StingrayHttpClientException{" +
                "url='" + getUrl() + '\'' +
                ", requestHeaders=" + getRequestHeaders() +
                ", requestBody=" + getRequestBody() +
                ", statusCode=" + getStatusCode() +
                ", responseHeaders=" + getResponseHeaders() +
                ", responseBody='" + getResponseBody() + '\'' +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        // request
        private String url;
        private Map<String, StingrayHttpHeader> requestHeaders;
        private StingrayRequestBody requestBody;

        //response
        private int statusCode;
        private Map<String, StingrayHttpHeader> responseHeaders;
        private String responseBody;

        private Builder() {
        }

        public Builder withUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder withRequestHeaders(Map<String, StingrayHttpHeader> requestHeaders) {
            this.requestHeaders = requestHeaders;
            return this;
        }

        public Builder withRequestBody(StingrayRequestBody requestBody) {
            this.requestBody = requestBody;
            return this;
        }

        public Builder withStatusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder withResponseHeaders(Map<String, StingrayHttpHeader> responseHeaders) {
            this.responseHeaders = responseHeaders;
            return this;
        }

        public Builder withResponseBody(String responseBody) {
            this.responseBody = responseBody;
            return this;
        }

        public StingrayHttpClientException build() {
            return new StingrayHttpClientException(url, requestHeaders, requestBody, statusCode, responseHeaders, responseBody);
        }
    }
}
