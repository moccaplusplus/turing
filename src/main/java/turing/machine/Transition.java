package turing.machine;

public record Transition(
        String fromState,
        char readCharacter,
        String toState,
        char writeCharacter,
        String moveDirection
) {
}
