package turing.machine;

import java.util.List;

import static java.lang.String.format;

public interface Msg {
    String BAND_FORMAT = "%s%n%s^";
    String MACHINE_FORMAT = "State: %s%nBand:%n%s";
    String SETTINGS_FORMAT = "Band Alphabet: %s%nInput Alphabet: %s%nWord: %s%n" +
            "States: %s%nStart State: %s%nFinal States: %s%n" +
            "Transitions: (Count=%d)%n%s";
    String TRANSITION_FORMAT = "%-6s".repeat(5);
    String TRANSITION_HEAD = format(TRANSITION_FORMAT, "From", "To", "Read", "Write", "Move");
    String TRANSITION_NEXT_FORMAT = "%n" + TRANSITION_FORMAT;

    static String msg(Throwable error) {
        return error.getCause() == null ? error.toString() : format("%s%n%s", error, msg(error.getCause()));
    }

    static String msg(Band band) {
        return format(BAND_FORMAT, band.bandStr(), "-".repeat(band.headPos()));
    }

    static String msg(Machine machine) {
        return format(MACHINE_FORMAT, machine.state(), indent(msg(machine.band())));
    }

    static String msg(Settings settings) {
        return format(SETTINGS_FORMAT, settings.bandAlphabet(), settings.inputAlphabet(), settings.word(),
                settings.states(), settings.startState(), settings.finalStates(), settings.transitions().size(),
                indent(msg(settings.transitions())));
    }

    static String msg(Iterable<Transition> transitions) {
        StringBuilder sb = new StringBuilder(TRANSITION_HEAD);
        for (Transition t : transitions) {
            sb.append(format(TRANSITION_NEXT_FORMAT, t.fromState(), t.toState(), t.readChar(), t.writeChar(), t.moveDir()));
        }
        return sb.toString();
    }

    static String msg(Transition transition) {
        return msg(List.of(transition));
    }

    static String indent(String s) {
        return s.replaceAll("(?m)^", "    ");
    }
}
