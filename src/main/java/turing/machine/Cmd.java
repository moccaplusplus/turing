package turing.machine;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
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
        PrintStream log
) {
    private static final int REPORT_ITERATIONS_UNTIL = 5000;
    private static final Charset DEFAULT_CHARSET = UTF_8;
    private static final String DEFAULT_OUT = "./out.log";

    public static void main(String... args) {
        out.println("Turing Machine");
        try {
            parseArgs(args).run();
        } catch (Exception e) {
            err.printf("%s: %s%n", e.getClass().getSimpleName(), e.getMessage());
            if (e instanceof InstantiationException) {
                usage();
            }
            exit(1);
        }
    }

    private static Cmd parseArgs(String[] args) throws InstantiationException, IOException {
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
            throw new InstantiationException("Input path  is null");
        }
        var inputPath = Path.of(input);
        var outputPath = Path.of(output);
        var settings = Settings.readFromFile(inputPath, charset);
        settings.validate();
        return new Cmd(settings, logger(outputPath));
    }

    private static void usage() {
        out.println("Usage: turing-machine [-h] [-c <charset>] path/to/input.file");
        out.printf("%3s %-22s %s%n", "", "path/to/input.file", "Path to input file with settings.");
        out.println("Options:");
        out.printf("%6s, %-18s %s%n", "-c", "--charset", format("Optional. Input file encoding. Default: %s.", DEFAULT_CHARSET));
        out.printf("%6s, %-18s %s%n", "-o", "--out", format("Optional. Path to output file. Default: %s.", DEFAULT_OUT));
        out.printf("%6s, %-18s %s%n", "-h", "--help", "Prints help.");
    }

    private static PrintStream logger(Path path) throws IOException {
        return new PrintStream(new OutputStream() {
            private final OutputStream fileStream = Files.newOutputStream(path);

            @Override
            public void write(int b) throws IOException {
                out.write(b);
                fileStream.write(b);
            }

            @Override
            public void close() throws IOException {
                fileStream.close();
            }
        });
    }

    void run() {
        settings.validate();
        log.println(settings);

        var machine = new Machine(settings.startState(), settings.finalStates(), settings.transitions());
        machine.init(settings.word());

        log.println(machine.band);
        int iteration = 0;
        while (!machine.isInFinalState()) {
            iteration++;
            var transition = machine.proceed();
            if (iteration <= REPORT_ITERATIONS_UNTIL) {
                log.printf("Iteration %d, processed transition: %s%n", iteration, transition);
                log.println(machine.band);
            }
            if (machine.isInFinalState()) {
                log.println("Machine finished in accepting state");
                log.printf("Input word: %s, Computed word: %s%n", settings.word(), machine.band.currentWord());
                exit(0);
            }
            if (transition == null) {
                log.println("Machine finished in non-accepting state");
                exit(2);
            }
        }
    }
}