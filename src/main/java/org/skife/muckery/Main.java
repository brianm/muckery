package org.skife.muckery;

import io.airlift.airline.Cli;
import io.airlift.airline.Command;
import io.airlift.airline.Help;

import java.util.concurrent.Callable;

@Command(name = "main")
public class Main implements Callable<Void>
{
    public static void main(String[] args) throws Exception
    {
        Cli.<Callable>builder("muckery")
           .withCommands(Help.class, Main.class)
           .withDefaultCommand(Help.class)
           .build()
           .parse(args)
           .call();
    }

    @Override
    public Void call() throws Exception
    {
        System.out.println("NOOOOOO");
        return null;
    }
}
