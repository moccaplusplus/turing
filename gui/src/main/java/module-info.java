module turing.gui {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires guru.nidi.graphviz;
    requires turing.machine;
    opens turing.gui to javafx.fxml;
    exports turing.gui;
}