package io.sonata.lang;

import io.sonata.lang.cli.command.Compile;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

public class Bootstrap {
    public static void main(String[] args) throws Exception {
        ArgumentParser parser = ArgumentParsers.newFor("snc").build();
        parser.addArgument("input").nargs("*");
        parser.addArgument("-o", "--output");

        Namespace namespace = parser.parseArgs(args);

        Compile.execute(namespace.getList("input"), namespace.getString("output"));
    }
}
