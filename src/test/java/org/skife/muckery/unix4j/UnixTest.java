package org.skife.muckery.unix4j;

import org.junit.Ignore;
import org.junit.Test;
import org.unix4j.Unix4j;
import org.unix4j.unix.Find;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class UnixTest
{
    @Test
    @Ignore
    public void testFoo() throws Exception
    {
        String out = Unix4j.fromStrings("hello world",
                                        "hello frog",
                                        "bye dinner")
                           .grep("hello")
                           .sed("s/o/O/")
                           .xargs()
                           .toStringResult();

        assertThat(out).isEqualTo("hellO wOrld hellO frOg");
    }
}
