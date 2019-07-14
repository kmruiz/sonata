package io.sonata.lang.cli;

import io.sonata.lang.cli.command.Compile;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParserException;

public class Bootstrap {
    public static void main(String[] args) throws ArgumentParserException {
        var parser = ArgumentParsers.newFor("snc").build();
        parser.addArgument("input").nargs("*");
        parser.addArgument("-o", "--output").required(true);

        var namespace = parser.parseArgs(args);
        new Compile().execute(namespace.getList("input"), namespace.getString("output"));
    }
}
