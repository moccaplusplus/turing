package turing.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import turing.machine.Settings;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static turing.gui.Gui.initComponent;
import static turing.gui.Gui.regexFormatter;
import static turing.gui.Gui.runInBackgroundThread;
import static turing.gui.Gui.runInFxApplicationThread;
import static turing.machine.Cmd.DEFAULT_CHARSET;

public class SettingsWidget extends GridPane {
    private static final String LAYOUT = "settings_widget.fxml";

    @FXML
    private TextField inputFileField;

    @FXML
    private Button inputFileButton;

    @FXML
    private TextField charsetField;

    @FXML
    private TextField outDirField;

    @FXML
    private Button outDirButton;

    @FXML
    private Button loadButton;

    @FXML
    private SettingsView settingsView;

    @FXML
    private TextField delayField;

    @FXML
    private Button executeButton;

    private Settings settings;

    private boolean preventExecution;

    private Consumer<SettingsWidget> executeListener;

    private BiConsumer<Settings, Exception> onSettingsListener;

    public SettingsWidget() {
        initComponent(this, LAYOUT);
        inputFileButton.setOnAction(e -> chooseInputFile());
        charsetField.setText(DEFAULT_CHARSET.displayName());
        charsetField.textProperty().addListener(e -> updateLoadButton());
        loadButton.setOnAction(e -> loadSettings());
        loadButton.setDisable(true);
        settingsView.setDisable(true);
        outDirButton.setOnAction(e -> chooseOutDir());
        outDirField.setText(getCwd().toString());
        delayField.setTextFormatter(regexFormatter("^\\d*$"));
        delayField.setText("500");
        delayField.textProperty().addListener(e -> updateExecuteButton());
        executeButton.setOnAction(e -> execute());
        executeButton.setDisable(true);
    }

    public void preventExecution(boolean preventExecution) {
        this.preventExecution = preventExecution;
        updateLoadButton();
        updateExecuteButton();
    }

    public Settings getSettings() {
        return settings;
    }

    public Path getOutPath() {
        return Path.of(outDirField.getText());
    }

    public long getDelayMillis() {
        if (delayField.getText().isBlank()) {
            return 0;
        }
        return Long.parseLong(delayField.getText());
    }

    public void setExecuteListener(Consumer<SettingsWidget> executeListener) {
        this.executeListener = executeListener;
    }

    public void setOnSettingsListener(BiConsumer<Settings, Exception> onSettingsListener) {
        this.onSettingsListener = onSettingsListener;
    }

    private void execute() {
        if (executeListener != null) {
            executeListener.accept(this);
        }
    }

    private void loadSettings() {
        loadButton.setDisable(true);
        var inputFile = inputFileField.getText();
        var charset = charsetField.getText();
        runInBackgroundThread(() -> {
            try {
                var settings = Settings.read(Path.of(inputFile), Charset.forName(charset));
                settings.validate();
                runInFxApplicationThread(() -> {
                    settingsView.setSettings(settings);
                    this.settings = settings;
                    updateExecuteButton();
                    if (onSettingsListener != null) {
                        onSettingsListener.accept(settings, null);
                    }
                });
            } catch (Exception e) {
                runInFxApplicationThread(() -> {
                    settingsView.clear();
                    this.settings = null;
                    updateExecuteButton();
                    if (onSettingsListener != null) {
                        onSettingsListener.accept(null, e);
                    }
                });
            } finally {
                runInFxApplicationThread(() -> loadButton.setDisable(false));
            }
        });
    }

    private void chooseInputFile() {
        final var fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(getDir(inputFileField.getText()));
        final var chosen = fileChooser.showOpenDialog(getScene().getWindow());
        if (chosen != null) {
            inputFileField.setText(chosen.getAbsolutePath());
            updateLoadButton();
        }
    }

    private void chooseOutDir() {
        final var directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(getDir(outDirField.getText()));
        final var chosen = directoryChooser.showDialog(getScene().getWindow());
        if (chosen != null) {
            outDirField.setText(chosen.getAbsolutePath());
        }
    }

    private void updateLoadButton() {
        loadButton.setDisable(preventExecution || inputFileButton.getText().isBlank() || charsetField.getText().isBlank());
    }

    private void updateExecuteButton() {
        executeButton.setDisable(preventExecution || settings == null || outDirField.getText().isBlank()
                || delayField.getText().isBlank());
    }

    private File getDir(String path) {
        return Optional.of(path)
                .map(File::new).map(File::getParentFile).filter(File::exists).filter(File::isDirectory)
                .orElseGet(() -> getCwd().toFile());
    }

    private static Path getCwd() {
        return Paths.get(".").toAbsolutePath().normalize();
    }
}
