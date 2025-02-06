package turing.gui;

import guru.nidi.graphviz.attribute.Attributes;
import guru.nidi.graphviz.attribute.ForLink;
import guru.nidi.graphviz.attribute.ForNode;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.MutableNode;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static guru.nidi.graphviz.attribute.Attributes.attr;
import static guru.nidi.graphviz.attribute.Attributes.attrs;
import static guru.nidi.graphviz.attribute.GraphAttr.pad;
import static guru.nidi.graphviz.attribute.GraphAttr.sizeMax;
import static guru.nidi.graphviz.attribute.Rank.RankDir.LEFT_TO_RIGHT;
import static guru.nidi.graphviz.attribute.Rank.dir;
import static guru.nidi.graphviz.engine.Graphviz.fromGraph;
import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.mutNode;
import static java.lang.String.format;
import static turing.gui.Gui.runInBackgroundThread;
import static turing.gui.Gui.runInFxApplicationThread;

public class GraphWidget extends ImageView {
    private static final Attributes<ForLink> LINK_SELECTED_ATTRS = attrs(
            attr("color", "#01637a"), attr("fillcolor", "#d6f7ff"), attr("fontcolor", "#01637a"));
    private static final Attributes<ForNode> NODE_SELECTED_ATTRS = attrs(
            attr("color", "#01637a"), attr("fillcolor", "#d6f7ff"), attr("fontcolor", "#01637a"),
            attr("style", "filled"));
    private static final Attributes<ForLink> LINK_CLEAR_ATTRS = attrs(
            attr("color", null), attr("fillcolor", null), attr("fontcolor", null));
    private static final Attributes<ForNode> NODE_CLEAR_ATTRS = attrs(
            attr("color", null), attr("fillcolor", null), attr("fontcolor", null), attr("style", null));

    private final Map<String, MutableNode> nodeMap = new HashMap<>();
    private final Map<String, Map<Character, Link>> linkMap = new HashMap<>();
    private Graph graph;
    private Runnable deselectCallback;

    public void init(Settings settings) {
        clear();
        runInBackgroundThread(() -> {
            initGraph(settings);
            draw();
        });
    }

    public void select(String state) {
        runInBackgroundThread(() -> {
            selectNode(nodeMap.get(state));
            draw();
        });
    }

    public void select(Transition transition) {
        runInBackgroundThread(() -> {
            selectEdge(linkMap.get(transition.fromState()).get(transition.readChar()));
            draw();
        });
    }

    public void clear() {
        runInBackgroundThread(() -> {
            deselect();
            nodeMap.clear();
            linkMap.clear();
            graph = null;
            draw();
        });
    }

    private void selectNode(MutableNode node) {
        deselect();
        node.add(NODE_SELECTED_ATTRS);
        deselectCallback = () -> node.add(NODE_CLEAR_ATTRS);
    }

    private void selectEdge(Link link) {
        deselect();
        link.attrs().add(LINK_SELECTED_ATTRS);
        deselectCallback = () -> link.attrs().add(LINK_CLEAR_ATTRS);
    }

    private void deselect() {
        if (deselectCallback != null) {
            deselectCallback.run();
            deselectCallback = null;
        }
    }

    private void initGraph(Settings settings) {
        for (var s : settings.states()) {
            nodeMap.put(s, mutNode(s).add(
                    attr("shape", settings.finalStates().contains(s) ? "doublecircle" : "circle")));
        }
        for (var t : settings.transitions()) {
            var fromNode = nodeMap.get(t.fromState()).addLink(
                    Link.to(nodeMap.get(t.toState())).add(attr("label", edgeLabel(t))));
            linkMap.computeIfAbsent(t.fromState(), k -> new HashMap<>()).put(
                    t.readChar(), fromNode.links().getLast());
        }
        graph = graph().directed()
                .graphAttr().with(pad(0.1), dir(LEFT_TO_RIGHT), sizeMax((int) getFitWidth()))
                .with(mutNode("#")
                        .add(attr("shape", "point"), attr("label", ""))
                        .addLink(Link.to(nodeMap.get(settings.startState())).with(attr("minlen", 0.1))))
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
