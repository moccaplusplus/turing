package turing.gui;

import guru.nidi.graphviz.attribute.Attributes;
import guru.nidi.graphviz.attribute.ForLink;
import guru.nidi.graphviz.attribute.ForNode;
import guru.nidi.graphviz.engine.Rasterizer;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import turing.machine.Settings;
import turing.machine.Transition;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Supplier;

import static guru.nidi.graphviz.attribute.Attributes.attr;
import static guru.nidi.graphviz.attribute.Attributes.attrs;
import static guru.nidi.graphviz.attribute.GraphAttr.pad;
import static guru.nidi.graphviz.attribute.GraphAttr.sizeMax;
import static guru.nidi.graphviz.attribute.Rank.RankDir.LEFT_TO_RIGHT;
import static guru.nidi.graphviz.attribute.Rank.dir;
import static guru.nidi.graphviz.engine.Graphviz.fromGraph;
import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;
import static java.lang.String.format;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
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

    private Map<String, MutableNode> nodeMap;
    private Map<String, Map<Character, Link>> linkMap;
    private Supplier<Image> viz;
    private Runnable deselectCallback;

    public void init(Settings settings, Runnable callback) {
        clear();
        runInBackgroundThread(() -> {
            initGraph(settings);
            draw();
            runInFxApplicationThread(callback);
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
            nodeMap = null;
            linkMap = null;
            viz = null;
            setImage(null);
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
        var nodeMap = settings.states().stream()
                .collect(toMap(identity(), s -> mutNode(s).add(
                        attr("shape", settings.finalStates().contains(s) ? "doublecircle" : "circle"))));
        var linkMap = settings.transitions().stream()
                .collect(groupingBy(Transition::fromState,
                        toMap(Transition::readChar, t -> nodeMap.get(t.fromState()).addLink(
                                Link.to(nodeMap.get(t.toState())).add(attr("label", edgeLabel(t)))).links().getLast())));
        var graph = mutGraph().setDirected(true)
                .graphAttrs().add(pad(0.1), dir(LEFT_TO_RIGHT), sizeMax((int) getFitWidth()))
                .add(mutNode("#")
                        .add(attr("shape", "point"), attr("label", ""))
                        .addLink(Link.to(nodeMap.get(settings.startState())).with(attr("minlen", 0.1))))
                .add(new ArrayList<>(nodeMap.values()));
        this.nodeMap = nodeMap;
        this.linkMap = linkMap;
        viz = viz(graph, (int) getFitWidth());
    }

    private void draw() {
        var image = viz.get();
        runInFxApplicationThread(() -> setImage(image));
    }

    private static String edgeLabel(Transition t) {
        return format("(%s, %s, %s)", t.readChar(), t.writeChar(), t.moveDir());
    }

    private Supplier<Image> viz(MutableGraph graph, int size) {
        var outputStream = new ByteArrayOutputStream(1 << 15);
        var renderer = fromGraph(graph).width(size).rasterize(Rasterizer.SALAMANDER);
        return () -> {
            try {
                renderer.toOutputStream(outputStream);
                return new Image(new ByteArrayInputStream(outputStream.toByteArray()));
            } catch (Exception e) {
                return null;
            } finally {
                outputStream.reset();
            }
        };
    }
}
