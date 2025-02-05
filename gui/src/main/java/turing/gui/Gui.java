package turing.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Gui extends Application {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

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

    public static TextFormatter<?> regexFormatter(String regex) {
        return new TextFormatter<>(change -> change.getControlNewText().matches(regex) ? change : null);
    }

    @Override
    public void start(Stage stage) {
        getParameters().getRaw();
        var mainScreen = new MainScreen();
        var scene = new Scene(mainScreen);
        stage.setScene(scene);
        stage.setTitle("Turing Machine Emulator");
        stage.show();
    }

    @Override
    public void stop() {
        executor.close();
    }
}