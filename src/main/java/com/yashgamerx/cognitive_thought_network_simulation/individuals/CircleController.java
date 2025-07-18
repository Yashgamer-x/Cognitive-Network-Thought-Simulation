package com.yashgamerx.cognitive_thought_network_simulation.individuals;

import com.yashgamerx.cognitive_thought_network_simulation.manager.ThoughtManager;
import com.yashgamerx.cognitive_thought_network_simulation.Tool;
import com.yashgamerx.cognitive_thought_network_simulation.Whiteboard;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Ellipse;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the visual and interactive behavior of a labeled circle node
 * on the cognitive whiteboard. Supports movement, label binding,
 * and integration with drawing tools like lines and erasers.
 */
public class CircleController {

    /** The container for the label and ellipse graphics. */
    @Getter
    @FXML private StackPane stackPane;

    /** The ellipse representing the circle background. */
    @Getter
    @FXML private Ellipse ellipse;

    /** Text label displayed in the circle. */
    @Getter @FXML
    private Label label;

    /** Stores the previous mouse position during drag events. */
    private double lastMouseX, lastMouseY;

    private List<Arrow> arrows;

    /** Called automatically by FXMLLoader after component creation. */
    @FXML private void initialize() {
        arrows = new ArrayList<>();
    }

    /**
     * Sets the label text and binds the ellipse radius
     * to match the label's dimensions.
     *
     * @param label the label text to display
     */
    public void setLabel(String label) {
        this.label.setText(label);
        ThoughtManager.createThought(label);
        ellipse.radiusXProperty().bind(this.label.widthProperty());
        ellipse.radiusYProperty().bind(this.label.heightProperty());
    }

    /**
     * Handles mouse press events to track position for dragging.
     * Only activates when no tool is selected.
     *
     * @param e the mouse press event
     */
    @FXML
    private void handleMousePressed(MouseEvent e) {
        if (Whiteboard.getInstance().getCurrentTool() == null) {
            lastMouseX = e.getSceneX();
            lastMouseY = e.getSceneY();
        }
    }

    /**
     * Enables drag behavior, moving the circle across the whiteboard.
     * Only works when no tool is selected.
     *
     * @param e the mouse drag event
     */
    @FXML
    private void handleMouseDragged(MouseEvent e) {
        if (Whiteboard.getInstance().getCurrentTool() == null) {
            double currentX = e.getSceneX();
            double currentY = e.getSceneY();

            double deltaX = currentX - lastMouseX;
            double deltaY = currentY - lastMouseY;

            stackPane.setLayoutX(stackPane.getLayoutX() + deltaX);
            stackPane.setLayoutY(stackPane.getLayoutY() + deltaY);

            lastMouseX = currentX;
            lastMouseY = currentY;
        }
    }

    /**
     * Handles mouse clicks based on the currently selected tool:
     * <ul>
     *   <li><b>LINE:</b> Starts or ends arrow drawing</li>
     *   <li><b>ERASER:</b> Deletes this node</li>
     * </ul>
     */
    @FXML
    private void handleMouseClicked() {
        var whiteboard = Whiteboard.getInstance();
        var currentTool = whiteboard.getCurrentTool();

        if (currentTool == Tool.LINE) {
            double centerX = stackPane.getLayoutX() + stackPane.getWidth() / 2;
            double centerY = stackPane.getLayoutY() + stackPane.getHeight() / 2;

            if (!whiteboard.isArrowing()) {
                whiteboard.startArrowDraw(centerX, centerY, this);
            } else {
                whiteboard.setCurrentArrowTransparency(false);
                whiteboard.endArrowDraw(centerX, centerY, this);
            }

        } else if (currentTool == Tool.ERASER) {
            whiteboard.removeChildrenObject(stackPane);
            var newArrows = new ArrayList<>(arrows);
            newArrows.parallelStream().forEach(Arrow::detachCircles);
            stackPane.getChildren().clear();
            arrows = null;
            ThoughtManager.removeThought(label.getText());
        }
    }

    public void addArrow(Arrow arrow) {
        arrows.add(arrow);
    }

    public void removeArrow(Arrow arrow) {
        arrows.remove(arrow);
    }
}