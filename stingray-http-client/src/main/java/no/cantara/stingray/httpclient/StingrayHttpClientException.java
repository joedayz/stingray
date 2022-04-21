package no.cantara.stingray.httpclient;

import java.util.Map;

public class StingrayHttpClientException extends RuntimeException {

    // request
    private final String method;
    private final String url;
    private final Map<String, StingrayHttpHeader> requestHeaders;
    private final StingrayRequestBody requestBody;

    //response
    private final int statusCode;
    private final Map<String, StingrayHttpHeader> responseHeaders;
    private final String responseBody;

    private StingrayHttpClientException(String message, Throwable cause, String method, String url, Map<String, StingrayHttpHeader> requestHeaders, StingrayRequestBody requestBody, int statusCode, Map<String, StingrayHttpHeader> responseHeaders, String responseBody) {
        super(message, cause);
        this.method = method;
        this.url = url;
        this.requestHeaders = requestHeaders;
        this.requestBody = requestBody;
        this.statusCode = statusCode;
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, StingrayHttpHeader> getRequestHeaders() {
        return requestHeaders;
    }

    public String getRequestBody() {
        if (requestBody == null) {
            return null;
        }
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

        private String message = "";
        private Throwable cause;

        // request
        private String method;
        private String url;
        private Map<String, StingrayHttpHeader> requestHeaders;
        private StingrayRequestBody requestBody;

        //response
        private int statusCode;
        private Map<String, StingrayHttpHeader> responseHeaders;
        private String responseBody;

        private Builder() {
        }

        public Builder withMethod(String method) {
            this.method = method;
            return this;
        }

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder withCause(Throwable cause) {
            this.cause = cause;
            return this;
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
            StringBuilder sb = new StringBuilder();
            sb.append(message);
            sb.append("\nREQUEST:\n");
            if (method != null) {
                sb.append(method);
                sb.append(" ");
            }
            if (url != null) {
                sb.append(url);
            }
            if (method != null || url != null) {
                sb.append("\n");
            }
            if (requestHeaders != null) {
                for (Map.Entry<String, StingrayHttpHeader> entry : requestHeaders.entrySet()) {
                    StingrayHttpHeader header = entry.getValue();
                    for (String value : header.all()) {
                        sb.append(header.name());
                        sb.append(": ");
                        sb.append(value);
                        sb.append("\n");
                    }
                }
            }
            if (requestBody != null) {
                try {
                    String body = requestBody.asString();
                    if (body != null) {
                        sb.append(body);
                        sb.append("\n");
                    }
                } catch (Throwable t) {
                    sb.append("Unable to get request-body. Stingray encountered Exception while attempting to resolve it.\n");
                }
            }
            sb.append("RESPONSE:\n");
            sb.append(statusCode);
            // TODO Get response-status-message, e.g. "OK"
            sb.append("\n");
            if (responseHeaders != null) {
                for (Map.Entry<String, StingrayHttpHeader> entry : responseHeaders.entrySet()) {
                    StingrayHttpHeader header = entry.getValue();
                    for (String value : header.all()) {
                        sb.append(header.name());
                        sb.append(": ");
                        sb.append(value);
                        sb.append("\n");
                    }
                }
            }
            if (responseBody != null) {
                sb.append(responseBody);
                sb.append("\n");
            }
            return new StingrayHttpClientException(sb.toString(), cause, method, url, requestHeaders, requestBody, statusCode, responseHeaders, responseBody);
        }
    }
}
