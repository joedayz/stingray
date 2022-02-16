package no.cantara.stingray.security.authentication;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class DefaultStingrayApplicationAuthentication implements StingrayApplicationAuthentication {

    private final String applicationId;
    private final String forwardingToken;
    private final Supplier<List<StingrayApplicationTag>> tagsSupplier;
    private final AtomicReference<List<StingrayApplicationTag>> tagsRef = new AtomicReference<>();
    private final String accessGroupTagName;

    public DefaultStingrayApplicationAuthentication(String applicationId, String forwardingToken, Supplier<List<StingrayApplicationTag>> tagsSupplier, String accessGroupTagName) {
        this.applicationId = applicationId;
        this.forwardingToken = forwardingToken;
        this.tagsSupplier = tagsSupplier;
        this.accessGroupTagName = accessGroupTagName;
    }

    @Override
    public String ssoId() {
        return applicationId;
    }

    @Override
    public Instant expires() {
        return null;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DefaultStingrayApplicationAuthentication.class.getSimpleName() + "[", "]")
                .add("applicationId='" + applicationId + "'")
                .toString();
    }

    @Override
    public String forwardingToken() {
        return forwardingToken;
    }

    @Override
    public List<String> groups() {
        List<String> groups = new ArrayList<>();
        tags().stream()
                .filter(tag -> accessGroupTagName.equalsIgnoreCase(tag.getName()))
                .findFirst()
                .ifPresent(groupTag -> {
                    String[] parts = groupTag.getValue().split("[ ,:;]+");
                    for (String part : parts) {
                        groups.add(part.trim());
                    }
                });
        return groups;
    }

    @Override
    public List<StingrayApplicationTag> tags() {
        List<StingrayApplicationTag> tags = tagsRef.get();
        if (tags == null) {
            tags = tagsSupplier.get();
            tagsRef.set(tags);
        }
        return tags;
    }
}
