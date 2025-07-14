package com.yashgamerx.cognitive_thought_network_simulation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Ellipse;
import lombok.Getter;

public class CircleController {
    @Getter
    @FXML private StackPane stackPane;
    @Getter
    @FXML private Ellipse ellipse;
    @FXML private Label label;
    private double lastMouseX, lastMouseY;


    @FXML private void initialize() {}

    public void setLabel(String label){
        this.label.setText(label);
        ellipse.radiusXProperty().bind(this.label.widthProperty());
        ellipse.radiusYProperty().bind(this.label.heightProperty());
    }


    @FXML
    private void handleMousePressed(MouseEvent e) {
        if(Whiteboard.getInstance().getCurrentTool() == null) {
            lastMouseX = e.getSceneX();
            lastMouseY = e.getSceneY();
        }
    }

    @FXML
    private void handleMouseDragged(MouseEvent e) {
        if(Whiteboard.getInstance().getCurrentTool() == null){
            double currentX = e.getSceneX();
            double currentY = e.getSceneY();

            double deltaX = currentX - lastMouseX;
            double deltaY = currentY - lastMouseY;

            // Move node
            stackPane.setLayoutX(stackPane.getLayoutX() + deltaX);
            stackPane.setLayoutY(stackPane.getLayoutY() + deltaY);

            // Update last mouse position
            lastMouseX = currentX;
            lastMouseY = currentY;
        }
    }

    @FXML
    private void handleMouseClicked() {
        var whiteboard = Whiteboard.getInstance();
        var currentTool = whiteboard.getCurrentTool();
        if(currentTool == Tool.LINE ){
            if(!whiteboard.isArrowing()){
                whiteboard.startArrowDraw(
                        stackPane.getLayoutX() + stackPane.getWidth()/2,
                        stackPane.getLayoutY() + stackPane.getHeight()/2,
                        this
                );
            }else{
                whiteboard.setCurrentArrowTransparency(false);
                whiteboard.endArrowDraw(
                        stackPane.getLayoutX() + stackPane.getWidth()/2,
                        stackPane.getLayoutY() + stackPane.getHeight()/2,
                        this
                );
            }
        }else if(currentTool == Tool.ERASER){
            whiteboard.removeChildrenObject(stackPane);
            stackPane.getChildren().clear();
        }
    }

}
