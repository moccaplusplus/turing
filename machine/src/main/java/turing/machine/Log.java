package turing.machine;

import java.util.Arrays;
import java.util.function.Consumer;

import static turing.machine.Msg.indent;
import static turing.machine.Msg.msg;

public class Log {
    public Consumer<String> appender;

    @SafeVarargs
    public Log(Consumer<String>... appender) {
        setAppender(appender);
    }

    @SafeVarargs
    public final void setAppender(Consumer<String>... appender) {
        this.appender = Arrays.stream(appender).reduce(Consumer::andThen).orElse(null);
    }

    @SafeVarargs
    public final void addAppender(Consumer<String>... appender) {
        this.appender = Arrays.stream(appender).reduce(this.appender, Consumer::andThen);
    }

    public void clearAppender() {
        setAppender();
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

    public void abandoned(int iteration) {
        log("Logging abandoned after " + iteration + "iterations. Machine still running...");
    }

    public void log(String msg) {
        if (appender != null) {
            appender.accept(msg + System.lineSeparator());
        }
    }
}
