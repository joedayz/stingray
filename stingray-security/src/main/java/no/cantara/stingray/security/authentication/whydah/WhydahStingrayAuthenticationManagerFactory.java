package no.cantara.stingray.security.authentication.whydah;

import no.cantara.config.ApplicationProperties;
import no.cantara.stingray.security.authentication.StingrayAuthenticationManagerFactory;

public class WhydahStingrayAuthenticationManagerFactory implements StingrayAuthenticationManagerFactory {

    public static final String WHYDAH_AUTH_GROUP_USER_ROLE_NAME_FIX = "whydah_auth_group_user_role_name_fix";
    public static final String WHYDAH_AUTH_GROUP_APPLICATION_TAG_NAME = "whydah_auth_group_application_tag_name";

    public static final String DEFAULT_AUTH_GROUP_APPLICATION_TAG_NAME = "access-groups";
    public static final String DEFAULT_AUTH_GROUP_USER_ROLE_NAME_FIX = "access-groups";

    public WhydahStingrayAuthenticationManagerFactory() {
    }

    @Override
    public Class<?> providerClass() {
        return WhydahStingrayAuthenticationManager.class;
    }

    @Override
    public String alias() {
        return "whydah";
    }

    @Override
    public WhydahStingrayAuthenticationManager create(ApplicationProperties applicationProperties) {
        String oauth2Uri = applicationProperties.get(WhydahStingraySecurityProperties.WHYDAH_OAUTH2_URI);
        StingrayWhydahSession stingrayWhydahSession = StingrayWhydahApplicationSessionConfigurator.from(applicationProperties);
        DefaultStingrayWhydahService whydahService = new DefaultStingrayWhydahService(stingrayWhydahSession.getApplicationSession());
        String whydahAuthGroupUserRoleNameFix = applicationProperties.get(WHYDAH_AUTH_GROUP_USER_ROLE_NAME_FIX, DEFAULT_AUTH_GROUP_USER_ROLE_NAME_FIX);
        String whydahAuthGroupApplicationTagName = applicationProperties.get(WHYDAH_AUTH_GROUP_APPLICATION_TAG_NAME, DEFAULT_AUTH_GROUP_APPLICATION_TAG_NAME);
        return new WhydahStingrayAuthenticationManager(oauth2Uri, stingrayWhydahSession, whydahService, whydahAuthGroupUserRoleNameFix, whydahAuthGroupApplicationTagName);
    }
}
