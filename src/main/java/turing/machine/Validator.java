package turing.machine;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

public class Validator {
    public static void validateConfiguration(Configuration configuration) {
        if (!isValidWord(configuration.inputAlphabet, configuration.word)) {
            throw new IllegalStateException("Input word is not matching input alphabet");
        }
        if (!configuration.states.contains(configuration.startState)) {
            throw new IllegalStateException("");
        }
        if (!configuration.states.containsAll(configuration.finalStates)) {
            throw new IllegalStateException("");
        }
        configuration.transitions.values().stream()
                .map(Map::values)
                .flatMap(Collection::stream)
    }

    private static boolean isValidWord(Set<Character> inputAlphabet, String word) {
        return inputAlphabet.containsAll(word.chars().mapToObj(char.class::cast).collect(toSet()));
    }
}
