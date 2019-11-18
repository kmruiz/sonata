package io.sonata.lang.cli;

import io.sonata.lang.cli.command.Compile;
import net.sourceforge.argparse4j.ArgumentParsers;

public class Bootstrap {
    public static void main(String[] args) throws Exception {
        var parser = ArgumentParsers.newFor("snc").build();
        parser.addArgument("input").nargs("*");
        parser.addArgument("-o", "--output");

        var namespace = parser.parseArgs(args);

        Compile.execute(namespace.getList("input"), namespace.getString("output"));
    }
}
