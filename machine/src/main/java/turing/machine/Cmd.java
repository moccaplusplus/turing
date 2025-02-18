package turing.machine;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.String.format;
import static java.lang.System.err;
import static java.lang.System.exit;
import static java.lang.System.out;
import static java.nio.charset.StandardCharsets.UTF_8;
import static turing.machine.Msg.msg;

public class Cmd {
    public static final int REPORT_ITERATIONS_UNTIL = 5000;
    public static final Charset DEFAULT_CHARSET = UTF_8;
    public static final String DEFAULT_OUT = "out.log";

    public static void main(String... args) {
        try {
            runWithArgs(args);
        } catch (Exception e) {
            err.println(msg(e));
            if (e instanceof InstantiationException) {
                usage();
            }
            exit(1);
        }
    }

    private static void usage() {
        out.println("Usage: turing-machine [-h] [-c <charset>] [-o path/to/out.log] path/to/input.file");
        out.printf("%3s %-22s %s%n", "", "path/to/input.file", "Path to input file with settings.");
        out.println("Options:");
        out.printf("%6s, %-18s %s%n", "-c", "--charset", format("Optional. Input file encoding. Default: %s.", DEFAULT_CHARSET));
        out.printf("%6s, %-18s %s%n", "-o", "--out", format("Optional. Path to output file. Default: %s.", DEFAULT_OUT));
        out.printf("%6s, %-18s %s%n", "-h", "--help", "Prints help.");
    }

    private static void runWithArgs(String... args) throws InstantiationException, IOException {
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
        runWithParams(charset, Path.of(input), Path.of(output));
    }

    private static void runWithParams(Charset charset, Path input, Path output) throws IOException {
        var settings = Settings.parse(input, charset);
        settings.validate();
        try (var fileWriter = new PrintWriter(Files.newOutputStream(output))) {
            run(settings, fileWriter);
        }
    }

    private static void run(Settings settings, PrintWriter fileWriter) {
        var log = new Log(out::print, fileWriter::print);
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
            log.iteration(iteration, transition, machine);

            if (iteration == REPORT_ITERATIONS_UNTIL) {
                log.abandoned(iteration);
                log.clearAppender();
                fileWriter.close();
            }
        }

        long time = System.currentTimeMillis() - start;
        log.result(machine, settings.word(), iteration, time);
    }
}