package no.cantara.stingray.security.authentication;

import no.cantara.config.ApplicationProperties;
import no.cantara.stingray.security.authentication.whydah.WhydahStingrayAuthenticationManager;
import no.cantara.stingray.security.authentication.whydah.WhydahStingrayAuthenticationManagerFactory;

public class DefaultStingrayAuthenticationManagerFactory implements StingrayAuthenticationManagerFactory {

    public DefaultStingrayAuthenticationManagerFactory() {
    }

    @Override
    public Class<?> providerClass() {
        return WhydahStingrayAuthenticationManager.class;
    }

    @Override
    public String alias() {
        return "default";
    }

    @Override
    public WhydahStingrayAuthenticationManager create(ApplicationProperties applicationProperties) {
        return new WhydahStingrayAuthenticationManagerFactory().create(applicationProperties);
    }
}
