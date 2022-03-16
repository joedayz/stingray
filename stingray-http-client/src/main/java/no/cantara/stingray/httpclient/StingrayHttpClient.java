package no.cantara.stingray.httpclient;

public interface StingrayHttpClient {

    StingrayHttpRequestBuilder get();

    StingrayHttpRequestBuilder post();

    StingrayHttpRequestBuilder put();

    StingrayHttpRequestBuilder options();

    StingrayHttpRequestBuilder head();

    StingrayHttpRequestBuilder delete();

    StingrayHttpRequestBuilder patch();

    StingrayHttpRequestBuilder trace();
}
