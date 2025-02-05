package turing.gui;

import javafx.beans.NamedArg;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Border;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import turing.machine.Band;

import java.util.LinkedList;
import java.util.stream.IntStream;

import static turing.gui.Gui.runInFxApplicationThread;

public class BandWidget extends ListView<Character> {
    private static final int DISPLAY_COUNT = 16;

    public BandWidget(@NamedArg("prefWidth") double prefWidth) {
        setFocusTraversable(false);
        setMouseTransparent(true);
        setOrientation(Orientation.HORIZONTAL);
        setFixedCellSize(Region.USE_PREF_SIZE);
        var cellWidth = (prefWidth - 3) / DISPLAY_COUNT;
        setCellFactory(listView -> new ListCell<>() {
            {
                setAlignment(Pos.CENTER);
                setBorder(Border.EMPTY);
                setPadding(Insets.EMPTY);
                setPrefWidth(cellWidth);
            }

            @Override
            protected void updateItem(Character item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    setText(item.toString());
                }
            }
        });
        setPrefWidth(prefWidth);
        clearBand();
    }

    public void preview(Band band) {
        var bandStr = band.bandStr();
        var headPos = band.headPos();
        runInFxApplicationThread(() -> showBand(bandStr, headPos));
    }

    public void clear() {
        runInFxApplicationThread(this::clearBand);
    }

    private void showBand(String band, int head) {
        var viewFrame = new LinkedList<Character>();
        viewFrame.add(band.charAt(head));
        int i = 0, j = 0;
        while (viewFrame.size() < DISPLAY_COUNT) {
            if (head - i - 1 > -1) {
                viewFrame.addFirst(band.charAt(head - (++i)));
                if (viewFrame.size() == DISPLAY_COUNT) break;
            }
            if (head + j + 1 < band.length()) {
                viewFrame.addLast(band.charAt(head + (++j)));
            }
        }
        getItems().setAll(viewFrame);
        getSelectionModel().select(i);
        scrollTo(i);
    }

    private void clearBand() {
        getItems().setAll(IntStream.range(0, DISPLAY_COUNT).mapToObj(i -> Band.EMPTY_CHARACTER).toList());
    }
}
