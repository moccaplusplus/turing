package turing.machine;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MachineIntegrationTest {
    @Test
    void shouldRunSimpleMachineFromInputFile() throws IOException {
        // given
        var settings = Settings.parse(getClass().getResourceAsStream("/input.txt"), UTF_8);
        var machine = new Machine(settings.startState(), settings.finalStates(), settings.transitions());

        // when
        machine.init(settings.word());
        assertFalse(machine.isInFinalState());
        assertEquals(settings.startState(), machine.state());
        assertEquals(machine.band().currentWord(), settings.word());

        int transitionCount = 0;
        while (!machine.isInFinalState()) {
            var stateBefore = machine.state();
            var charBefore = machine.band().read();

            var transition = machine.proceed();
            if (transition == null) {
                break;
            }
            assertEquals(transition.fromState(), stateBefore);
            assertEquals(transition.readChar(), charBefore);
            assertEquals(transition.toState(), machine.state());
            assertEquals(
                    transition.writeChar(),
                    machine.band().bandStr().charAt(machine.band().headPos() + ("P".equals(transition.moveDir()) ? -1 : 1)));
            transitionCount++;
        }

        // then
        assertTrue(machine.isInFinalState());
        assertEquals("bb", machine.band().currentWord());
        assertEquals(4, transitionCount);
    }
}