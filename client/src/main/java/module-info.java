module com.example.musicplayer5 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires com.google.gson;
    requires java.management;


    opens com.example.musicplayer5 to javafx.fxml;
    exports com.example.musicplayer5;
}