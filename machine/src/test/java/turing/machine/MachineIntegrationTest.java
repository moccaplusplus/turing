package turing.machine;

import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MachineIntegrationTest {
    @Test
    void shouldRunSimpleMachineFromInputFile() {
        // given
        var settings = Settings.parse(getClass().getResourceAsStream("/przykladowy_input.txt"), UTF_8);
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
                    machine.band().bandStr().charAt(
                            machine.band().headPos() + ("P".equals(transition.moveDir()) ? -1 : 1)));
            transitionCount++;
        }

        // then
        assertTrue(machine.isInFinalState());
        assertEquals("bb", machine.band().currentWord());
        assertEquals(4, transitionCount);
    }

    @Test
    void shouldExpandBandToTheLeft() {
        // given
        var settings = Settings.parse(getClass().getResourceAsStream(
                "/rozszerzanie_tasmy_z_lewej_dodaje_b_na_poczatku_slowa.txt"), UTF_8);
        var machine = new Machine(settings.startState(), settings.finalStates(), settings.transitions());

        // when
        machine.init(settings.word());
        assertFalse(machine.isInFinalState());
        assertEquals(settings.startState(), machine.state());
        assertEquals(machine.band().currentWord(), settings.word());
        assertEquals(32, machine.band().length());

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
                    machine.band().bandStr().charAt(
                            machine.band().headPos() + ("P".equals(transition.moveDir()) ? -1 : 1)));
            transitionCount++;
        }

        // then
        assertTrue(machine.isInFinalState());
        assertEquals("bbbb", machine.band().currentWord());
        assertEquals(2, transitionCount);
        assertEquals(48, machine.band().length());
    }

    @Test
    void shouldEndInNonAcceptingStateAfterFirstIteration() {
        // given
        var settings = Settings.parse(getClass().getResourceAsStream("/blad_po_pierwszym_przejsciu.txt"), UTF_8);
        var machine = new Machine(settings.startState(), settings.finalStates(), settings.transitions());

        // when
        machine.init(settings.word());
        assertFalse(machine.isInFinalState());
        assertEquals(settings.startState(), machine.state());

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
                    machine.band().bandStr().charAt(
                            machine.band().headPos() + ("P".equals(transition.moveDir()) ? -1 : 1)));
            transitionCount++;
        }

        // then
        assertFalse(machine.isInFinalState());
        assertEquals(1, transitionCount);
        assertEquals("bab", machine.band().currentWord());
    }

    @Test
    void shouldMirrorShortInputWord() {
        // given
        var settings = Settings.parse(getClass().getResourceAsStream("/kopia_lustrzana_krotka.txt"), UTF_8);
        var machine = new Machine(settings.startState(), settings.finalStates(), settings.transitions());

        // when
        machine.init(settings.word());
        assertFalse(machine.isInFinalState());
        assertEquals(settings.startState(), machine.state());

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
                    machine.band().bandStr().charAt(
                            machine.band().headPos() + ("P".equals(transition.moveDir()) ? -1 : 1)));
            transitionCount++;
        }

        // then
        assertTrue(machine.isInFinalState());
        assertEquals(settings.word() + new StringBuilder(settings.word()).reverse(), machine.band().currentWord());
        assertEquals(62, transitionCount);
    }

    @Test
    void shouldMirrorLongerInputWordExpandingBandOnTheRightTwice() {
        // given
        var settings = Settings.parse(getClass().getResourceAsStream("/kopia_lustrzana_rozszerzanie_tasmy.txt"), UTF_8);
        var machine = new Machine(settings.startState(), settings.finalStates(), settings.transitions());

        // when
        machine.init(settings.word());
        assertFalse(machine.isInFinalState());
        assertEquals(settings.startState(), machine.state());
        assertEquals(32, machine.band().length());

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
                    machine.band().bandStr().charAt(
                            machine.band().headPos() + ("P".equals(transition.moveDir()) ? -1 : 1)));
            transitionCount++;
        }

        // then
        assertTrue(machine.isInFinalState());
        assertEquals(64, machine.band().length());
        assertEquals(settings.word() + new StringBuilder(settings.word()).reverse(), machine.band().currentWord());
        assertEquals(1406, transitionCount);
    }
}