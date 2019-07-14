package io.sonata.lang.cli;

import io.sonata.lang.cli.command.Compile;
import io.sonata.lang.cli.command.REPL;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParserException;

public class Bootstrap {
    public static void main(String[] args) throws ArgumentParserException {
        var parser = ArgumentParsers.newFor("snc").build();
        parser.addArgument("-i", "--interactive").type(Boolean.class);
        parser.addArgument("input").nargs("*");
        parser.addArgument("-o", "--output");

        var namespace = parser.parseArgs(args);

        if (namespace.getBoolean("interactive")) {
            REPL.execute();
        } else {
            Compile.execute(namespace.getList("input"), namespace.getString("output"));
        }
    }
}
