package turing.machine;

import java.util.Map;
import java.util.Set;

public record MachineDef(
        Set<Character> bandAlphabet,
        Set<Character> inputAlphabet,
        Set<String> states,
        String startState,
        Set<String> finalStates,
        Map<String, Map<Character, TransitionDef>> transitions
) {
}
