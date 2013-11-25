package org.skife.muckery.jmh;

import com.google.common.collect.Lists;
import io.airlift.command.Arguments;
import io.airlift.command.Command;
import org.openjdk.jmh.Main;

import java.util.List;
import java.util.concurrent.Callable;

@Command(name="jmh")
public class JmhCommand implements Callable<Void>
{
    @Arguments
    public List<String> args = Lists.newArrayList();

    @Override
    public Void call() throws Exception
    {
        Main.main(args.toArray(new String[args.size()]));
        return null;
    }
}
