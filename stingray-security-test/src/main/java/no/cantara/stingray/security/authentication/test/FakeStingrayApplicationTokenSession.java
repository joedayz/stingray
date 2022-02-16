package no.cantara.stingray.security.authentication.test;

import no.cantara.stingray.security.authentication.StingrayApplicationTokenSession;

public class FakeStingrayApplicationTokenSession implements StingrayApplicationTokenSession {

    final String applicationId;

    public FakeStingrayApplicationTokenSession(String applicationId) {
        this.applicationId = applicationId;
    }

    @Override
    public String getApplicationToken() {
        return "fake-application-id: " + applicationId;
    }
}
