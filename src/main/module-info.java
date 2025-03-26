module org.example {
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires java.sql;
    requires org.controlsfx.controls;
    requires kordamp.ikonli.javafx;
    requires kordamp.bootstrapfx.core;


    opens org.example to javafx.graphics, javafx.fxml, javafx.base, javafx.controls;
    exports org.example;
    exports main;
    opens main to javafx.graphics, javafx.fxml, javafx.base, javafx.controls;


}