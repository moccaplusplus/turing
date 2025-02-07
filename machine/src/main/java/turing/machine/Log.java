package turing.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static turing.machine.Msg.indent;
import static turing.machine.Msg.msg;

public class Log {
    public final List<Consumer<String>> appenders;

    @SafeVarargs
    public Log(Consumer<String>... appenders) {
        this.appenders = new ArrayList<>(List.of(appenders));
    }

    public void settings(Settings settings) {
        log("Machine Settings:");
        log(indent(msg(settings)));
    }

    public void initialized(Machine machine) {
        log("Machine Initialized:");
        log(indent(msg(machine)));
    }

    public void iteration(int iteration, Transition transition, Machine machine) {
        log("Iteration: " + iteration);
        log(indent("Applied Transition:"));
        log(indent(indent(msg(transition))));
        log(indent("Machine After Transition:"));
        log(indent(indent(msg(machine))));
    }

    public void result(Machine machine, String inputWord, int iterations, long time) {
        if (machine.isInFinalState()) {
            log("Machine Finished in Accepting State:");
            log(indent("Computed word: " + machine.band().currentWord()));
        } else {
            log("Machine Finished in Non-Accepting State:");
        }
        log(indent("Input word: " + inputWord));
        log(indent("Iterations: " + iterations));
        log(indent("Time: " + time + "ms"));
    }

    public void log(String msg) {
        for (var line : msg.split(System.lineSeparator())) {
            for (var appender : appenders) {
                appender.accept(line);
            }
        }
    }
}
