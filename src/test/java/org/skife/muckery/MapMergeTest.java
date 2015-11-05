package org.skife.muckery;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MapMergeTest
{
    @Test
    public void testFoo() throws Exception
    {
        SetMultimap<String, String> first = Multimaps.newSetMultimap(Maps.newHashMap(), Sets::newHashSet);
        first.put("a", "alphabet");
        first.put("b", "bedrock");


        SetMultimap<String, String> second = Multimaps.newSetMultimap(Maps.newHashMap(), Sets::newHashSet);
        second.put("a", "animal");
        second.put("c", "charisma");

        first.putAll(second);

        assertThat(first.get("a")).containsOnly("alphabet", "animal");
        assertThat(first.get("b")).containsOnly("bedrock");
        assertThat(first.get("c")).containsOnly("charisma");
    }
}
