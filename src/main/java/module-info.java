module com.yashgamerx.cognitive_thought_network_simulation {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;


    opens com.yashgamerx.cognitive_thought_network_simulation to javafx.fxml;
    exports com.yashgamerx.cognitive_thought_network_simulation;
}