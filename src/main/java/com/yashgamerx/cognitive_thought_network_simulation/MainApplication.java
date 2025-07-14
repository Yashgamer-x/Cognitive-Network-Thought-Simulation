package com.yashgamerx.cognitive_thought_network_simulation;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;

public class MainApplication extends Application {
    @Getter
    private static Stage stage;

    @Override
    public void start(Stage stage){
        MainApplication.stage = stage;
        Scene scene=null;
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(Whiteboard.class.getResource("fxml/Whiteboard.fxml"));
            scene = new Scene(fxmlLoader.load(), 320, 240);
        } catch (Exception e) {
            System.out.println("Unable to load Whiteboard.fxml");
        }
        stage.setTitle("Cognitive Thought Network Simulation");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}