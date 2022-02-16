package no.cantara.stingray.security.authentication.whydah;

import no.cantara.config.ApplicationProperties;

import java.util.Objects;


public class StingrayWhydahApplicationSessionConfigurator {

    public static StingrayWhydahSession from(ApplicationProperties properties) {
        String configuredWhydahBaseUri = properties.get(WhydahStingraySecurityProperties.WHYDAH_URI);
        String configuredWhydahStsUri = properties.get("whydah_uri_sts");
        String configuredWhydahUasUri = properties.get("whydah_uri_uas");
        String normalizedWhydahBaseUri = normalizeBaseUri(configuredWhydahBaseUri);
        String securityTokenServiceUri;
        if (configuredWhydahStsUri != null) {
            securityTokenServiceUri = normalizeBaseUri(configuredWhydahStsUri);
        } else {
            securityTokenServiceUri = normalizedWhydahBaseUri + "tokenservice/";
        }
        String userAdminServiceUri;
        if (configuredWhydahUasUri != null) {
            userAdminServiceUri = normalizeBaseUri(configuredWhydahUasUri);
        } else {
            userAdminServiceUri = normalizedWhydahBaseUri + "useradminservice/";
        }
        String applicationId = properties.get(WhydahStingraySecurityProperties.WHYDAH_APPLICATION_ID);
        String applicationName = properties.get(WhydahStingraySecurityProperties.WHYDAH_APPLICATION_NAME);
        String applicationSecret = properties.get(WhydahStingraySecurityProperties.WHYDAH_APPLICATION_SECRET);
        return new StingrayWhydahSession(securityTokenServiceUri, userAdminServiceUri, applicationId, applicationName, applicationSecret);
    }

    public static String normalizeBaseUri(String baseUri) {
        Objects.requireNonNull(baseUri);
        String normalized = baseUri;
        // trim trailing slashes
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        // add single trailing slash
        if (normalized.length() > 0) {
            normalized = normalized + "/";
        }
        return normalized;
    }
}
