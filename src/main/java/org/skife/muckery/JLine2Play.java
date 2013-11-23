package org.skife.muckery;


import com.google.common.collect.Lists;
import io.airlift.command.Arguments;
import io.airlift.command.Command;
import io.airlift.command.Option;
import jline.console.ConsoleReader;
import jline.console.completer.StringsCompleter;

import java.util.concurrent.Callable;

@Command(name = "jline",
         description = "some jline2 muckery")
public class JLine2Play implements Callable<Void>
{

    @Option(name = {"-p", "--prompt"}, title = "prompt", description = "Prompt for each line")
    public String prompt = "$";

    @Arguments
    public java.util.List<String> args = Lists.newArrayList();

    @Override
    public Void call() throws Exception
    {
        ConsoleReader reader = new ConsoleReader(System.in, System.out);
        reader.setPrompt("$ ");
        reader.addCompleter(new StringsCompleter(args));

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().equalsIgnoreCase("quit")) {
                System.exit(0);
            }
            System.out.printf("=> \"%s\"\n", line);
        }

        return null;
    }
}
