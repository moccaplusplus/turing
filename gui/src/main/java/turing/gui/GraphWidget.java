package turing.gui;

import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.For;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.MutableAttributed;
import guru.nidi.graphviz.model.MutableNode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import turing.machine.Transition;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.SinglePixelPackedSampleModel;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static guru.nidi.graphviz.attribute.GraphAttr.pad;
import static guru.nidi.graphviz.attribute.GraphAttr.sizeMax;
import static guru.nidi.graphviz.attribute.Rank.RankDir.LEFT_TO_RIGHT;
import static guru.nidi.graphviz.attribute.Rank.dir;
import static guru.nidi.graphviz.engine.Graphviz.fromGraph;
import static guru.nidi.graphviz.model.Factory.graph;
import static java.lang.String.format;
import static turing.gui.Gui.runInBackgroundThread;
import static turing.gui.Gui.runInFxApplicationThread;

public class GraphWidget extends ImageView {
    private final Map<String, MutableNode> nodeMap = new HashMap<>();
    private final Map<String, Map<Character, Link>> linkMap = new HashMap<>();
    private Graph graph;
    private MutableAttributed<?, For> selected;

    public void init(Collection<Transition> transitions) {
        clear();
        runInBackgroundThread(() -> {
            initGraph(transitions);
            draw();
        });
    }

    public void select(String state) {
        runInBackgroundThread(() -> {
            select(nodeMap.get(state).attrs());
            draw();
        });
    }

    public void select(Transition transition) {
        runInBackgroundThread(() -> {
            select(linkMap.get(transition.fromState()).get(transition.readChar()).attrs());
            draw();
        });
    }

    public void clear() {
        runInBackgroundThread(() -> {
            nodeMap.clear();
            linkMap.clear();
            graph = null;
            selected = null;
            draw();
        });
    }

    @SuppressWarnings("unchecked")
    private void select(MutableAttributed<?, ? extends For> candidate) {
        if (selected != null) {
            selected.add(Color.BLACK);
            selected = null;
        }
        if (candidate != null) {
            selected = (MutableAttributed<?, For>) candidate;
            selected.add(Color.RED);
        }
    }

    private void initGraph(Collection<Transition> transitions) {
        for (var t : transitions) {
            var fromNode = nodeMap.computeIfAbsent(t.fromState(), Factory::mutNode);
            var toNode = nodeMap.computeIfAbsent(t.toState(), Factory::mutNode);
            var link = Link.to(toNode);
            link.attrs().add("label", edgeLabel(t));
            fromNode.addLink(link);
            linkMap.computeIfAbsent(t.fromState(), k -> new HashMap<>()).put(
                    t.readChar(), fromNode.links().getLast());
        }
        graph = graph().directed()
                .graphAttr().with(pad(0.1), dir(LEFT_TO_RIGHT), sizeMax((int) getFitWidth()))
                .with(new ArrayList<>(nodeMap.values()));
    }

    private void draw() {
        var image = graph == null ? null : toImage(graph, (int) getFitWidth());
        runInFxApplicationThread(() -> setImage(image));
    }

    private static String edgeLabel(Transition t) {
        return format("(%s, %s, %s)", t.readChar(), t.writeChar(), t.moveDir());
    }

    private static Image toImage(Graph graph, int size) {
        return convertToFxImage(fromGraph(graph).width(size).render(Format.PNG).toImage());
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
