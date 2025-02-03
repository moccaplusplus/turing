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

import static java.nio.charset.StandardCharsets.UTF_8;
import static turing.gui.Gui.initComponent;
import static turing.gui.Gui.regexFormatter;
import static turing.gui.Gui.runOnFxApplicationThread;

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
    private TextField outFileNameField;

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
        outDirButton.setOnAction(e -> chooseOutDir());
        outDirField.setText(getCwd().getAbsolutePath());
        outFileNameField.setText("out.log");
        loadButton.setOnAction(e -> loadSettings());
        loadButton.setDisable(true);
        executeButton.setOnAction(e -> execute());
        executeButton.setDisable(true);
        charsetField.setText(UTF_8.displayName());
        charsetField.textProperty().addListener(e -> updateLoadButton());
        delayField.setTextFormatter(regexFormatter("^\\d*$"));
        delayField.setText("500");
        settingsView.setDisable(true);
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
        if (outDirField.getText().isBlank() || outFileNameField.getText().isBlank()) {
            return null;
        }
        return Path.of(outDirField.getText(), outFileNameField.getText());
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
        new Thread(() -> {
            try {
                var settings = Settings.read(Path.of(inputFile), Charset.forName(charset));
                settings.validate();
                runOnFxApplicationThread(() -> {
                    settingsView.setSettings(settings);
                    this.settings = settings;
                    updateExecuteButton();
                    if (onSettingsListener != null) {
                        onSettingsListener.accept(settings, null);
                    }
                });
            } catch (Exception e) {
                runOnFxApplicationThread(() -> {
                    settingsView.setError(e);
                    this.settings = null;
                    updateExecuteButton();
                    if (onSettingsListener != null) {
                        onSettingsListener.accept(null, e);
                    }
                });
            } finally {
                runOnFxApplicationThread(() -> loadButton.setDisable(false));
            }
        }).start();

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
                .orElseGet(SettingsWidget::getCwd);
    }

    private static File getCwd() {
        return Paths.get(".").toAbsolutePath().normalize().toFile();
    }
}
