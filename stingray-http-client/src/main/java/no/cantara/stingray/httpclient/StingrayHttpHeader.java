package no.cantara.stingray.httpclient;

import java.util.List;

public interface StingrayHttpHeader {

    /**
     * @return name of the header
     */
    String name();

    /**
     * @return the first header value for this header
     */
    String first();

    /**
     * @return a list of all values with the name of this header
     */
    List<String> all();
}
