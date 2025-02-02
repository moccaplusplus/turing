package turing.cmd;

import turing.machine.Log;
import turing.machine.Machine;
import turing.machine.Settings;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.String.format;
import static java.lang.System.err;
import static java.lang.System.exit;
import static java.lang.System.out;
import static java.nio.charset.StandardCharsets.UTF_8;

public record Cmd(
        Settings settings,
        Log log
) implements Runnable, AutoCloseable {
    private static final int REPORT_ITERATIONS_UNTIL = 5000;
    private static final Charset DEFAULT_CHARSET = UTF_8;
    private static final String DEFAULT_OUT = "./out.log";

    public static void main(String... args) {
        try (var cmd = fromArgs(args)) {
            cmd.run();
        } catch (Exception e) {
            err.printf("%s: %s%n", e.getClass().getSimpleName(), e.getMessage());
            if (e instanceof InstantiationException) {
                usage();
            }
            exit(1);
        }
    }

    private static Cmd fromArgs(String... args) throws InstantiationException, IOException {
        Charset charset = DEFAULT_CHARSET;
        String input = null;
        String output = DEFAULT_OUT;
        for (var i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-h", "--help" -> {
                    usage();
                    exit(0);
                }
                case "-c", "--charset" -> charset = Charset.forName(args[++i]);
                case "-o", "--out" -> output = args[++i];
                default -> input = args[i];
            }
        }
        if (input == null) {
            throw new InstantiationException("Input path is null");
        }
        var settings = Settings.read(Path.of(input), charset);
        var log = new Log(logOutputStream(Files.newOutputStream(Path.of(output))));
        return new Cmd(settings, log);
    }

    private static void usage() {
        out.println("Usage: turing-machine [-h] [-c <charset>] path/to/input.file");
        out.printf("%3s %-22s %s%n", "", "path/to/input.file", "Path to input file with settings.");
        out.println("Options:");
        out.printf("%6s, %-18s %s%n", "-c", "--charset", format("Optional. Input file encoding. Default: %s.", DEFAULT_CHARSET));
        out.printf("%6s, %-18s %s%n", "-o", "--out", format("Optional. Path to output file. Default: %s.", DEFAULT_OUT));
        out.printf("%6s, %-18s %s%n", "-h", "--help", "Prints help.");
    }

    private static OutputStream logOutputStream(OutputStream fileStream) {
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                out.write(b);
                fileStream.write(b);
            }

            @Override
            public void flush() throws IOException {
                out.flush();
                fileStream.flush();
            }

            @Override
            public void close() throws IOException {
                fileStream.close();
            }
        };
    }

    @Override
    public void run() {
        settings.validate();
        log.settings(settings);

        long start = System.currentTimeMillis();

        var machine = new Machine(settings.startState(), settings.finalStates(), settings.transitions());
        machine.init(settings.word());
        log.initialized(machine);

        int iteration = 0;
        while (!machine.isInFinalState()) {
            var transition = machine.proceed();
            if (transition == null) {
                break;
            }
            iteration++;
            if (iteration <= REPORT_ITERATIONS_UNTIL /*|| iteration % 1000 == 0 */) {
                log.iteration(iteration, transition, machine);
            }
        }

        long time = System.currentTimeMillis() - start;
        log.result(machine, settings.word(), iteration, time);
    }

    @Override
    public void close() {
        log.close();
    }
}