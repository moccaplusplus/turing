package turing.machine;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

import static turing.machine.Msg.indent;
import static turing.machine.Msg.msg;

public class Log extends PrintWriter {
    public Log(Writer out) {
        super(out);
    }

    public Log(OutputStream out) {
        super(out);
    }

    public void settings(Settings settings) {
        println("Machine Settings:");
        println(indent(msg(settings)));
    }

    public void initialized(Machine machine) {
        println("Machine Initialized:");
        println(indent(msg(machine)));
    }

    public void iteration(int iteration, Transition transition, Machine machine) {
        println("Iteration: " + iteration);
        println(indent("Applied Transition:"));
        println(indent(indent(msg(transition))));
        println(indent("Machine After Transition:"));
        println(indent(indent(msg(machine))));
    }

    public void result(Machine machine, String inputWord, int iterations, long time) {
        if (machine.isInFinalState()) {
            println("Machine Finished in Accepting State:");
            println(indent("Computed word: " + machine.band().currentWord()));
        } else {
            println("Machine Finished in Non-Accepting State:");
        }
        println(indent("Input word: " + inputWord));
        println(indent("Iterations: " + iterations));
        println(indent("Time: " + time + "ms"));
    }
}
