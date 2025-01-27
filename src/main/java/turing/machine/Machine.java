package turing.machine;

import jdk.internal.icu.impl.BMPSet;

import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyMap;

public class Machine {
    private final Band band = new Band();
    private Map<String, Map<Character, Transition>> transitions;
    private String state;
    private Set<String> finalStates;

    public void setup(Configuration config) {
        transitions = config.transitions;
        finalStates = config.finalStates;
        band.reset(config.word);
        state = config.startState;
    }

    public Transition proceed() {
        var transition = transitions.getOrDefault(state, emptyMap()).get(band.read());
        if (transition != null) {
            band.write(transition.writeChar(), transition.moveDir());
            state = transition.toState();
        }
        return transition;
    }

    public boolean isInFinalState() {
        return finalStates.contains(state);
    }

    public String readWord() {
        // TODO
        return null;
    }
}
