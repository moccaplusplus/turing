package turing.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import turing.machine.Machine;
import turing.machine.Settings;
import turing.machine.Transition;

import static java.lang.String.format;
import static turing.gui.Gui.initComponent;

public class PreviewWidget extends BorderPane {
    private static final String LAYOUT = "preview_widget.fxml";

    @FXML
    private BandWidget bandWidget;

    @FXML
    private GraphWidget graphWidget;

    @FXML
    private Label statusLabel;

    public PreviewWidget() {
        initComponent(this, LAYOUT);
    }

    public void init(Settings settings, Runnable callback) {
        bandWidget.clear();
        graphWidget.init(settings, callback);
        statusLabel.setText("Settings Valid");
    }

    public void update(int iteration, Machine machine) {
        // state is volatile - read first for machine object synchronization.
        var state = machine.state();
        var bandStr = machine.band().bandStr();
        int headPos = machine.band().headPos();
        graphWidget.select(state);
        bandWidget.preview(bandStr, headPos);
        if (iteration == 0) {
            statusLabel.setText("Machine Started");
        } else {
            statusLabel.setText("Iteration " + iteration);
        }
    }

    public void update(Transition transition) {
        graphWidget.select(transition);
    }

    public void result(int iteration, boolean accepted) {
        statusLabel.setText(format("Finished in %s State after %d iterations",
                accepted ? "Accepting" : "Non-Accepting", iteration));
    }

    public void clear() {
        graphWidget.clear();
        bandWidget.clear();
        statusLabel.setText("");
    }
}
