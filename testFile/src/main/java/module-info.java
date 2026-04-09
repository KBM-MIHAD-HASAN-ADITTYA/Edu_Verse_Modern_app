module com.example.testfile {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens com.example.testfile to javafx.fxml;
    exports com.example.testfile;
}