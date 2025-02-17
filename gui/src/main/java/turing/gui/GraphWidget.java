package turing.gui;

import guru.nidi.graphviz.attribute.Attributes;
import guru.nidi.graphviz.attribute.ForLink;
import guru.nidi.graphviz.attribute.ForNode;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.Rasterizer;
import guru.nidi.graphviz.engine.Renderer;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.MutableNode;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Pair;
import turing.machine.Settings;
import turing.machine.Transition;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

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
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static turing.gui.Gui.addOnCloseListener;
import static turing.gui.Gui.initComponent;
import static turing.gui.Gui.runInFxApplicationThread;
import static turing.machine.Msg.msg;

public class GraphWidget extends StackPane {
    private static final String LAYOUT = "graph_widget.fxml";

    private static final Attributes<ForLink> LINK_SELECTED_ATTRS = attrs(
            attr("color", "#01637a"), attr("fillcolor", "#d6f7ff"), attr("fontcolor", "#01637a"));
    private static final Attributes<ForNode> NODE_SELECTED_ATTRS = attrs(
            attr("color", "#01637a"), attr("fillcolor", "#d6f7ff"), attr("fontcolor", "#01637a"),
            attr("style", "filled"));
    private static final Attributes<ForLink> LINK_CLEAR_ATTRS = attrs(
            attr("color", null), attr("fillcolor", null), attr("fontcolor", null));
    private static final Attributes<ForNode> NODE_CLEAR_ATTRS = attrs(
            attr("color", null), attr("fillcolor", null), attr("fontcolor", null), attr("style", null));

    private static final ExecutorService renderingThread = Executors.newSingleThreadExecutor();

    static {
        renderingThread.execute(() -> Graphviz.useEngine(new GraphvizHackEngine(false)));
        addOnCloseListener(renderingThread::shutdownNow);
    }

    private final Object lock = new Object();

    @FXML
    private ImageView imageView;

    @FXML
    private ProgressIndicator progressIndicator;

    // sync access from any thread
    private boolean isRendering;
    private Runnable renderTask;

    // access only from rendering thread
    private Map<String, MutableNode> nodeMap;
    private Map<String, Map<String, Link>> linkMap;
    private Runnable deselectCallback;
    private Renderer renderer;

    public GraphWidget() {
        initComponent(this, LAYOUT);
        clear();
    }

    public void init(Settings settings) {
        imageView.setImage(null);
        imageView.setVisible(false);
        progressIndicator.setVisible(true);
        runInRenderingThread(() -> {
            clearInRenderingThread();
            initGraphInRenderingThread(settings);
            drawInRenderingThread();
        });
    }

    public void select(String state) {
        synchronized (lock) {
            renderTask = () -> {
                selectNodeInRenderingThread(nodeMap.get(state));
                drawInRenderingThread();
            };
        }
        renderNext();
    }

    public void select(Transition transition) {
        synchronized (lock) {
            renderTask = () -> {
                selectEdgeInRenderingThread(linkMap.get(transition.fromState()).get(transition.toState()));
                drawInRenderingThread();
            };
        }
        renderNext();
    }

    public void clear() {
        runInRenderingThread(this::clearInRenderingThread);
        imageView.setImage(null);
        imageView.setVisible(false);
        progressIndicator.setVisible(false);
    }

    private void clearInRenderingThread() {
        deselectInRenderingThread();
        nodeMap = null;
        linkMap = null;
        renderer = null;
    }

    private void renderNext() {
        var task = nextTask();
        if (task == null) {
            return;
        }
        runInRenderingThread(wrapRenderTask(task));
    }

    private Runnable wrapRenderTask(Runnable task) {
        return () -> {
            synchronized (lock) {
                isRendering = true;
            }
            task.run();
            synchronized (lock) {
                isRendering = false;
            }
            renderNext();
        };
    }

    private Runnable nextTask() {
        Runnable nextTask = null;
        synchronized (lock) {
            if (!isRendering) {
                nextTask = renderTask;
                renderTask = null;
            }
        }
        return nextTask;
    }

    private void selectNodeInRenderingThread(MutableNode node) {
        deselectInRenderingThread();
        node.add(NODE_SELECTED_ATTRS);
        deselectCallback = () -> node.add(NODE_CLEAR_ATTRS);
    }

    private void selectEdgeInRenderingThread(Link link) {
        deselectInRenderingThread();
        link.attrs().add(LINK_SELECTED_ATTRS);
        deselectCallback = () -> link.attrs().add(LINK_CLEAR_ATTRS);
    }

    private void deselectInRenderingThread() {
        if (deselectCallback != null) {
            deselectCallback.run();
            deselectCallback = null;
        }
    }

    private void initGraphInRenderingThread(Settings settings) {
        var nodeMap = settings.states().stream()
                .collect(toMap(identity(), s -> mutNode(s).add(
                        attr("shape", settings.finalStates().contains(s) ? "doublecircle" : "circle"))));
        var linkMap = settings.transitions().stream()
                .collect(groupingBy(t -> new Pair<>(t.fromState(), t.toState())))
                .entrySet().stream()
                .collect(groupingBy(e -> e.getKey().getKey(),
                        toMap(e -> e.getKey().getValue(), e -> nodeMap.get(e.getKey().getKey()).addLink(
                                        Link.to(nodeMap.get(e.getKey().getValue()))
                                                .add(attr("label", e.getValue().stream()
                                                        .map(GraphWidget::edgeLabel).collect(joining("\n")))))
                                .links().getLast())));
        var graph = mutGraph().setDirected(true)
                .graphAttrs().add(pad(0.1), dir(LEFT_TO_RIGHT), sizeMax(484, 360))
                .add(mutNode("#")
                        .add(attr("shape", "point"), attr("label", ""))
                        .addLink(Link.to(nodeMap.get(settings.startState()))))
                .add(new ArrayList<>(nodeMap.values()));
        this.nodeMap = nodeMap;
        this.linkMap = linkMap;
        renderer = fromGraph(graph).width((int) getMaxWidth()).rasterize(Rasterizer.SALAMANDER);
    }

    private void drawInRenderingThread() {
        var image = renderImageInRenderingThread();
        runInFxApplicationThread(() -> {
            progressIndicator.setVisible(false);
            imageView.setVisible(true);
            imageView.setImage(image);
        });
    }

    private Image renderImageInRenderingThread() {
        try {
            var outputStream = new ByteArrayOutputStream(1 << 15);
            renderer.toOutputStream(outputStream);
            return new Image(new ByteArrayInputStream(outputStream.toByteArray()));
        } catch (Throwable e) {
            return null;
        }
    }

    private void runInRenderingThread(Runnable runnable) {
        try {
            renderingThread.execute(runnable);
        } catch (RejectedExecutionException e) {
            System.err.println(msg(e));
        }
    }

    private static String edgeLabel(Transition t) {
        return format("(%s, %s, %s)", t.readChar(), t.writeChar(), t.moveDir());
    }
}
