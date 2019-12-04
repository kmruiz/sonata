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

//    public static void main(String[] args) throws Exception {
//        final String line = "console.log('Hello World!')";
//        final StringBuilder builder = new StringBuilder(500000);
//        for (int i = 0; i < 1000000; i++) {
//            builder.append(line).append('\n');
//        }
//
//        Path path = Paths.get("big-file.sn");
//        System.out.println("Printing");
//        Files.write(path, builder.toString().getBytes());
//    }
}
