package no.cantara.stingray.httpclient;

import java.io.InputStream;
import java.util.List;

interface StingrayHttpResponse {

    StingrayHttpResponse isSuccessful() throws StingrayHttpClientException;

    StingrayHttpResponse hasStatusCode(int status) throws StingrayHttpClientException;

    int status();

    String statusReasonPhrase();

    String protocolVersion();

    String firstHeader(String name);

    List<String> header(String name);

    List<String> headerNames();

    String contentAsString();

    InputStream content();

    /**
     * Tells the length of the content, if known.
     *
     * @return the number of bytes of the content, or
     * a negative number if unknown. If the content length is known
     * but exceeds {@link Long#MAX_VALUE Long.MAX_VALUE},
     * a negative number is returned.
     */
    long contentLength();

    /**
     * Obtains the Content-Type header, if known.
     * This is the header that should be used when sending the entity,
     * or the one that was received with the entity. It can include a
     * charset attribute.
     *
     * @return the Content-Type header for this entity, or
     * {@code null} if the content type is unknown
     */
    String contentType();

    /**
     * Obtains the Content-Encoding header, if known.
     * This is the header that should be used when sending the entity,
     * or the one that was received with the entity.
     * Wrapping entities that modify the content encoding should
     * adjust this header accordingly.
     *
     * @return the Content-Encoding header for this entity, or
     * {@code null} if the content encoding is unknown
     */
    String contentEncoding();
}
