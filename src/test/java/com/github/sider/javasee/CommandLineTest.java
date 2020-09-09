package com.github.sider.javasee;

import com.github.sider.javasee.command.CLICommand;
import com.github.sider.javasee.command.CheckCommand;
import com.github.sider.javasee.command.FindCommand;
import com.github.sider.javasee.command.HelpCommand;
import com.github.sider.javasee.command.InitCommand;
import com.github.sider.javasee.command.TestCommand;
import com.github.sider.javasee.command.VersionCommand;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class CommandLineTest {
    @Test
    public void testNoSubcommand() throws Exception {
        var stdout = new StringPrintStream();
        var stderr = new StringPrintStream();

        var main = new Main(stdout.getStream(), stderr.getStream(), "javasee");
        CLICommand command = main.parse(new String[] {}).get();

        assertTrue(command instanceof HelpCommand);
        assertEquals("", stdout.getString());
    }

    @Test
    public void testVersionCommand() throws Exception {
        var stdout = new StringPrintStream();
        var stderr = new StringPrintStream();

        var main = new Main(stdout.getStream(), stderr.getStream(), "javasee");
        Optional<CLICommand> command = main.parse(new String[] { "version" });

        assertTrue(command.get() instanceof VersionCommand);
    }

    @Test
    public void testVersionCommandWithUnexpectedOption() throws Exception {
        var stdout = new StringPrintStream();
        var stderr = new StringPrintStream();

        var main = new Main(stdout.getStream(), stderr.getStream(), "javasee");
        assertEquals(Optional.empty(), main.parse(new String[] { "version", "--help" }));

        assertEquals("Usage: javasee version\n", stdout.getString());
    }

    @Test
    public void testInitCommand() throws Exception {
        var stdout = new StringPrintStream();
        var stderr = new StringPrintStream();

        var main = new Main(stdout.getStream(), stderr.getStream(), "javasee");
        InitCommand command = (InitCommand)main.parse(new String[] { "init" }).get();

        assertEquals(Paths.get("javasee.yml"), command.configPath);
    }

    @Test
    public void testInitCommandWithOption() throws Exception {
        var stdout = new StringPrintStream();
        var stderr = new StringPrintStream();

        var main = new Main(stdout.getStream(), stderr.getStream(), "javasee");
        InitCommand command = (InitCommand)main.parse(new String[] { "init", "--config=foo.yml" }).get();

        assertEquals(Paths.get("foo.yml"), command.configPath);
    }

    @Test
    public void testInitCommandWithUnexpectedOption() throws Exception {
        var stdout = new StringPrintStream();
        var stderr = new StringPrintStream();

        var main = new Main(stdout.getStream(), stderr.getStream(), "javasee");
        assertEquals(Optional.empty(), main.parse(new String[] { "init", "--help" }));

        assertEquals(
                "Usage: javasee init [-config (--config) <path>]\n" +
                " -config (--config) <path> : Configuration path (default: javasee.yml)\n",
                stdout.getString()
        );
    }

    @Test
    public void testFindCommand() throws Exception {
        var stdout = new StringPrintStream();
        var stderr = new StringPrintStream();

        var main = new Main(stdout.getStream(), stderr.getStream(), "javasee");
        FindCommand command = (FindCommand)main.parse(new String[] { "find", "Array", "src" }).get();

        assertEquals("Array", command.optionPattern);
        assertEquals(List.of("src"), command.optionPaths);
    }

    @Test
    public void testFindCommandWithUnexpectedOption() throws Exception {
        var stdout = new StringPrintStream();
        var stderr = new StringPrintStream();

        var main = new Main(stdout.getStream(), stderr.getStream(), "javasee");
        assertEquals(Optional.empty(), main.parse(new String[] { "find", "-help" }));

        assertEquals("Usage: javasee find <pattern> [<path> ...]\n" +
                " <pattern> : ast pattern in <path> ...\n" +
                " <path>    : paths\n", stdout.getString());
    }

    @Test
    public void testTestCommand() throws Exception {
        var stdout = new StringPrintStream();
        var stderr = new StringPrintStream();

        var main = new Main(stdout.getStream(), stderr.getStream(), "javasee");
        TestCommand command = (TestCommand)main.parse(new String[] { "test" }).get();

        assertEquals("javasee.yml", command.optionConfig);
    }

    @Test
    public void testTestCommandWithOption() throws Exception {
        var stdout = new StringPrintStream();
        var stderr = new StringPrintStream();

        var main = new Main(stdout.getStream(), stderr.getStream(), "javasee");
        TestCommand command = (TestCommand)main.parse(new String[] { "test", "-config", "foo.yml" }).get();

        assertEquals("foo.yml", command.optionConfig);
    }

    @Test
    public void testTestCommandWithUnexpectedOption() throws Exception {
        var stdout = new StringPrintStream();
        var stderr = new StringPrintStream();

        var main = new Main(stdout.getStream(), stderr.getStream(), "javasee");
        assertEquals(Optional.empty(), main.parse(new String[] { "test", "-help" }));

        assertEquals("Usage: javasee test [-config (--config) <config>]\n" +
                " -config (--config) <config> : config YAML file (default: javasee.yml)\n", stdout.getString());
    }

    @Test
    public void testCheckCommand() throws Exception {
        var stdout = new StringPrintStream();
        var stderr = new StringPrintStream();

        var main = new Main(stdout.getStream(), stderr.getStream(), "javasee");
        CheckCommand command = (CheckCommand)main.parse(new String[] { "check" }).get();

        assertEquals("javasee.yml", command.optionConfig);
        assertEquals("text", command.optionFormat);
        assertNull(command.optionRoot);
        assertEquals(List.of(), command.paths);
    }

    @Test
    public void testCheckCommandWithOption() throws Exception {
        var stdout = new StringPrintStream();
        var stderr = new StringPrintStream();

        var main = new Main(stdout.getStream(), stderr.getStream(), "javasee");
        CheckCommand command = (CheckCommand)main.parse(new String[] { "check", "-config", "foo.yml", "-format", "json", "-root", "src", "src" }).get();

        assertEquals("foo.yml", command.optionConfig);
        assertEquals("json", command.optionFormat);
        assertEquals("src", command.optionRoot);
        assertEquals(List.of("src"), command.paths);
    }

    @Test
    public void testCheckCommandWithUnexpectedOption() throws Exception {
        var stdout = new StringPrintStream();
        var stderr = new StringPrintStream();

        var main = new Main(stdout.getStream(), stderr.getStream(), "javasee");
        assertEquals(Optional.empty(), main.parse(new String[] { "check", "-help" }));

        assertEquals("Usage: javasee check [VAL ...] [-config (--config) <config>] [-format (--format) <format>] [-root (--root) <root>]\n" +
                " -config (--config) <config> : config YAML file (default: javasee.yml)\n" +
                " -format (--format) <format> : output format (default: text)\n" +
                " -root (--root) <root>       : root directory\n", stdout.getString());
    }

    @Test
    public void testHelpCommand() throws Exception {
        var stdout = new StringPrintStream();
        var stderr = new StringPrintStream();

        var main = new Main(stdout.getStream(), stderr.getStream(), "javasee");
        HelpCommand command = (HelpCommand)main.parse(new String[] { "help" }).get();

        assertEquals("", stdout.getString());

        assertEquals(JavaSee.ExitStatus.OK, command.start(stdout.getStream(), stderr.getStream()));

        assertEquals("Usage: javasee <command>\n" +
                "  Where command is one of:\n" +
                "    init\n" +
                "    check\n" +
                "    find\n" +
                "    test\n" +
                "    version\n" +
                "    help\n" +
                "  Status code:\n" +
                "    OK (0): The program has terminated successfully\n" +
                "    ERROR (1): The program has encountered some errors\n" +
                "    FAILURE (2): The program has detected some issues in your Java programs\n" +
                "    CONFIG_FILE_NOT_FOUND (3): Config file (.yaml) is not found\n" +
                "    CONFIG_FILE_SYNTAX_ERROR (4): Config file (.yaml) has syntax errors\n" +
                "    CONFIG_FILE_SCHEMA_ERROR (5): Config file (.yaml) has schema errors\n" +
                "    CONFIG_FILE_UNKNOWN_ERROR (6): Config file (.yaml) has unknown errors\n", stdout.getString());
    }
}
