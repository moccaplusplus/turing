package turing.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import turing.machine.Settings;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static turing.gui.Gui.initComponent;
import static turing.gui.Gui.regexFormatter;
import static turing.machine.Cmd.DEFAULT_CHARSET;
import static turing.machine.Cmd.DEFAULT_OUT;

public class SettingsWidget extends GridPane {
    private static final String LAYOUT = "settings_widget.fxml";
    private static final long DEFAULT_DELAY = 150;

    @FXML
    private TextField inputFileField;

    @FXML
    private Button inputFileButton;

    @FXML
    private TextField charsetField;

    @FXML
    private Button loadButton;

    @FXML
    private SettingsView settingsView;

    @FXML
    private TextField outputDirField;

    @FXML
    private Button outputDirButton;

    @FXML
    private TextField outputFilenameField;

    @FXML
    private TextField delayField;

    @FXML
    private Button executeButton;

    private boolean preventExecution;

    public SettingsWidget() {
        initComponent(this, LAYOUT);
        inputFileButton.setOnAction(e -> chooseInputFile());
        charsetField.setText(DEFAULT_CHARSET.displayName());
        charsetField.textProperty().addListener(e -> updateLoadButton());
        loadButton.setDisable(true);
        settingsView.setDisable(true);
        outputDirButton.setOnAction(e -> chooseOutDir());
        outputDirField.setText(getCwd().toString());
        outputFilenameField.setText(DEFAULT_OUT);
        outputFilenameField.textProperty().addListener(e -> updateExecuteButton());
        delayField.setTextFormatter(regexFormatter("^\\d*$"));
        delayField.setText(String.valueOf(DEFAULT_DELAY));
        delayField.textProperty().addListener(e -> updateExecuteButton());
        executeButton.setDisable(true);
    }

    public String getInputFile() {
        return inputFileField.getText();
    }

    public String getCharset() {
        return charsetField.getText();
    }

    public String getOutputDir() {
        return outputDirField.getText();
    }

    public String getOutputFilename() {
        return outputFilenameField.getText();
    }

    public long getDelay() {
        try {
            return Math.max(1, Long.parseLong(delayField.getText()));
        } catch (Exception e) {
            return DEFAULT_DELAY;
        }
    }

    public void preventExecution(boolean preventExecution) {
        this.preventExecution = preventExecution;
        updateLoadButton();
        updateExecuteButton();
    }

    public Settings getSettings() {
        return settingsView.getSettings();
    }

    public void setSettings(Settings settings) {
        if (settings == null) {
            settingsView.clear();
        } else {
            settingsView.setSettings(settings);
        }
    }

    public void setExecuteListener(Runnable executeListener) {
        executeButton.setOnAction(executeListener == null ? null : e -> executeListener.run());
    }

    public void setLoadListener(Runnable loadListener) {
        loadButton.setOnAction(loadListener == null ? null : e -> loadListener.run());
    }

    private void chooseInputFile() {
        var fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(getDir(inputFileField.getText()));
        var chosen = fileChooser.showOpenDialog(getScene().getWindow());
        if (chosen != null) {
            inputFileField.setText(chosen.getAbsolutePath());
            updateLoadButton();
        }
    }

    private void chooseOutDir() {
        var directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(getDir(outputDirField.getText()));
        var chosen = directoryChooser.showDialog(getScene().getWindow());
        if (chosen != null) {
            outputDirField.setText(chosen.getAbsolutePath());
            updateExecuteButton();
        }
    }

    private void updateLoadButton() {
        loadButton.setDisable(preventExecution || getInputFile().isBlank() || getCharset().isBlank());
    }

    private void updateExecuteButton() {
        executeButton.setDisable(preventExecution || getSettings() == null || getOutputDir().isBlank()
                || getOutputFilename().isBlank() || delayField.getText().isBlank());
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
