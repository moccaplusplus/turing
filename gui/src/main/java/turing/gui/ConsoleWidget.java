package turing.gui;

import javafx.beans.NamedArg;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static turing.gui.Gui.initComponent;
import static turing.gui.Gui.runInFxApplicationThread;

public class ConsoleWidget extends BorderPane {
    private static final String LAYOUT = "console_widget.fxml";

    private final Object lock = new Object();
    private final List<String> contentLines;
    private final int maxLines;
    private boolean updatePending;

    @FXML
    private TextArea console;

    public ConsoleWidget(@NamedArg("maxLines") int maxLines) {
        initComponent(this, LAYOUT);
        this.maxLines = maxLines;
        contentLines = new ArrayList<>();
    }

    public void writeConsole(String text) {
        synchronized (lock) {
            contentLines.addAll(text.lines().toList());
            int size = contentLines.size();
            if (size > maxLines) {
                var lines = new ArrayList<>(contentLines.subList(size - maxLines, size));
                contentLines.clear();
                contentLines.addAll(lines);
            }
        }
        updateUI();
    }

    public void clearConsole() {
        synchronized (lock) {
            contentLines.clear();
        }
        updateUI();
    }

    private void updateUI() {
        synchronized (lock) {
            if (updatePending) {
                return;
            }
            updatePending = true;
        }
        runInFxApplicationThread(() -> {
            String text;
            synchronized (lock) {
                text = contentLines.stream().collect(joining(System.lineSeparator()));
                updatePending = false;
            }
            console.setText(text);
            console.positionCaret(text.length());
        });
    }
}
