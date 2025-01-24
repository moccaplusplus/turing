package turing.machine;

public record TransitionDef(
//        String fromState,
//        char readChar,
        String toState,
        char writeChar,
        String moveDir
) {
}
