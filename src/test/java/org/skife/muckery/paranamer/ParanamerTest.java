package org.skife.muckery.paranamer;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

public class ParanamerTest
{
    @Test
    public void testByteCodeReadingParanamer() throws Exception
    {
        Paranamer p = new BytecodeReadingParanamer();

        Method greet = getClass().getMethod("greet", String.class);
        String[] names = p.lookupParameterNames(greet);

        assertThat(names).isEqualTo(new String[]{"name"});
    }


    public String greet(String name)
    {
        return String.format("Hello, %s!", name);
    }
}
