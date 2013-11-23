package org.skife.muckery.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigTest
{
    @Test
    public void testOverrideFallback() throws Exception
    {
        Config c = ConfigFactory.parseFile(new File("src/test/resources/config/example.conf"))
                                .withFallback(ConfigFactory.load("config/fallback.conf"));

        assertThat(c.getStringList("foo")).containsExactly("hello", "world");
    }


    @Test
    public void testNullIfMissing() throws Exception
    {
        Config c = ConfigFactory.parseFile(new File("src/test/resources/config/example.conf"))
                                .withFallback(ConfigFactory.load("config/fallback.conf"));

        String val = c.hasPath("bar") ? c.getString("bar") : null;
        assertThat(val).isNull();
    }


}
