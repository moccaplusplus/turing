package turing.machine;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static java.lang.String.format;
import static java.lang.System.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.*;

public interface Main {
    Charset DEFAULT_CHARSET = UTF_8;

    static void main(String... args) {
        out.println("Turing Machine");
        try {
            fromArgs(args);
        } catch (InstantiationException e) {
            logError(e);
            usage(1);
        } catch (Exception e) {
            logError(e);
            exit(1);
        }
    }

    static void fromArgs(String... args) throws InstantiationException, IOException {
        Charset charset = DEFAULT_CHARSET;
        for (var i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-h", "--help" -> usage(0);
                case "-c", "--charset" -> charset = Charset.forName(args[++i]);
                default -> throw new InstantiationException("Unknown option: " + args[i]);
            }
        }
        // TODO
        Path path = null;
        var m = parse(path, charset);
    }

    static void usage(int status) {
        out.println("Usage: turing-machine [-h] [-c <charset>]");
        out.println("Options:");
        out.printf("%6s, %-18s %s%n", "-c", "--charset", format("Optional. Input file encoding. Default: %s.", DEFAULT_CHARSET));
        out.printf("%6s, %-18s %s%n", "-h", "--help", "Prints help.");
        exit(status);
    }

    static void logError(Exception e) {
        err.printf("%s: %s%n", e.getClass().getSimpleName(), e.getMessage());
    }

    static TuringMachine parse(Path path, Charset charset) throws IOException {
        var lines = Files.readAllLines(path, charset);
        var machineDef = new MachineDef(
                lines.get(1).trim().chars().mapToObj(char.class::cast).collect(toSet()),
                lines.get(3).trim().chars().mapToObj(char.class::cast).collect(toSet()),
                Arrays.stream(lines.get(7).trim().split("\\s+")).collect(toSet()),
                lines.get(9).trim(),
                Arrays.stream(lines.get(11).trim().split("\\s+")).collect(toSet()),
                lines.stream().skip(13)
                        .map(line -> line.split("\\s+"))
//                        .map(s -> new TransitionDef(s[0], s[1].charAt(0), s[2], s[3].charAt(0), s[4]))
                        .collect(groupingBy(s -> s[0], toMap(s -> s[1].charAt(0), s -> new TransitionDef(s[2], s[3].charAt(0), s[4])))));
        var word = lines.get(5);

        var machine = new TuringMachine(machineDef);
        machine.setup(word);

        return machine;
    }
}