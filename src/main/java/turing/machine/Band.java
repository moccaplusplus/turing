package turing.machine;

import java.util.Arrays;

class Band {
    private static final int INITIAL_SIZE = 32;
    private static final int EXPAND_SIZE = 16;
    private static final char EMPTY_CHARACTER = '#';

    private char[] backingArray;
    private int headPos;

    public void reset(String word) {
        int bandSize = INITIAL_SIZE;
        if (bandSize < word.length()) {
            bandSize += (int) (Math.ceil((double) (word.length() - INITIAL_SIZE) / EXPAND_SIZE) * EXPAND_SIZE);
        }
        backingArray = new char[bandSize];
        Arrays.fill(backingArray, EMPTY_CHARACTER);
        System.arraycopy(word.toCharArray(), 0, backingArray, 1, word.length());
        headPos = 1;
    }

    public char read() {
        return backingArray[headPos];
    }

    public void write(Character character, String move) {
        backingArray[headPos] = character;
        switch (move) {
            case "L", "l" -> moveLeft();
            case "R", "r" -> moveRight();
        }
    }

    private void moveLeft() {
        if (--headPos < 0) {
            var oldBackingArray = backingArray;
            backingArray = new char[backingArray.length + EXPAND_SIZE];
            System.arraycopy(oldBackingArray, 0, backingArray, EXPAND_SIZE, oldBackingArray.length);
            Arrays.fill(backingArray, 0, EXPAND_SIZE, EMPTY_CHARACTER);
            headPos += EXPAND_SIZE;
        }
    }

    private void moveRight() {
        if (++headPos >= backingArray.length) {
            var oldBackingArray = backingArray;
            backingArray = new char[backingArray.length + EXPAND_SIZE];
            System.arraycopy(oldBackingArray, 0, backingArray, 0, oldBackingArray.length);
            Arrays.fill(backingArray, oldBackingArray.length, backingArray.length, EMPTY_CHARACTER);
        }
    }

    @Override
    public String toString() {
        return Arrays.toString(backingArray) + System.lineSeparator() + "-".repeat(headPos) + "^";
    }
}
