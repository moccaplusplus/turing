package turing.machine;

public interface Utils {
    static String indent(Object o) {
        return indent(String.valueOf(o));
    }

    static String indent(String s) {
        return s.replaceAll("(?m)^", "   ");
    }
}
