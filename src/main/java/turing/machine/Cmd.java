package turing.machine;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import static java.lang.String.format;
import static java.lang.System.err;
import static java.lang.System.exit;
import static java.lang.System.out;
import static java.nio.charset.StandardCharsets.UTF_8;
import static turing.machine.Parser.parseConfiguration;
import static turing.machine.Validator.validateConfiguration;

public class Cmd {
    private static final Charset DEFAULT_CHARSET = UTF_8;
    private static final String DEFAULT_OUT = "./out.log";
    private static final Logger LOG = Logger.getLogger(Cmd.class.getName());

    public static void main(String... args) {
        out.println("Turing Machine");
        try {
            run(fromArgs(args));
        } catch (InstantiationException e) {
            logError(e);
            usage(1);
        } catch (Exception e) {
            logError(e);
            exit(1);
        }
    }

    private static void run(Configuration configuration) {
        validateConfiguration(configuration);
        var machine = new Machine();
        machine.setup(configuration);
        int iteration = 0;
        while (!machine.isInFinalState()) {
            iteration++;
            var transition = machine.proceed();
            if (machine.isInFinalState()) {
                LOG.info("Machine finished in accepting state");
                LOG.info(format("Input word: %s, Computed word: %s", configuration.word, machine.readWord()));
                exit(0);
            }
            if (transition == null) {
                LOG.warning("Machine finished in non-accepting state");
                exit(2);
            }
        }
    }

    private static Configuration fromArgs(String... args) throws InstantiationException, IOException {
        Charset charset = DEFAULT_CHARSET;
        Path path = null;
        String out = DEFAULT_OUT;
        for (var i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-h", "--help" -> usage(0);
                case "-c", "--charset" -> charset = Charset.forName(args[++i]);
                case "-o", "--out" -> out = args[++i];
                default -> path = Path.of(args[i]);
            }
        }
        if (path == null) throw new InstantiationException("Path  is null");
        setLogFile(Path.of(out));
        return parseConfiguration(path, charset);
    }

    private static void setLogFile(Path out) throws IOException {
        LOG.addHandler(new FileHandler(out.toAbsolutePath().toString()));
    }

    private static void usage(int status) {
        out.println("Usage: turing-machine [-h] [-c <charset>] path/to/input.file");
        out.printf("%3s %s%n", "", "path/to/input.file - Path to input file with machine configuration.");
        out.println("Options:");
        out.printf("%6s, %-18s %s%n", "-c", "--charset", format("Optional. Input file encoding. Default: %s.", DEFAULT_CHARSET));
        out.printf("%6s, %-18s %s%n", "-o", "--out", format("Optional. Path to output file. Default: %s.", DEFAULT_OUT));
        out.printf("%6s, %-18s %s%n", "-h", "--help", "Prints help.");
        exit(status);
    }

    private static void logError(Exception e) {
        err.printf("%s: %s%n", e.getClass().getSimpleName(), e.getMessage());
    }
}