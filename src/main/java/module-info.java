module com.yashgamerx.cognitive_thought_network_simulation {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;


    opens com.yashgamerx.cognitive_thought_network_simulation to javafx.fxml, javafx.graphics;
    exports com.yashgamerx.cognitive_thought_network_simulation;
    exports com.yashgamerx.cognitive_thought_network_simulation.individuals;
    opens com.yashgamerx.cognitive_thought_network_simulation.individuals to javafx.fxml, javafx.graphics;
    exports com.yashgamerx.cognitive_thought_network_simulation.storage;
    opens com.yashgamerx.cognitive_thought_network_simulation.storage to javafx.fxml, javafx.graphics;
    exports com.yashgamerx.cognitive_thought_network_simulation.manager;
    opens com.yashgamerx.cognitive_thought_network_simulation.manager to javafx.fxml, javafx.graphics;
    exports com.yashgamerx.cognitive_thought_network_simulation.controller;
    opens com.yashgamerx.cognitive_thought_network_simulation.controller to javafx.fxml, javafx.graphics;
}