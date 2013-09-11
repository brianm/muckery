package org.skife.muckery.shrinkwrap;

import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import static org.assertj.core.api.Assertions.assertThat;

public class ResolverTest
{
    @Test
    public void testFoo() throws Exception
    {
        File[] files = Maven.resolver()
                            .resolve("org.apache.activemq:apollo-cli:1.6")
                            .withTransitivity()
                            .asFile();

        URL[] urls = new URL[files.length];
        for (int i = files.length - 1; i >= 0; i--) {
            urls[i] = files[i].toURI().toURL();
            System.out.println(files[i].getAbsolutePath());
        }

        URLClassLoader cl = new URLClassLoader(urls);
        Class<?> clz = cl.loadClass("org.apache.activemq.apollo.cli.commands.Run");
        assertThat(clz).isNotNull();
    }
}
