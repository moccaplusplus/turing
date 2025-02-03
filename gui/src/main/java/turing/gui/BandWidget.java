package turing.gui;

import javafx.geometry.Orientation;
import javafx.scene.control.ListView;
import turing.machine.Band;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import static turing.gui.Gui.runOnFxApplicationThread;

public class BandWidget extends ListView<Character> {
    private static final int DISPLAY_COUNT = 17;

    public BandWidget() {
        setFocusTraversable(false);
        setMouseTransparent(true);
        setOrientation(Orientation.HORIZONTAL);
        setFixedCellSize(Math.floor(getPrefWidth() / DISPLAY_COUNT));
        clear();
    }

    public void preview(Band band) {
        var bandStr = band.bandStr();
        var headPos = band.headPos();
        runOnFxApplicationThread(() -> showBand(bandStr, headPos));
    }

    public void clear() {
        runOnFxApplicationThread(this::clearBand);
    }

    private void showBand(String band, int head) {
        var viewFrame = new LinkedList<Character>();
        viewFrame.add(band.charAt(head));
        int i = 0, j = 0;
        while (viewFrame.size() < DISPLAY_COUNT) {
            if (head - i - 1 > -1) {
                viewFrame.addFirst(band.charAt(head - (++i)));
            }
            if (head + j + 1 < band.length()) {
                viewFrame.addLast(band.charAt(head + (++j)));
            }
        }
        setBandItems(viewFrame);
        getSelectionModel().select(i);
        scrollTo(i);
    }

    private void clearBand() {
        setBandItems(IntStream.range(0, DISPLAY_COUNT).mapToObj(i -> Band.EMPTY_CHARACTER).toList());
    }

    private void setBandItems(List<Character> items) {
        getItems().clear();
        getItems().addAll(items);
    }
}
