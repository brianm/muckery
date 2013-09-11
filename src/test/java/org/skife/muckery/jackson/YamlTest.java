package org.skife.muckery.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class YamlTest
{
    @Test
    public void testFoo() throws Exception
    {
        ObjectMapper m = new ObjectMapper(new YAMLFactory());
        Foo foo = m.readValue("things:\n" +
                              "  - hello\n" +
                              "  - world\n",
                              Foo.class);
        assertThat(foo.things).containsExactly("hello", "world");

    }

    public static class Foo
    {
        public List<String> things = Lists.newArrayList();
    }
}
