package org.skife.muckery;

import io.airlift.command.Cli;
import io.airlift.command.Help;

import java.util.concurrent.Callable;

public class Main
{
public static void main(String[] args) throws Exception
{
    Cli.<Callable>builder("muckery")
       .withCommands(Help.class, JLine2Play.class)
       .withDefaultCommand(Help.class)
       .build()
       .parse(args)
       .call();

}
}
