package no.cantara.stingray.security;

import no.cantara.stingray.application.StingrayApplication;

public class StingraySecurity {

    public static void initSecurity(StingrayApplication application) {
        StingraySecurityInitializationHelper helper = new StingraySecurityInitializationHelper(application);
        helper.initSecurity();
    }
}
