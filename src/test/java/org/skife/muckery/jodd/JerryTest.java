package org.skife.muckery.jodd;

import jodd.io.NetUtil;
import jodd.jerry.Jerry;
import jodd.jerry.JerryFunction;
import org.junit.Test;

public class JerryTest
{
    @Test
    public void testFoo() throws Exception
    {
        Jerry j = Jerry.jerry(NetUtil.downloadString("http://skife.org/"));
        j.$("#page h1 a").each(new JerryFunction() {
            @Override
            public boolean onNode(final Jerry $this, final int index)
            {
                System.out.println($this.text());
                return true;
            }
        });
    }
}
