package turing.gui;

import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import turing.machine.Log;
import turing.machine.Machine;
import turing.machine.Settings;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import static turing.gui.Gui.initComponent;
import static turing.gui.Gui.runInBackgroundThread;
import static turing.gui.Gui.runInFxApplicationThread;
import static turing.machine.Cmd.REPORT_ITERATIONS_UNTIL;
import static turing.machine.Msg.msg;

public class MainScreen extends SplitPane {
    private static final String LAYOUT = "main_screen.fxml";

    private final Object syncLock = new Object();

    @FXML
    private SettingsWidget settingsWidget;

    @FXML
    private BandWidget bandWidget;

    @FXML
    private GraphWidget graphWidget;

    @FXML
    private TextArea console;

    public MainScreen() {
        initComponent(this, LAYOUT);
        console.setStyle("-fx-font-size: 12; -fx-font-family: monospace;");
        settingsWidget.setExecuteListener(e -> execute(e.getSettings(),
                e.getOutPath(), e.getDelayMillis()));
        settingsWidget.setOnSettingsListener(this::onSettings);
    }

    private void onSettings(Settings settings, Exception error) {
        runInBackgroundThread(() -> {
            bandWidget.clear();
            clearConsole();
            if (settings != null) {
                graphWidget.init(settings.transitions());
                writeConsole(msg(settings));
            }
            if (error != null) {
                graphWidget.clear();
                writeConsole(msg(error));
            }
        });
    }

    @SuppressWarnings("BusyWait")
    private void execute(Settings settings, Path outPath, long delay) {
        settingsWidget.preventExecution(true);
        clearConsole();

        runInBackgroundThread(() -> {
            try (var log = new Log(logOutputWriter(Files.newBufferedWriter(outPath)))) {
                log.settings(settings);

                long start = System.currentTimeMillis();

                var machine = new Machine(settings.startState(), settings.finalStates(), settings.transitions());
                machine.init(settings.word());

                log.initialized(machine);
                bandWidget.preview(machine.band());
                graphWidget.select(machine.state());
                if (delay > 0) {
                    Thread.sleep(delay);
                }

                int iteration = 0;
                while (!machine.isInFinalState()) {
                    var transition = machine.proceed();
                    if (transition == null) {
                        break;
                    }

                    graphWidget.select(transition);
                    if (delay > 0) {
                        Thread.sleep(delay);
                    }

                    iteration++;
                    if (iteration <= REPORT_ITERATIONS_UNTIL) {
                        log.iteration(iteration, transition, machine);
                    } else if (iteration == REPORT_ITERATIONS_UNTIL + 1) {
                        log.close();
                    }

                    bandWidget.preview(machine.band());
                    graphWidget.select(machine.state());
                    if (delay > 0) {
                        Thread.sleep(delay);
                    }
                }

                long time = System.currentTimeMillis() - start;
                if (iteration <= REPORT_ITERATIONS_UNTIL) {
                    log.result(machine, settings.word(), iteration, time);
                }
            } catch (Exception e) {
                writeConsole(msg(e));
            } finally {
                runInFxApplicationThread(() -> settingsWidget.preventExecution(false));
            }
        });
    }

    private void writeConsole(String text) {
        runInFxApplicationThread(() -> {
            synchronized (syncLock) {
                console.appendText(text);
            }
        });
    }

    private void clearConsole() {
        runInFxApplicationThread(() -> {
            synchronized (syncLock) {
                console.clear();
            }
        });
    }

    private Writer logOutputWriter(Writer fileWriter) {
        return new Writer() {
            @Override
            public void write(@Nonnull char[] buf, int off, int len) throws IOException {
                writeConsole(new String(buf, off, len));
                fileWriter.write(buf, off, len);
            }

            @Override
            public void flush() throws IOException {
                fileWriter.flush();
            }

            @Override
            public void close() throws IOException {
                fileWriter.close();
            }
        };
    }
}
