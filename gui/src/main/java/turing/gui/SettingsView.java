package turing.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import turing.machine.Settings;
import turing.machine.Transition;

import java.util.function.Function;

import static java.lang.String.format;
import static turing.gui.Gui.initComponent;

public class SettingsView extends GridPane {
    private static final String LAYOUT = "settings_view.fxml";

    @FXML
    private TextField bandAlphabetField;

    @FXML
    private TextField inputAlphabetField;

    @FXML
    private TextField wordField;

    @FXML
    private TextField statesField;

    @FXML
    private TextField startStateField;

    @FXML
    private TextField finalStatesField;

    @FXML
    private TableView<Transition> transitionsTable;

    @FXML
    private Label statusLabel;

    public SettingsView() {
        initComponent(this, LAYOUT);
        var columns = transitionsTable.getColumns();
        columns.add(simpleColumn("From", Transition::fromState));
        columns.add(simpleColumn("To", Transition::toState));
        columns.add(simpleColumn("Read", Transition::readChar));
        columns.add(simpleColumn("Write", Transition::writeChar));
        columns.add(simpleColumn("Move", Transition::moveDir));
        transitionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    public void setSettings(Settings settings) {
        statusLabel.setText("Settings Valid");
        setDisable(false);
        bandAlphabetField.setText(settings.bandAlphabet().toString());
        inputAlphabetField.setText(settings.inputAlphabet().toString());
        wordField.setText(settings.word());
        statesField.setText(settings.states().toString());
        startStateField.setText(settings.startState());
        finalStatesField.setText(settings.finalStates().toString());
        transitionsTable.getItems().clear();
        transitionsTable.getItems().addAll(settings.transitions());
    }

    public void setError(Exception e) {
        statusLabel.setText(format("%s: %s", e.getClass().getSimpleName(), e.getMessage()));
        bandAlphabetField.clear();
        inputAlphabetField.clear();
        wordField.clear();
        statesField.clear();
        startStateField.clear();
        finalStatesField.clear();
        transitionsTable.getItems().clear();
        setDisable(true);
    }

    private static TableColumn<Transition, ?> simpleColumn(
            String label, Function<Transition, Object> valueMapping) {
        var col = new TableColumn<Transition, String>(label);
        col.setCellValueFactory(t -> new SimpleStringProperty(String.valueOf(valueMapping.apply(t.getValue()))));
        return col;
    }
}
