package turing.machine;

import static java.lang.String.format;

public record Transition(
        String fromState,
        char readChar,
        String toState,
        char writeChar,
        String moveDir
) {
    private static final String FORMAT = "%-6s".repeat(5);
    private static final String NEXT_FORMAT = "%n" + FORMAT;
    private static final String HEAD = format(FORMAT, "From", "To", "Read", "Write", "Move");

    public static String toString(Iterable<Transition> transitions) {
        StringBuilder sb = new StringBuilder(HEAD);
        for (Transition t : transitions) {
            sb.append(format(NEXT_FORMAT, t.fromState(), t.toState(), t.readChar(), t.writeChar(), t.moveDir()));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return HEAD + format(NEXT_FORMAT, fromState(), toState(), readChar(), writeChar(), moveDir());
    }
}
