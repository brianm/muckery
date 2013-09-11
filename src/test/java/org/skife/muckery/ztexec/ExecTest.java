package org.skife.muckery.ztexec;

import org.junit.Test;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.listener.ProcessListener;

import java.util.concurrent.TimeUnit;

public class ExecTest
{
    @Test
    public void testFoo() throws Exception
    {
        StartedProcess p = new ProcessExecutor().command("ls", "/tmp/")
                                                .readOutput(true)
                                                .exitValues(0)
                                                .addListener(new ProcessListener()
                                                {
                                                    // ...
                                                })
                                                .start();
        String out = p.future().get(1, TimeUnit.SECONDS).outputUTF8();
        System.out.println(out);
    }
}
