module turing.gui {
    requires java.desktop;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires guru.nidi.graphviz;
    requires turing.machine;
    requires svgSalamander;
    requires jsr305;
    opens turing.gui to javafx.fxml;
    exports turing.gui;
}