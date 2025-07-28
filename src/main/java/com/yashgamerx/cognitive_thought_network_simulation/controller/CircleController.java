package com.yashgamerx.cognitive_thought_network_simulation.controller;

import com.yashgamerx.cognitive_thought_network_simulation.individuals.Arrow;
import com.yashgamerx.cognitive_thought_network_simulation.manager.ThoughtManager;
import com.yashgamerx.cognitive_thought_network_simulation.Tool;
import com.yashgamerx.cognitive_thought_network_simulation.ui.Whiteboard;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Ellipse;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for a labeled circle node on the cognitive whiteboard.
 *
 * <p>This class handles:
 * <ul>
 *   <li>Binding and resizing the Ellipse to match its Label text</li>
 *   <li>Drag‐and‐drop movement when no tool is active</li>
 *   <li>Click interactions for LINE, ERASER, or no‐tool (activation)</li>
 *   <li>Tracking incoming and outgoing Arrow connections</li>
 *   <li>Safe suppression of click events immediately after a drag</li>
 * </ul>
 */
public class CircleController {

    /** Flag set when a drag motion is detected, to swallow the subsequent click. */
    private boolean dragDetected;

    /** Container holding the ellipse shape and label node. */
    @Getter @FXML
    private StackPane stackPane;

    /** Ellipse graphic that serves as the circle background. */
    @Getter @FXML
    private Ellipse ellipse;

    /** Centered text label displayed on the circle. */
    @Getter @FXML
    private Label label;

    /** Last recorded X coordinate during a drag operation. */
    private double lastMouseX;

    /** Last recorded Y coordinate during a drag operation. */
    private double lastMouseY;

    /** List of arrows pointing into this node (incoming edges). */
    private List<Arrow> incomingArrows;

    /** List of arrows originating from this node (outgoing edges). */
    private List<Arrow> outgoingArrows;

    /**
     * Called once by FXMLLoader after all @FXML fields are injected.
     * Initializes arrow‐tracking lists and resets the drag flag.
     */
    @FXML
    private void initialize() {
        incomingArrows = new ArrayList<>();
        outgoingArrows = new ArrayList<>();
        dragDetected = false;
    }

    /**
     * Sets or updates this circle’s label text and registers the thought.
     * Also binds the ellipse radii so the circle always wraps the text.
     *
     * @param text the new label string to display
     */
    public void setLabel(String text) {
        label.setText(text);
        // Create/register thought in storage and associate this controller
        ThoughtManager.createThought(text, this);
        // Bind ellipse size to label dimensions
        ellipse.radiusXProperty().bind(label.widthProperty());
        ellipse.radiusYProperty().bind(label.heightProperty());
    }

    /**
     * Captures the mouse-down position if no tool is selected,
     * resetting the drag detection flag.
     *
     * @param e the mouse press event
     */
    @FXML
    private void handleMousePressed(MouseEvent e) {
        if (Whiteboard.getInstance().getCurrentTool() == null) {
            lastMouseX = e.getSceneX();
            lastMouseY = e.getSceneY();
            dragDetected = false;
        }
    }

    /**
     * Moves the circle by the drag delta if no tool is active.
     * Sets dragDetected to true once pixel movement exceeds zero.
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

            // Translate the StackPane by the drag offset
            stackPane.setLayoutX(stackPane.getLayoutX() + deltaX);
            stackPane.setLayoutY(stackPane.getLayoutY() + deltaY);

            lastMouseX = currentX;
            lastMouseY = currentY;
            dragDetected = true;
        }
    }

    /**
     * Handles click events according to the active tool:
     * <ul>
     *   <li>Suppress the click if a drag was just detected</li>
     *   <li>LINE: start or finish drawing an Arrow</li>
     *   <li>ERASER: delete this circle and detach its arrows</li>
     *   <li>null tool: activate the thought (flash & energy propagation)</li>
     * </ul>
     */
    @FXML
    private void handleMouseClicked() {
        // If this click immediately followed a drag, ignore it
        if (dragDetected) {
            dragDetected = false;
            return;
        }

        Whiteboard whiteboard = Whiteboard.getInstance();
        Tool currentTool = whiteboard.getCurrentTool();

        if (currentTool == Tool.LINE) {
            if (!whiteboard.isArrowing()) {
                whiteboard.startArrowDraw(this);
            } else {
                whiteboard.endArrowDraw(this);
            }

        } else if (currentTool == Tool.ERASER) {
            // Remove the visual node
            whiteboard.removeChildrenObject(stackPane);

            // Detach all arrows safely
            new ArrayList<>(incomingArrows)
                    .parallelStream()
                    .forEach(Arrow::detachCircles);
            new ArrayList<>(outgoingArrows)
                    .parallelStream()
                    .forEach(Arrow::detachCircles);

            stackPane.getChildren().clear();
            incomingArrows = outgoingArrows = null;

            ThoughtManager.removeThought(label.getText());

        } else if (currentTool == null) {
            // No tool => activate this thought
            // (fires energy propagation & triggers visual flash)
            ThoughtManager.activateThought(label.getText());
        }
    }

    /**
     * Registers an incoming Arrow that terminates at this circle.
     *
     * @param arrow the Arrow directed toward this node
     */
    public void addIncomingArrow(Arrow arrow) {
        incomingArrows.add(arrow);
    }

    /**
     * Registers an outgoing Arrow that originates from this circle.
     *
     * @param arrow the Arrow originating from this node
     */
    public void addOutgoingArrow(Arrow arrow) {
        outgoingArrows.add(arrow);
    }

    /**
     * Removes a previously registered incoming Arrow.
     *
     * @param arrow the Arrow to detach
     */
    public void removeIncomingArrow(Arrow arrow) {
        incomingArrows.remove(arrow);
    }

    /**
     * Removes a previously registered outgoing Arrow.
     *
     * @param arrow the Arrow to detach
     */
    public void removeOutgoingArrow(Arrow arrow) {
        outgoingArrows.remove(arrow);
    }
}