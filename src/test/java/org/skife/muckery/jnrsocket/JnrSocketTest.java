package org.skife.muckery.jnrsocket;

import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;
import org.junit.Test;

import java.io.File;
import java.nio.ByteBuffer;

public class JnrSocketTest
{
    @Test
    public void testFoo() throws Exception
    {
        UnixSocketAddress address = new UnixSocketAddress(new File("/dev/log"));
        UnixSocketChannel c = UnixSocketChannel.open(address);
        c.write(ByteBuffer.wrap("hello world".getBytes()));
        c.close();
    }
}
