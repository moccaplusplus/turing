package turing.gui;

import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import turing.machine.Log;
import turing.machine.Machine;
import turing.machine.Settings;

import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import static turing.gui.Gui.initComponent;
import static turing.gui.Gui.runInBackgroundThread;
import static turing.gui.Gui.runInFxApplicationThread;
import static turing.gui.Gui.scheduleInBackgroundThread;
import static turing.machine.Cmd.REPORT_ITERATIONS_UNTIL;
import static turing.machine.Msg.msg;

public class MainScreen extends SplitPane {
    private static final String LAYOUT = "main_screen.fxml";

    private final Log log;

    @FXML
    private SettingsWidget settingsWidget;

    @FXML
    private PreviewWidget previewWidget;

    @FXML
    private ConsoleWidget consoleWidget;

    public MainScreen() {
        initComponent(this, LAYOUT);
        settingsWidget.setLoadListener(this::readSettings);
        settingsWidget.setExecuteListener(this::execute);
        previewWidget.setDisable(true);
        log = new Log(consoleWidget::writeConsole);
    }

    private void readSettings() {
        var inputFile = settingsWidget.getInputFile();
        var charset = settingsWidget.getCharset();
        previewWidget.setDisable(true);
        settingsWidget.preventExecution(true);
        consoleWidget.clearConsole();
        runInBackgroundThread(() -> {
            try {
                var settings = Settings.parse(Path.of(inputFile), Charset.forName(charset));
                settings.validate();
                log.settings(settings);
                runInFxApplicationThread(() -> {
                    settingsWidget.setSettings(settings);
                    previewWidget.init(settings);
                    settingsWidget.preventExecution(false);
                });
            } catch (Exception e) {
                runInFxApplicationThread(() -> {
                    previewWidget.clear();
                    settingsWidget.setSettings(null);
                    settingsWidget.preventExecution(false);
                });
                consoleWidget.writeConsole(msg(e));
            }
        });
    }

    private void execute() {
        var settings = settingsWidget.getSettings();
        var delay = settingsWidget.getDelay();
        var outputDir = settingsWidget.getOutputDir();
        var outputFilename = settingsWidget.getOutputFilename();

        settingsWidget.preventExecution(true);
        previewWidget.setDisable(false);
        consoleWidget.clearConsole();

        runInBackgroundThread(new Runnable() {
            private PrintWriter fileWriter;
            private Machine machine;
            private int iteration = 0;
            private long start;

            @Override
            public void run() {
                try {
                    var outputPath = Path.of(outputDir).resolve(outputFilename);
                    fileWriter = new PrintWriter(Files.newOutputStream(outputPath), true);
                    log.appenders.add(fileWriter::println);
                    log.settings(settings);
                    start = System.currentTimeMillis();
                    machine = new Machine(settings.startState(), settings.finalStates(), settings.transitions());
                    machine.init(settings.word());
                    log.initialized(machine);
                    runInFxApplicationThread(() -> previewWidget.update(iteration, machine));
                    scheduleInBackgroundThread(safeWrap(this::applyTransition), delay);
                } catch (Exception e) {
                    doCatch(e);
                }
            }

            private void applyTransition() {
                var transition = machine.proceed();
                if (transition == null) {
                    exitLoop();
                } else {
                    iteration++;
                    log.iteration(iteration, transition, machine);
                    runInFxApplicationThread(() -> previewWidget.update(transition));
                    scheduleInBackgroundThread(safeWrap(this::showState), delay);
                }
            }

            private void showState() {
                if (iteration == REPORT_ITERATIONS_UNTIL) {
                    log.appenders.clear();
                    fileWriter.close();
                }
                runInFxApplicationThread(() -> previewWidget.update(iteration, machine));
                if (machine.isInFinalState()) {
                    exitLoop();
                } else {
                    scheduleInBackgroundThread(safeWrap(this::applyTransition), delay);
                }
            }

            private void exitLoop() {
                long time = System.currentTimeMillis() - start;
                log.result(machine, settings.word(), iteration, time);
                boolean accepted = machine.isInFinalState();
                runInFxApplicationThread(() -> previewWidget.result(iteration, accepted));
                doFinally();
            }

            private Runnable safeWrap(Runnable runnable) {
                return () -> {
                    try {
                        runnable.run();
                    } catch (Exception e) {
                        doCatch(e);
                    }
                };
            }

            private void doCatch(Exception e) {
                consoleWidget.writeConsole(msg(e));
                doFinally();
            }

            private void doFinally() {
                log.appenders.clear();
                log.appenders.add(consoleWidget::writeConsole);
                fileWriter.close();
                runInFxApplicationThread(() -> settingsWidget.preventExecution(false));
            }
        });
    }
}
