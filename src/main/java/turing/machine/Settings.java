package turing.machine;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.lang.String.format;
import static java.lang.System.err;
import static java.util.stream.Collectors.toSet;
import static turing.machine.Utils.indent;

public record Settings(
        Set<Character> bandAlphabet,
        Set<Character> inputAlphabet,
        String word,
        Set<String> states,
        String startState,
        Set<String> finalStates,
        Set<Transition> transitions
) {
    public static Settings readFromFile(Path path, Charset charset) throws IOException {
        try (var reader = Files.newBufferedReader(path, charset)) {
            expectHeader(reader.readLine(), "alfabet tasmowy:");
            var bandAlphabet = reader.readLine().chars().mapToObj(i -> (char) i).collect(toSet());

            expectHeader(reader.readLine(), "alfabet wejsciowy:");
            var inputAlphabet = reader.readLine().chars().mapToObj(i -> (char) i).collect(toSet());

            expectHeader(reader.readLine(), "slowo wejsciowe:");
            var word = reader.readLine();

            expectHeader(reader.readLine(), "stany:");
            var states = Arrays.stream(reader.readLine().split(" ")).collect(toSet());

            expectHeader(reader.readLine(), "stan poczatkowy:");
            var startState = reader.readLine();

            expectHeader(reader.readLine(), "stany akceptujace:");
            var finalStates = Arrays.stream(reader.readLine().split(" ")).collect(toSet());

            expectHeader(reader.readLine(), "relacja przejscia:");
            var transitions = new HashSet<Transition>();
            for (var line = reader.readLine(); line != null; line = reader.readLine()) {
                var split = line.split(" ");
                transitions.add(new Transition(split[0], split[1].charAt(0), split[2], split[3].charAt(0), split[4]));
            }
            return new Settings(bandAlphabet, inputAlphabet, word, states, startState, finalStates, transitions);
        }
    }

    private static <T> void expectHeader(T actual, T expected) {
        if (!Objects.equals(expected, actual)) {
            err.printf("Expected header to be \"%s\" but was \"%s\"%n", expected, actual);
        }
    }

    private static void validateCharacter(Set<Character> alphabet, char character) {
        if (!alphabet.contains(character)) {
            throw new IllegalStateException(format("Character \"%s\" not present in alphabet %s", character, alphabet));
        }
    }

    private static void validateState(Set<String> states, String state) {
        if (!states.contains(state)) {
            throw new IllegalStateException(format("State: \"%s\" not present in states %s", state, states));
        }
    }

    private static void validateDirection(String direction) {
        switch (direction) {
            case "L", "P": return;
            default: throw new IllegalStateException(format("Move Direction \"%s\" is not valid", direction));
        }
    }

    public void validate() {
        validateWord();
        validateStartState();
        validateFinalStates();
        validateTransitions();
    }

    private void validateWord() {
        try {
            for (char c : word().toCharArray()) {
                validateCharacter(inputAlphabet, c);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Input word is not matching input alphabet", e);
        }
    }

    private void validateStartState() {
        try {
            validateState(states(), startState());
        } catch (Exception e) {
            throw new IllegalStateException("Invalid start state", e);
        }
    }

    private void validateFinalStates() {
        try {
            for (var state : finalStates()) {
                validateState(states(), state);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Invalid final state", e);
        }
    }

    private void validateTransitions() {
        try {
            for (var transition : transitions()) {
                validateCharacter(bandAlphabet(), transition.readChar());
                validateCharacter(bandAlphabet(), transition.writeChar());
                validateState(states(), transition.fromState());
                validateState(states(), transition.toState());
                validateDirection(transition.moveDir());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Invalid transition", e);
        }
    }

    @Override
    public String toString() {
        return "Band Alphabet: " + bandAlphabet() +
                "\nInput Alphabet: " + inputAlphabet() +
                "\nWord: " + word() +
                "\nStates: " + states() +
                "\nStart State: " + startState() +
                "\nFinal States: " + finalStates() +
                "\nTransitions: (Count=" + transitions().size() + ")\n" +
                indent(Transition.toString(transitions()));
    }
}
