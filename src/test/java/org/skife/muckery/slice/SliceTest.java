package org.skife.muckery.slice;

import io.airlift.slice.Slice;
import io.airlift.slice.Slices;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;

public class SliceTest
{
    public static final int GIGABYTE = (int) Math.pow(2, 10);

    @Test
    public void testFoo() throws Exception
    {
        Slice slice = Slice.toUnsafeSlice(ByteBuffer.allocateDirect(128 * GIGABYTE));
        slice.fill((byte) 0);

        slice.setBytes(4, new byte[]{1, 2, 3, 4, 5});
        Slice sub = slice.slice(1, 4);
    }
}
