package turing.gui;

import guru.nidi.graphviz.engine.Graphviz;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static turing.machine.Msg.msg;

public class Gui extends Application {
    private static final Collection<Runnable> onCloseListeners = new ArrayList<>();
    private static final ScheduledExecutorService executor;

    static {
        executor = Executors.newSingleThreadScheduledExecutor();
        addOnCloseListener(executor::shutdownNow);

        Graphviz.useEngine(new VizHack());
        addOnCloseListener(Graphviz::releaseEngine);

        var noop = new PrintStream(PrintStream.nullOutputStream());
        System.setOut(noop);
        System.setErr(noop);
    }

    public static void main(String[] args) {
        launch();
    }

    public static <T> T initComponent(Region component, String layout) {
        final var fxmlLoader = new FXMLLoader(component.getClass().getResource(layout));
        fxmlLoader.setRoot(component);
        fxmlLoader.setController(component);
        try {
            return fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static void addOnCloseListener(Runnable onClose) {
        synchronized (onCloseListeners) {
            onCloseListeners.add(onClose);
        }
    }

    public static void runInFxApplicationThread(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }

    public static void runInBackgroundThread(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            executor.execute(runnable);
        } else {
            runnable.run();
        }
    }

    public static void scheduleInBackgroundThread(Runnable runnable, long delay) {
        executor.schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }

    public static TextFormatter<?> regexFormatter(String regex) {
        return new TextFormatter<>(change -> change.getControlNewText().matches(regex) ? change : null);
    }

    @Override
    public void start(Stage stage) {
        var mainScreen = new MainScreen();
        var scene = new Scene(mainScreen);
        stage.setScene(scene);
        stage.setTitle("Turing Machine Emulator");
        stage.show();
    }

    @Override
    public void stop() {
        for (var onClose : onCloseListeners) {
            try {
                onClose.run();
            } catch (Throwable e) {
                System.err.println(msg(e));
            }
        }
    }
}