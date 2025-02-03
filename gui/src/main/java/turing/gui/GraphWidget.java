package turing.gui;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import turing.machine.Settings;
import turing.machine.Transition;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.SinglePixelPackedSampleModel;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.Objects;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static turing.gui.Gui.runOnFxApplicationThread;

public class GraphWidget extends ImageView {
    private static final String TRANSITION_FORMAT = "%s -> %s[label=\"(%s, %s, %s)\", color=\"%s\"];";
    private static final String NODE_FORMAT = "%s [color=\"%s\"];";
    private static final String GRAPH_FORMAT = "digraph { %s %s }";
    private static final String NORMAL_COLOR = "black";
    private static final String SELECTED_COLOR = "red";

    public void draw(Settings settings) {
        draw(settings.transitions(), settings.states(), null, null);
    }

    public void draw(Settings settings, String state) {
        draw(settings.transitions(), settings.states(), state, null);
    }

    public void draw(Settings settings, Transition transition) {
        draw(settings.transitions(), settings.states(), null, transition);
    }

    public void clear() {
        draw(emptyList(), emptyList(), null, null);
    }

    private void draw(
            Collection<Transition> transitions, Collection<String> states, String state, Transition transition) {
        var nodes = states.stream()
                .map(s -> format(NODE_FORMAT, s, Objects.equals(state, s) ? SELECTED_COLOR : NORMAL_COLOR))
                .collect(joining());
        var edges = transitions.stream()
                .map(t -> format(TRANSITION_FORMAT,
                        t.fromState(), t.toState(), t.readChar(), t.writeChar(), t.moveDir(),
                        Objects.equals(transition, t) ? SELECTED_COLOR : NORMAL_COLOR))
                .collect(joining());
        var image = toImage(format(GRAPH_FORMAT, nodes, edges), (int) getFitWidth());
        runOnFxApplicationThread(() -> setImage(image));
    }

    private static Image toImage(String dot, int size) {
        var img = Graphviz.fromString(dot)
                .width(size).height(size)
                .render(Format.SVG)
                .toImage();
        return convertToFxImage(img);
    }

    private static Image convertToFxImage(BufferedImage bImg) {
        int bw = bImg.getWidth();
        int bh = bImg.getHeight();
        var wImg = new WritableImage(bw, bh);
        var pw = wImg.getPixelWriter();
        var db = (DataBufferInt) bImg.getRaster().getDataBuffer();
        var data = db.getData();
        int offset = bImg.getRaster().getDataBuffer().getOffset();
        int scan = bImg.getRaster().getSampleModel() instanceof SinglePixelPackedSampleModel sm ?
                sm.getScanlineStride() : 0;
        PixelFormat<IntBuffer> pf = (bImg.isAlphaPremultiplied() ?
                PixelFormat.getIntArgbPreInstance() :
                PixelFormat.getIntArgbInstance());
        pw.setPixels(0, 0, bw, bh, pf, data, offset, scan);
        return wImg;
    }
}
