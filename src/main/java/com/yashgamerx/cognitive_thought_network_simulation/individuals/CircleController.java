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
 *   <li>Distinguishes incoming vs. outgoing arrows for directional awareness</li>
 * </ul>
 */
public class CircleController {

    /** Container for the ellipse and label graphics. */
    @Getter @FXML private StackPane stackPane;

    /** Ellipse shape forming the circle background. */
    @Getter @FXML private Ellipse ellipse;

    /** Text label displayed at the center of the circle. */
    @Getter @FXML private Label label;

    /** Last recorded mouse X coordinate during dragging. */
    private double lastMouseX;

    /** Last recorded mouse Y coordinate during dragging. */
    private double lastMouseY;

    /** List of arrows pointing into this node (incoming edges). */
    private List<Arrow> incomingArrows;

    /** List of arrows originating from this node (outgoing edges). */
    private List<Arrow> outgoingArrows;

    /**
     * Initializes the controller after FXML loading.
     * Sets up arrow tracking lists.
     */
    @FXML
    private void initialize() {
        incomingArrows = new ArrayList<>();
        outgoingArrows = new ArrayList<>();
    }

    /**
     * Sets the label text and binds the ellipse dimensions
     * to dynamically wrap the text label. Also registers the
     * thought name in the system memory.
     *
     * @param text the new label to apply
     */
    public void setLabel(String text) {
        label.setText(text);
        ThoughtManager.createThought(text);
        ellipse.radiusXProperty().bind(label.widthProperty());
        ellipse.radiusYProperty().bind(label.heightProperty());
    }

    /**
     * Records the starting position for dragging when no tool is selected.
     *
     * @param e mouse press event
     */
    @FXML
    private void handleMousePressed(MouseEvent e) {
        if (Whiteboard.getInstance().getCurrentTool() == null) {
            lastMouseX = e.getSceneX();
            lastMouseY = e.getSceneY();
        }
    }

    /**
     * Moves the circle node on drag, only when the active tool is null.
     * Computes delta movement based on the previous mouse position.
     *
     * @param e mouse drag event
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
     * Performs tool-based click handling:
     * <ul>
     *   <li>LINE: initiates or completes an arrow draw</li>
     *   <li>ERASER: deletes this circle and its arrows</li>
     * </ul>
     */
    @FXML
    private void handleMouseClicked() {
        Whiteboard whiteboard = Whiteboard.getInstance();
        Tool currentTool = whiteboard.getCurrentTool();

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

            // Detach all associated arrows safely in parallel
            new ArrayList<>(incomingArrows).parallelStream().forEach(Arrow::detachCircles);
            new ArrayList<>(outgoingArrows).parallelStream().forEach(Arrow::detachCircles);

            stackPane.getChildren().clear();
            incomingArrows = outgoingArrows = null;

            ThoughtManager.removeThought(label.getText());
        }
    }

    /**
     * Adds an incoming arrow reference to this node.
     * Typically called when another node targets this one.
     *
     * @param arrow the Arrow directed toward this node
     */
    public void addIncomingArrow(Arrow arrow) {
        incomingArrows.add(arrow);
    }

    /**
     * Adds an outgoing arrow reference from this node.
     * Typically called when this node targets another.
     *
     * @param arrow the Arrow originating from this node
     */
    public void addOutgoingArrow(Arrow arrow) {
        outgoingArrows.add(arrow);
    }

    /**
     * Removes an incoming arrow reference.
     *
     * @param arrow the Arrow to detach
     */
    public void removeIncomingArrow(Arrow arrow) {
        incomingArrows.remove(arrow);
    }

    /**
     * Removes an outgoing arrow reference.
     *
     * @param arrow the Arrow to detach
     */
    public void removeOutgoingArrow(Arrow arrow) {
        outgoingArrows.remove(arrow);
    }
}