package turing.machine;

import java.util.Arrays;

public class Band {
    public static final int INITIAL_SIZE = 32;
    public static final int EXPAND_SIZE = 16;
    public static final char EMPTY_CHARACTER = '#';

    private char[] backingArray;
    private int headPos;

    public void reset(String word) {
        int bandSize = INITIAL_SIZE;
        if (bandSize < word.length() + 2) {
            bandSize += (int) (Math.ceil((double) (word.length() + 2 - INITIAL_SIZE) / EXPAND_SIZE) * EXPAND_SIZE);
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
            case "P", "p" -> moveRight();
            default -> throw new IllegalStateException("Invalid move direction: " + character);
        }
    }

    private void moveLeft() {
        if (--headPos == -1) {
            var oldBackingArray = backingArray;
            backingArray = new char[backingArray.length + EXPAND_SIZE];
            System.arraycopy(oldBackingArray, 0, backingArray, EXPAND_SIZE, oldBackingArray.length);
            Arrays.fill(backingArray, 0, EXPAND_SIZE, EMPTY_CHARACTER);
            headPos += EXPAND_SIZE;
        }
    }

    private void moveRight() {
        if (++headPos == backingArray.length) {
            var oldBackingArray = backingArray;
            backingArray = new char[backingArray.length + EXPAND_SIZE];
            System.arraycopy(oldBackingArray, 0, backingArray, 0, oldBackingArray.length);
            Arrays.fill(backingArray, oldBackingArray.length, backingArray.length, EMPTY_CHARACTER);
        }
    }

    public int headPos() {
        return headPos;
    }

    public String bandStr() {
        return String.valueOf(backingArray);
    }

    public int length() {
        return backingArray.length;
    }

    public String currentWord() {
        int start = 0;
        int end = backingArray.length;
        while (start < end && backingArray[start] == EMPTY_CHARACTER) start++;
        while (start < end && backingArray[end - 1] == EMPTY_CHARACTER) end--;
        int length = end - start;
        return length <= 0 ? "" : new String(backingArray, start, length);
    }
}
