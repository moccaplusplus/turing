package turing.gui;

import javafx.beans.NamedArg;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import turing.machine.Band;

import java.util.stream.IntStream;

import static java.lang.String.format;
import static turing.gui.Gui.initComponent;
import static turing.gui.Gui.runInBackgroundThread;
import static turing.gui.Gui.runInFxApplicationThread;

public class BandWidget extends BorderPane {
    private static final String LAYOUT = "band_widget.fxml";
    private static final int DISPLAY_COUNT = 16;
    private static final String EMPTY_CELL_TEXT = String.valueOf(Band.EMPTY_CHARACTER);
    private static final Background BACKGROUND_EVEN = Background.fill(Color.WHITE);
    private static final Background BACKGROUND_ODD = Background.EMPTY;
    private static final Background BACKGROUND_SELECTED = Background.fill(Color.LIGHTBLUE);

    @FXML
    private HBox cellBox;

    @FXML
    private HBox numBox;

    @FXML
    private Label statusLabel;

    private int selected = -1;

    public BandWidget(@NamedArg("prefWidth") double prefWidth, @NamedArg("cellHeight") double cellHeight) {
        initComponent(this, LAYOUT);
        setFocusTraversable(false);
        setMouseTransparent(true);
        var cellWidth = prefWidth / DISPLAY_COUNT;
        cellBox.getChildren().addAll(IntStream.range(0, DISPLAY_COUNT)
                .mapToObj(i -> {
                    var label = new Label(EMPTY_CELL_TEXT);
                    label.setAlignment(Pos.CENTER);
                    label.setStyle("-fx-font-size: 16");
                    label.setPrefWidth(cellWidth);
                    label.setPrefHeight(cellHeight);
                    label.setBackground(i % 2 == 0 ? BACKGROUND_EVEN : BACKGROUND_ODD);
                    return label;
                })
                .toList());
        cellBox.setPrefWidth(prefWidth);
        cellBox.setPrefHeight(cellHeight);
        cellBox.setBorder(Border.stroke(Color.LIGHTGRAY));

        numBox.getChildren().addAll(IntStream.range(0, DISPLAY_COUNT)
                .mapToObj(i -> {
                    var label = new Label(String.valueOf(i));
                    label.setAlignment(Pos.CENTER);
                    label.setStyle("-fx-font-size: 9");
                    label.setTextFill(Color.GRAY);
                    label.setPrefWidth(cellWidth);
                    label.setBackground(Background.EMPTY);
                    return label;
                })
                .toList());
        numBox.setPrefWidth(prefWidth);

        statusLabel.setText("Machine Band Size: ");
        setPrefWidth(prefWidth);
    }

    public void preview(Band band) {
        preview(band.bandStr(), band.headPos());
    }

    public void clear() {
        runInFxApplicationThread(() -> {
            statusLabel.setText("Machine Band Size: ");
            updateNums(0);
            clearBand();
            selectCell(-1);
        });
    }

    private void preview(String band, int head) {
        runInBackgroundThread(() -> {
            int start = Math.max(0, head - DISPLAY_COUNT / 2);
            var viewFrame = band.substring(start, start + DISPLAY_COUNT);
            var headTransl = head + start;
            runInFxApplicationThread(() -> {
                statusLabel.setText(format("Machine Band Size: %d", band.length()));
                updateNums(start);
                writeCells(viewFrame);
                selectCell(headTransl);
            });
        });
    }

    private void selectCell(int index) {
        if (selected != -1) {
            getCell(selected).setBackground(selected % 2 == 0 ? BACKGROUND_EVEN : BACKGROUND_ODD);
        }
        selected = index;
        if (selected != -1) {
            getCell(selected).setBackground(BACKGROUND_SELECTED);
        }
    }

    private void updateNums(int start) {
        if (!getNum(0).getText().equals(String.valueOf(start))) {
            IntStream.range(0, DISPLAY_COUNT)
                    .forEach(i -> getNum(i).setText(String.valueOf(start + i)));
        }
    }

    private void writeCells(String viewFrame) {
        IntStream.range(0, Math.min(DISPLAY_COUNT, viewFrame.length()))
                .forEach(i -> getCell(i).setText(String.valueOf(viewFrame.charAt(i))));
    }

    private void clearBand() {
        IntStream.range(0, DISPLAY_COUNT).mapToObj(this::getCell).forEach(label -> label.setText(EMPTY_CELL_TEXT));
    }

    private Label getCell(int index) {
        return (Label) cellBox.getChildren().get(index);
    }

    private Label getNum(int index) {
        return (Label) numBox.getChildren().get(index);
    }
}
