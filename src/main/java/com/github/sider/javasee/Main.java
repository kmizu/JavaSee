package com.github.sider.javasee;

import com.github.sider.javasee.command.*;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    private PrintStream out;
    private PrintStream err;
    private String commandName;

    public Main(PrintStream out, PrintStream err, String commandName) {
        this.out = out;
        this.err = err;
        this.commandName = commandName;
    }

    public CLICommand parse(String[] args) {
        ArrayList<CLICommand> commands = new ArrayList<>();

        HelpCommand help = new HelpCommand(commands, commandName);
        commands.add(new InitCommand());
        commands.add(new CheckCommand());
        commands.add(new FindCommand());
        commands.add(new TestCommand());
        commands.add(new VersionCommand());
        commands.add(help);

        String name = args.length > 0 ? args[0] : null;
        String[] rest = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[] {};

        CLICommand command;
        if (name != null) {
            command = commands.stream().filter(c -> c.getName().equals(name)).findAny().orElse(help);
        } else {
            command = help;
        }

        try {
            CmdLineParser parser = new CmdLineParser(command);
            parser.parseArgument(rest);
            return command;
        } catch (CmdLineException e) {
            printCommandUsage(e.getParser(), command);
            return null;
        }
    }

    private void printCommandUsage(CmdLineParser parser, CLICommand command) {
        out.print(String.format("Usage: %s %s", commandName, command.getName()));
        parser.printSingleLineUsage(out);
        out.println();
        parser.printUsage(out);
    }

    public static void main(String[] args) {
        try {
            var command = new Main(System.out, System.err, JavaSee.getCommandLineName()).parse(args);
            if (command != null) {
                var status = command.start(System.out, System.err);
                System.exit(status.getInt());
            } else {
                System.exit(JavaSee.ExitStatus.ERROR.getInt());
            }
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
