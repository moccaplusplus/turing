package turing.machine;

import java.util.Map;
import java.util.Set;

public class Configuration {
    public Set<Character> bandAlphabet;
    public Set<Character> inputAlphabet;
    public String word;
    public Set<String> states;
    public String startState;
    public Set<String> finalStates;
    public Map<String, Map<Character, Transition>> transitions;
}
