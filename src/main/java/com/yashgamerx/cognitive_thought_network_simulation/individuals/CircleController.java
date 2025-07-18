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
 * Controls a labeled circle node on the cognitive whiteboard.
 *
 * <p>This controller manages:
 * <ul>
 *   <li>Binding and updating the label and ellipse size</li>
 *   <li>Drag‐and‐drop movement when no tool is active</li>
 *   <li>Integration with LINE and ERASER tools for drawing or deleting</li>
 *   <li>Tracking and disconnecting associated Arrow objects</li>
 * </ul>
 */
public class CircleController {

    /**
     * Container for the ellipse and label graphics.
     */
    @Getter @FXML
    private StackPane stackPane;

    /**
     * Ellipse shape forming the circle background.
     */
    @Getter @FXML
    private Ellipse ellipse;

    /**
     * Text label displayed at the center of the circle.
     */
    @Getter @FXML
    private Label label;

    /**
     * Last recorded mouse X coordinate during dragging.
     */
    private double lastMouseX;

    /**
     * Last recorded mouse Y coordinate during dragging.
     */
    private double lastMouseY;

    /**
     * List of arrows connected to this circle for easy cleanup.
     */
    private List<Arrow> arrows;

    /**
     * Called once by FXMLLoader after fields are injected.
     * Initializes the arrow list.
     */
    @FXML
    private void initialize() {
        arrows = new ArrayList<>();
    }

    /**
     * Sets the text label and adjusts ellipse radii to match the text size.
     * Also registers the thought name in the ThoughtManager.
     *
     * @param text the new label text to display
     */
    public void setLabel(String text) {
        // Update the displayed text
        label.setText(text);

        // Create or retrieve the thought in storage
        ThoughtManager.createThought(text);

        // Bind ellipse radii to label dimensions so circle wraps text
        ellipse.radiusXProperty().bind(label.widthProperty());
        ellipse.radiusYProperty().bind(label.heightProperty());
    }

    /**
     * Records the initial mouse position for dragging, but only when no tool is selected.
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
     * Moves the circle by the mouse drag delta, only when no tool is active.
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

            // Shift the stack pane by the drag delta
            stackPane.setLayoutX(stackPane.getLayoutX() + deltaX);
            stackPane.setLayoutY(stackPane.getLayoutY() + deltaY);

            // Update last positions for continuous dragging
            lastMouseX = currentX;
            lastMouseY = currentY;
        }
    }

    /**
     * Handles click actions based on the currently selected tool:
     * <ul>
     *   <li>LINE: start or finish drawing an Arrow</li>
     *   <li>ERASER: remove this node and disconnect arrows</li>
     * </ul>
     */
    @FXML
    private void handleMouseClicked() {
        Whiteboard whiteboard = Whiteboard.getInstance();
        Tool currentTool = whiteboard.getCurrentTool();

        if (currentTool == Tool.LINE) {
            // Compute the center coordinates of this circle
            double centerX = stackPane.getLayoutX() + stackPane.getWidth() / 2;
            double centerY = stackPane.getLayoutY() + stackPane.getHeight() / 2;

            if (!whiteboard.isArrowing()) {
                // Begin a new arrow from this node
                whiteboard.startArrowDraw(centerX, centerY, this);
            } else {
                // Finalize the arrow to this node
                whiteboard.setCurrentArrowTransparency(false);
                whiteboard.endArrowDraw(centerX, centerY, this);
            }

        } else if (currentTool == Tool.ERASER) {
            // Remove the visual node from the whiteboard
            whiteboard.removeChildrenObject(stackPane);

            // Detach all connected arrows in parallel
            new ArrayList<>(arrows)
                    .parallelStream()
                    .forEach(Arrow::detachCircles);

            // Clear children and null out references for GC
            stackPane.getChildren().clear();
            arrows = null;

            // Remove the thought from memory storage
            ThoughtManager.removeThought(label.getText());
        }
    }

    /**
     * Registers a newly created Arrow with this circle controller.
     *
     * @param arrow the Arrow to add
     */
    public void addArrow(Arrow arrow) {
        arrows.add(arrow);
    }

    /**
     * Unregisters a detached Arrow from this circle controller.
     *
     * @param arrow the Arrow to remove
     */
    public void removeArrow(Arrow arrow) {
        arrows.remove(arrow);
    }
}