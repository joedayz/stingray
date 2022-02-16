package no.cantara.stingray.security.authentication.test;

import no.cantara.config.ApplicationProperties;
import no.cantara.stingray.security.authentication.StingrayAuthenticationManagerFactory;

public class FakeStingrayAuthenticationManagerFactory implements StingrayAuthenticationManagerFactory {

    @Override
    public Class<?> providerClass() {
        return FakeStingrayAuthenticationManager.class;
    }

    @Override
    public String alias() {
        return "fake";
    }

    @Override
    public FakeStingrayAuthenticationManager create(ApplicationProperties applicationProperties) {
        String defaultFakeUserId = applicationProperties.get("default-fake-user-id", "fake-user");
        String defaultFakeUsername = applicationProperties.get("default-fake-username", "fake-username");
        String defaultFakeUsertokenId = applicationProperties.get("default-fake-usertoken-id", "fake-usertoken-id");
        String defaultFakeCustomerRef = applicationProperties.get("default-fake-customer-ref", "fake-customer");
        String defaultFakeApplicationId = applicationProperties.get("default-fake-application-id", "fake-application");
        String selfApplicationId = applicationProperties.get("self-application-id", "my-application");
        return new FakeStingrayAuthenticationManager(selfApplicationId, defaultFakeUserId, defaultFakeUsername, defaultFakeUsertokenId, defaultFakeCustomerRef, defaultFakeApplicationId);
    }
}


