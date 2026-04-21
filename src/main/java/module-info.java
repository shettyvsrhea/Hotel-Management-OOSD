module HotelManagement {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;

    opens application to javafx.fxml, javafx.graphics, javafx.controls, javafx.base;
    
    exports application;
}