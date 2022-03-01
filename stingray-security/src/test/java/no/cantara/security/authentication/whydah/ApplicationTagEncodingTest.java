package no.cantara.security.authentication.whydah;

import net.whydah.sso.application.mappers.ApplicationTagMapper;
import net.whydah.sso.application.types.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApplicationTagEncodingTest {

    @Test
    public void checkTagEncodingForAccessGroup() {
        List<Tag> tags = new ArrayList<>();
        Tag tagA = new Tag("TagA", "ValueA");
        tags.add(tagA);
        Tag tagAccessGroups = new Tag("access_groups", "somegroup anothergroup yetagroup");
        tags.add(tagAccessGroups);
        Tag tagB = new Tag("TagB", "ValueB");
        tags.add(tagB);
        String tagString = ApplicationTagMapper.toApplicationTagString(tags);
        //System.out.printf("%s%n", tagString);
        assertEquals(tagString, "TagA_ValueA, access!ugroups_somegroup!sanothergroup!syetagroup, TagB_ValueB");
        List<Tag> deserializedTagList = ApplicationTagMapper.getTagList(tagString);
        assertEquals(tags, deserializedTagList);
    }
}
