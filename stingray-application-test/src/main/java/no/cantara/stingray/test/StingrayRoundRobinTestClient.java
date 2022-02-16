package no.cantara.stingray.test;

import java.net.URI;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public class StingrayRoundRobinTestClient implements StingrayTestClient {

    private final AtomicInteger nextDelegate = new AtomicInteger();
    private final StingrayTestClient[] delegates;

    private StingrayRoundRobinTestClient(StingrayTestClient[] delegates) {
        this.delegates = delegates;
    }

    public static StingrayRoundRobinTestClient newClient(StingrayTestClient... delegates) {
        return new StingrayRoundRobinTestClient(delegates);
    }

    public static StingrayRoundRobinTestClient newClient(Collection<StingrayTestClient> delegates) {
        return new StingrayRoundRobinTestClient(delegates.toArray(new StingrayTestClient[0]));
    }

    private StingrayTestClient select() {
        int indexChoice = nextDelegate.get();
        while (!nextDelegate.compareAndSet(indexChoice, (indexChoice + 1) % delegates.length)) {
            indexChoice = nextDelegate.get();
        }
        return delegates[indexChoice];
    }

    @Override
    public StingrayTestClient useAuthorization(String authorization) {
        return select().useAuthorization(authorization);
    }

    @Override
    public FakeApplicationAuthorizationBuilder useFakeApplicationAuth() {
        return select().useFakeApplicationAuth();
    }

    public StingrayTestClient useFakeApplicationAuth(String applicationId) {
        return select().useFakeApplicationAuth(applicationId);
    }

    public FakeUserAuthorizationBuilder useFakeUserAuth() {
        return select().useFakeUserAuth();
    }

    public StingrayTestClient useHeader(String header, String value) {
        return select().useHeader(header, value);
    }

    public String getHost() {
        return select().getHost();
    }

    public int getPort() {
        return select().getPort();
    }

    public URI getBaseURI() {
        return select().getBaseURI();
    }

    public RequestBuilder get() {
        return select().get();
    }

    public RequestBuilder post() {
        return select().post();
    }

    public RequestBuilder put() {
        return select().put();
    }

    public RequestBuilder options() {
        return select().options();
    }

    public RequestBuilder head() {
        return select().head();
    }

    public RequestBuilder delete() {
        return select().delete();
    }

    public RequestBuilder patch() {
        return select().patch();
    }

    public RequestBuilder trace() {
        return select().trace();
    }
}
