package turing.machine;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;

import static java.lang.String.format;
import static java.util.stream.Collectors.*;

public class Parser {
    private static final Logger LOG = Logger.getLogger(Parser.class.getName());

    public static Configuration parseConfiguration(Path path, Charset charset) throws IOException {
        var configuration = new Configuration();
        try (var reader = Files.newBufferedReader(path, charset)) {
            expectHeader(reader.readLine(), "alfabet tasmowy:");
            configuration.bandAlphabet = reader.readLine().chars().mapToObj(char.class::cast).collect(toSet());

            expectHeader(reader.readLine(), "alfabet wejsciowy:");
            configuration.inputAlphabet = reader.readLine().chars().mapToObj(char.class::cast).collect(toSet());

            expectHeader(reader.readLine(), "slowo wejsciowe:");
            configuration.word = reader.readLine();

            expectHeader(reader.readLine(), "stany:");
            configuration.states = Arrays.stream(reader.readLine().split(" ")).collect(toSet());

            expectHeader(reader.readLine(), "stan poczatkowy:");
            configuration.startState = reader.readLine();

            expectHeader(reader.readLine(), "stany akceptujace:");
            configuration.finalStates = Arrays.stream(reader.readLine().split(" ")).collect(toSet());

            expectHeader(reader.readLine(), "relacja przejscia:");
            configuration.transitions = reader.lines()
                    .map(line -> line.split(" "))
//                        .map(s -> new TransitionDef(s[0], s[1].charAt(0), s[2], s[3].charAt(0), s[4]))
                    .collect(groupingBy(s -> s[0], toMap(s -> s[1].charAt(0), s -> new Transition(s[2], s[3].charAt(0), s[4]))));

        }
        return configuration;
    }

    private static <T> void expectHeader(T expected, T actual) {
        if (!Objects.equals(expected, actual)) {
            LOG.warning(format("Expected header to be \"%s\" but was \"%s\"", expected, actual));
        }
    }
}
