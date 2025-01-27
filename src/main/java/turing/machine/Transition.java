package turing.machine;

public record Transition(
//        String fromState,
//        char readChar,
        String toState,
        char writeChar,
        String moveDir
) {
}
