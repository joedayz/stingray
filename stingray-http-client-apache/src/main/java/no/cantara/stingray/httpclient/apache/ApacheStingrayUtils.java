package no.cantara.stingray.httpclient.apache;

class ApacheStingrayUtils {

    static String normalizeBasePath(String input) {
        if (input == null) {
            return "";
        }
        String result = input.trim();
        // strip all leading slashes
        while (result.startsWith("/")) {
            result = result.substring(1);
        }
        // strip all trailing slashes
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        result = result.trim();
        if (result.length() > 0) {
            // when base-path is not empty, prefix with one slash
            result = "/" + result;
        }
        return result;
    }
}
