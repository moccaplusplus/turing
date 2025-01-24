package turing.machine;

import static java.util.Collections.emptyMap;

public class TuringMachine {
    private final Band band = new Band();
    private final MachineDef machineDef;
    private String state;

    public TuringMachine(MachineDef machineDef) {
        this.machineDef = machineDef;
    }

    public void setup(String word) {
        if (isValidInput(word)) {
            throw new IllegalArgumentException("Input word is not matching input alphabet");
        }
        band.reset(word);
        state = machineDef.startState();
    }

    public TransitionDef proceed() {
        var transition = machineDef.transitions().getOrDefault(state, emptyMap()).get(band.read());
        if (transition == null) {
            throw new IllegalStateException("Machine finished in non-accepting state");
        }
        band.write(transition.writeChar(), transition.moveDir());
        state = transition.toState();
        return transition;
    }

    public boolean isInFinalState() {
        return machineDef.finalStates().contains(state);
    }

    private boolean isValidInput(String word) {
        for (int i = 0; i < word.length(); i++) {
            if (!machineDef.inputAlphabet().contains(word.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
