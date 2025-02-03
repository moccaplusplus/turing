package turing.machine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BandTest {
    private Band objectUnderTest;

    @BeforeEach
    void setup() {
        objectUnderTest = new Band();
    }

    @Test
    void shouldSetShortWord() {
        // given
        var word = "short#word";

        // when
        objectUnderTest.reset(word);

        // then
        assertEquals("short#word", objectUnderTest.currentWord());
        assertEquals(1, objectUnderTest.headPos());
        assertEquals(Band.INITIAL_SIZE, objectUnderTest.bandStr().length());
        assertEquals("#short#word#####################", objectUnderTest.bandStr());
    }

    @Test
    void shouldSetLongWord() {
        // given
        var word = "init#word#loner#than#initial#band#size";

        // when
        objectUnderTest.reset(word);

        // then
        assertEquals("init#word#loner#than#initial#band#size", objectUnderTest.currentWord());
        assertEquals(1, objectUnderTest.headPos());
        assertEquals(Band.INITIAL_SIZE + Band.EXPAND_SIZE, objectUnderTest.bandStr().length());
        assertEquals("#init#word#loner#than#initial#band#size#########", objectUnderTest.bandStr());
    }

    @Test
    void shouldExpandLeftOnce() {
        // given
        var word = "word";

        // when
        objectUnderTest.reset(word);
        objectUnderTest.write('x', "L");
        objectUnderTest.write('y', "L");

        // then
        assertEquals("yxord", objectUnderTest.currentWord());
        assertEquals(Band.EXPAND_SIZE - 1, objectUnderTest.headPos());
        assertEquals(Band.INITIAL_SIZE + Band.EXPAND_SIZE, objectUnderTest.bandStr().length());
        assertEquals("################yxord###########################", objectUnderTest.bandStr());
    }

    @Test
    void shouldExpandRightOnce() {
        // given
        var word = "word";

        // when
        objectUnderTest.reset(word);
        for (int i = 0; i < word.length(); i++) {
            objectUnderTest.write('x', "P");
        }
        for (int i = 0, limit = Band.INITIAL_SIZE - objectUnderTest.headPos(); i < limit; i++) {
            objectUnderTest.write('y', "P");
        }

        // then
        assertEquals("xxxxyyyyyyyyyyyyyyyyyyyyyyyyyyy", objectUnderTest.currentWord());
        assertEquals(Band.INITIAL_SIZE, objectUnderTest.headPos());
        assertEquals(Band.INITIAL_SIZE + Band.EXPAND_SIZE, objectUnderTest.bandStr().length());
        assertEquals("#xxxxyyyyyyyyyyyyyyyyyyyyyyyyyyy################", objectUnderTest.bandStr());
    }

    @Test
    void shouldExpandLeftAndRightOnce() {
        // given
        var word = "word";

        // when
        objectUnderTest.reset(word);
        for (int i = 0; i < 5; i++) {
            objectUnderTest.write('x', "L");
        }
        for (int i = 0; i < 40; i++) {
            objectUnderTest.write('y', "P");
        }

        // then
        assertEquals("y".repeat(40), objectUnderTest.currentWord());
        assertEquals(52, objectUnderTest.headPos());
        assertEquals(Band.INITIAL_SIZE + 2 * Band.EXPAND_SIZE, objectUnderTest.bandStr().length());
        assertEquals("############yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy############", objectUnderTest.bandStr());
    }

    @Test
    void shouldExpandLeftAndRightTwice() {
        // given
        var word = "word";

        // when
        objectUnderTest.reset(word);
        for (int i = 0; i < 2; i++) {
            while (objectUnderTest.headPos() > 0) {
                char c = objectUnderTest.read();
                objectUnderTest.write(c == Band.EMPTY_CHARACTER ? 'a' : c, "L");
            }
            objectUnderTest.write('b', "L");
            while (objectUnderTest.headPos() < objectUnderTest.length() - 1) {
                char c = objectUnderTest.read();
                objectUnderTest.write(c == Band.EMPTY_CHARACTER ? 'c' : c, "P");
            }
            objectUnderTest.write('d', "P");
        }

        // then
        assertEquals(
                "cbaaaaaaaaaaaaaacbwordccccccccccccccccccccccccccdaccccccccccccccd",
                objectUnderTest.currentWord());
        assertEquals(Band.INITIAL_SIZE + 3 * Band.EXPAND_SIZE, objectUnderTest.headPos());
        assertEquals(Band.INITIAL_SIZE + 4 * Band.EXPAND_SIZE, objectUnderTest.bandStr().length());
        assertEquals(
                "###############cbaaaaaaaaaaaaaacbwordccccccccccccccccccccccccccdaccccccccccccccd################",
                objectUnderTest.bandStr());
    }
}