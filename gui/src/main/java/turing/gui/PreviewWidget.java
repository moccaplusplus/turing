package turing.gui;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import turing.machine.Machine;
import turing.machine.Settings;
import turing.machine.Transition;

import static turing.gui.Gui.initComponent;

public class PreviewWidget extends BorderPane {
    private static final String LAYOUT = "preview_widget.fxml";

    @FXML
    private BandWidget bandWidget;

    @FXML
    private GraphWidget graphWidget;

    public PreviewWidget() {
        initComponent(this, LAYOUT);
    }

    public void init(Settings settings) {
        bandWidget.clear();
        graphWidget.init(settings);
    }

    public void update(Machine machine) {
        bandWidget.preview(machine.band());
        graphWidget.select(machine.state());
    }

    public void update(Transition transition) {
        graphWidget.select(transition);
    }

    public void clear() {
        bandWidget.clear();
        graphWidget.clear();
    }
}
