package turing.machine;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static turing.machine.Utils.indent;

public class Machine {
    private final Band band = new Band();
    private final Map<String, Map<Character, Transition>> transitions;
    private final String startState;
    private final Set<String> finalStates;
    private String state;

    public Machine(String startState, Set<String> finalStates, Collection<Transition> transitions) {
        this.startState = startState;
        this.finalStates = finalStates;
        this.transitions = transitions.stream()
                .collect(groupingBy(Transition::fromState, toMap(Transition::readChar, identity())));
    }

    public void init(String word) {
        state = startState;
        band.reset(word);
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

    public Band band() {
        return band;
    }

    @Override
    public String toString() {
        return "State: " + state + "\nBand:\n" + indent(band);
    }
}
