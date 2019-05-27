package com.github.kmizu.java_see;

import com.github.kmizu.java_see.cli.Formatters;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {

    @Option(name="-config")
    private String config;

    @Option(name="-root")
    private String root;

    @Option(name="-format")
    private String format;

    @Option(name="-rule")
    private String rule;

    @Argument
    private List<String> paths = new ArrayList<>();

    private File configPath() {
        var path = new File(config);
        if(path.isFile()) return path;
        return new File("querly.yaml");
    }

    public String getConfig() {
        return config;
    }

    public String getRoot() {
        return root;
    }

    public String getFormat() {
        return format;
    }

    public String getRule() {
        return rule;
    }

    public final CmdLineParser parser;

    public Main() {
        parser = new CmdLineParser(this);
    }

    private void println(String line) {
        System.err.println(line);
    }

    private void help() {
        println("java -jar JavaSee-all.jar (check|init) ...");
    }

    public void run(String[] args) throws CmdLineException {
        if(args.length == 0) {
            help();
        } else {
            String subCommand = args[0];
            String[] tmpArgs = new String[args.length - 1];
            for(int i = 0; i < tmpArgs.length; i++) {
                tmpArgs[i] = args[i + 1];
            }
            args = tmpArgs;
            this.parser.parseArgument(args);
            switch(subCommand) {
                case "check":
                    check(this.paths);
                    break;
                case "version":
                    version();
                    break;
                case "init":
                    init();
                    break;
            }
        }
    }

    public static void main(String[] args) throws CmdLineException {
        new Main().run(args);
    }

    /**
     * Main paths based on configuration
     * @param paths
     */
    private void check(List<String> paths) {
        if(this.config == null) {
            this.config = "querly.yml";
        }
        if(format == null) {
            this.format = "text";
        }
        Formatters.AbstractFormatter formatter;
        switch(this.format) {
            case "text":
                formatter = new Formatters.TextFormatter();
                break;
            case "json":
                formatter = new Formatters.JSONFormatter();
                break;
            default:
                throw new RuntimeException("cannot reach here");
        }
        formatter.onStart();

        try {
            if(!configPath().isFile()) {
                println("Configuration file " + configPath() + " does not look a file.");
                println("Specify configuration file by -config option");
                return;
            }
            File rootPath;
            if(this.root != null) {
                rootPath = new File(this.root);
            } else {
                rootPath = configPath().getParentFile();
            }
            Map<String, Object> yaml;
            Config config = null;
            try {
                yaml = new Yaml().load(new FileInputStream(configPath()));
                config = Config.load(yaml, configPath(), rootPath);
            } catch (Exception e) {
                e.printStackTrace();
                formatter.onConfigError(this.config, e);
            }
            var analyzer = new Analyzer(config, rule, new ArrayList<>());

            new ScriptEnumerator(paths.isEmpty() ? List.of(new File(".")) : paths.stream().map(p -> new File(p)).collect(Collectors.toList()),  config).forEach((path , script) -> {
                analyzer.scripts.add(script);
                formatter.onScriptLoaded(script);
            });

            analyzer.run((t) -> {
                var script = t._1;
                var rule = t._2;
                var pair = t._3;
                formatter.onIssueFound(script, rule, pair);
            });
        } catch (Exception e) {
            e.printStackTrace();
            formatter.onFatalError(e);
        } finally {
            formatter.onFinish();
        }
    }

    /**
     * This is a subcommand method.
     * Find for the pattern in given paths
     * @param pattern
     * @param paths
     */
    private void find(String pattern, String... paths) {

    }

    /*
    desc "find pattern [paths]", "Find for the pattern in given paths"
    def find(pattern, *paths)
      require 'querly/cli/find'

      Find.new(
        pattern: pattern,
        paths: paths.empty? ? [Pathname.pwd] : paths.map {|path| Pathname(path) },
      ).start
    end
    */

    /**
     * This is a subcommand method.
     * Main configuration
     */
    private void test() {
        config = "querly.yml";
    }
    /*
    desc "test", "Main configuration"
    option :config, default: "querly.yml"
    def test()
      require "querly/cli/test"
      exit Test.new(config_path: config_path).run
    end
    */

    /**
     * This is a subcommand method.
     * Print version
     */
    private void version() {
        System.out.println("JavaSee " + Version.VERSION);
    }


    /**
     * This is a subcommand method.
     * Generate JavaSee config file (java_see.yml)
     */
    private void init() {
        try {
            Files.copy(Paths.get("template.yml"), Paths.get("java_see.yml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
    def self.source_root
      File.join(__dir__, "../..")
    end

    include Thor::Actions

    desc "init", "Generate Querly config file (querly.yml)"
    def init()
      copy_file("template.yml", "querly.yml")
    end

end
            */
}