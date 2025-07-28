package com.yashgamerx.cognitive_thought_network_simulation.ui;

import com.yashgamerx.cognitive_thought_network_simulation.MainApplication;
import com.yashgamerx.cognitive_thought_network_simulation.dialogbox.TextInputDialogClass;
import com.yashgamerx.cognitive_thought_network_simulation.enums.Tool;
import com.yashgamerx.cognitive_thought_network_simulation.individuals.Arrow;
import com.yashgamerx.cognitive_thought_network_simulation.controller.CircleController;
import com.yashgamerx.cognitive_thought_network_simulation.manager.ThoughtManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import lombok.Setter;

/**
 * Manages an interactive whiteboard UI that supports:
 * - Drawing and binding arrows between circle nodes
 * - Placing and resizing labeled circles
 * - Panning via scroll‐drag
 * - Tool selection (line, circle, eraser, trash)
 */
public class Whiteboard {

    /** Singleton instance of Whiteboard. */
    @Getter
    private static Whiteboard instance;

    /** Scroll pane that wraps the whiteboard canvas for panning. */
    @FXML
    private ScrollPane scrollPane;

    /** Pane representing the infinite whiteboard canvas. */
    @FXML
    private Pane whiteboard;

    /** Currently selected drawing tool (LINE, CIRCLE, ERASER). */
    @Getter
    private Tool currentTool;

    /** Last recorded mouse X position for drag/pan calculations. */
    private double lastMouseX;

    /** Last recorded mouse Y position for drag/pan calculations. */
    private double lastMouseY;

    /** True while an arrow is mid‐draw (start set, end following cursor). */
    @Getter @Setter
    private boolean arrowing;

    /** Reference to the Arrow being drawn when in LINE mode. */
    @Setter
    private Arrow currentArrow;

    /** Padding beyond the click point before expanding canvas. */
    private static final double BUFFER = 200;

    /** Increment by which to grow the canvas when needed. */
    private static final double GROWTH_CHUNK = 1000;

    /** Upper bound on canvas size to prevent unbounded growth. */
    private static final double MAX_CANVAS_SIZE = 100_000;

    /**
     * Initializes the whiteboard after FXML loading.
     * - Sets a large minimum size
     * - Resets all tool/arrow state
     * - Registers this instance for global lookup
     */
    @FXML
    public void initialize() {
        whiteboard.setMinSize(4000, 4000);
        currentTool   = null;
        arrowing      = false;
        currentArrow  = null;
        instance      = this;
    }

    /**
     * Handles mouse clicks on the whiteboard area.
     * Delegates to specific draw actions based on the current tool.
     *
     * @param e the MouseEvent containing click coordinates
     */
    @FXML
    private void handleMouseClicked(MouseEvent e) {
        if (currentTool == null) {
            return;
        }

        double startX = e.getX();
        double startY = e.getY();

        switch (currentTool) {
            case CIRCLE ->
                    onCircleDraw(startX, startY);
            default ->
            {/* other tools handle clicks elsewhere */}
        }
    }

    /**
     * Binds the start point of the current arrow to the center of a circle.
     *
     * @param circleController controller of the circle at arrow’s origin
     */
    private void bindStartArrow(CircleController circleController) {
        StackPane cp = circleController.getStackPane();
        double halfW = cp.getWidth()  / 2;
        double halfH = cp.getHeight() / 2;

        currentArrow.getLine()
                .startXProperty()
                .bind(cp.layoutXProperty().add(halfW));
        currentArrow.getLine()
                .startYProperty()
                .bind(cp.layoutYProperty().add(halfH));
        currentArrow.setStartNode(circleController);
    }

    /**
     * Binds the end point of the current arrow to the center of a circle.
     *
     * @param circleController controller of the circle at arrow’s destination
     */
    private void bindEndArrow(CircleController circleController) {
        currentArrow.bindEndNode(circleController);
    }

    /**
     * Begins drawing a new arrow from a given point, binding its start to a circle.
     *
     * @param circleController controller of the circle at arrow’s start
     */
    public void startArrowDraw(CircleController circleController) {
        var stackPane = circleController.getStackPane();
        var startX = stackPane.getLayoutX() + stackPane.getWidth() / 2;
        var startY = stackPane.getLayoutY() + stackPane.getHeight() / 2;
        currentArrow = new Arrow(startX, startY, startX, startY);
        bindStartArrow(circleController);
        whiteboard.getChildren().addFirst(currentArrow);
        arrowing = true;
    }

    /**
     * Toggles whether the arrow under construction is transparent to mouse events.
     *
     * @param transparent true to ignore mouse events on the arrow
     */
    public void setCurrentArrowTransparency(boolean transparent) {
        currentArrow.setMouseTransparent(transparent);
    }

    /**
     * Removes an object (circle or arrow) from the whiteboard.
     *
     * @param object the node to remove
     */
    public void removeChildrenObject(Object object) {
        Platform.runLater(()->whiteboard.getChildren().remove(object));
    }

    /**
     * Finishes drawing the current arrow, binds its end to a circle, and clears state.
     *
     * @param circleController controller of the circle at arrow’s end
     */
    public void endArrowDraw(CircleController circleController) {
        var stackPane = circleController.getStackPane();
        double endX = stackPane.getLayoutX() + stackPane.getWidth() / 2;
        double endY = stackPane.getLayoutY() + stackPane.getHeight() / 2;
        setCurrentArrowTransparency(false);
        currentArrow.setEnd(endX, endY);
        bindEndArrow(circleController);
        currentArrow = null;
        arrowing      = false;
    }

    /**
     * Creates and places a labeled circle at the click position.
     * Expands the canvas if the new circle exceeds current bounds.
     *
     * @param startX x‐coordinate for the circle’s top‐left
     * @param startY y‐coordinate for the circle’s top‐left
     */
    private void onCircleDraw(double startX, double startY) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApplication.class.getResource("fxml/Circle_Label.fxml")
            );
            StackPane pane = loader.load();
            CircleController ctrl = loader.getController();

            // Prompt for label text; if canceled, abort
            if (!TextInputDialogClass.circleDialog(ctrl)) {
                return;
            }

            // Expand whiteboard if clicking near the edge
            if (startX + BUFFER > whiteboard.getPrefWidth()) {
                whiteboard.setPrefWidth(
                        Math.min(MAX_CANVAS_SIZE,
                                startX + BUFFER + GROWTH_CHUNK)
                );
            }
            if (startY + BUFFER > whiteboard.getPrefHeight()) {
                whiteboard.setPrefHeight(
                        Math.min(MAX_CANVAS_SIZE,
                                startY + BUFFER + GROWTH_CHUNK)
                );
            }

            pane.setLayoutX(startX);
            pane.setLayoutY(startY);
            whiteboard.getChildren().add(pane);

        } catch (Exception ex) {
            System.out.println("Unable to load Circle_Label.fxml");
        }
    }

    /**
     * Records the mouse position to support panning when drag begins.
     *
     * @param e the MouseEvent capturing initial press
     */
    @FXML
    private void handleMousePressed(MouseEvent e) {
        if (e.getButton() == MouseButton.MIDDLE ||
                e.getButton() == MouseButton.SECONDARY) {

            lastMouseX = e.getSceneX();
            lastMouseY = e.getSceneY();
        }
    }

    /**
     * Calculates and applies scroll deltas based on mouse movement.
     *
     * @param x current scene X
     * @param y current scene Y
     */
    private void manageScrollOnDrag(double x, double y) {
        Point2D curr = new Point2D(x, y);
        double deltaX = lastMouseX - curr.getX();
        double deltaY = lastMouseY - curr.getY();

        lastMouseX = curr.getX();
        lastMouseY = curr.getY();

        double contentWidth  = scrollPane.getContent()
                .getBoundsInLocal().getWidth();
        double viewportWidth = scrollPane.getViewportBounds().getWidth();
        double contentHeight = scrollPane.getContent()
                .getBoundsInLocal().getHeight();
        double viewportHeight= scrollPane.getViewportBounds().getHeight();

        if (contentWidth  > viewportWidth) {
            scrollPane.setHvalue(
                    scrollPane.getHvalue() + deltaX / (contentWidth - viewportWidth)
            );
        }
        if (contentHeight > viewportHeight) {
            scrollPane.setVvalue(
                    scrollPane.getVvalue() + deltaY / (contentHeight - viewportHeight)
            );
        }
    }

    /**
     * Handles drag events on the whiteboard; uses middle or right button for panning.
     *
     * @param e the MouseEvent with drag coordinates
     */
    @FXML
    private void handleMouseDragged(MouseEvent e) {
        if (e.getButton() == MouseButton.MIDDLE ||
                e.getButton() == MouseButton.SECONDARY) {

            manageScrollOnDrag(e.getSceneX(), e.getSceneY());
        }
    }

    /**
     * Updates the arrow’s end point to follow the mouse while drawing.
     *
     * @param e MouseEvent with current pointer location
     */
    @FXML
    private void handleMouseMoved(MouseEvent e) {
        if (currentTool == Tool.LINE &&
                arrowing        &&
                currentArrow   != null) {
            currentArrow.setEnd(e.getX(), e.getY());
        }
    }

    /**
     * Looks up the ImageView icon for a given tool in the main scene.
     *
     * @param tool enum value of the desired tool icon
     * @return the ImageView node, or null if not found
     */
    private ImageView imageViewLookup(Tool tool) {
        var scene = MainApplication.getStage().getScene();
        return switch (tool) {
            case LINE   -> (ImageView) scene.lookup("#line");
            case CIRCLE -> (ImageView) scene.lookup("#circle");
            case ERASER -> (ImageView) scene.lookup("#eraser");
        };
    }

    /**
     * Unequips the current tool, resets its icon scale, and aborts any in-progress draw.
     */
    private void unequipTool() {
        if (currentTool != null) {
            stopDrawing();
            ImageView iv = imageViewLookup(currentTool);
            iv.setScaleX(1.0);
            iv.setScaleY(1.0);
        }
    }

    /**
     * Equips the specified tool, scaling its icon to indicate selection.
     * Clicking the same tool again will unequip it.
     *
     * @param tool the Tool to select
     */
    private void equipTool(Tool tool) {
        if (currentTool == tool) {
            currentTool = null;
            return;
        }
        ImageView iv = imageViewLookup(tool);
        if (iv != null) {
            iv.setScaleX(1.2);
            iv.setScaleY(1.2);
        }
        currentTool = tool;
    }

    /**
     * Cancels any arrow currently being drawn and removes it from the board.
     */
    private void stopDrawing() {
        if (currentTool == Tool.LINE) {
            arrowing = false;
            if (currentArrow != null) {
                whiteboard.getChildren().remove(currentArrow);
                currentArrow = null;
            }
        }
    }

    /** Activates the line drawing tool or cancels if already active. */
    @FXML
    private void useLine() {
        unequipTool();
        stopDrawing();
        equipTool(Tool.LINE);
    }

    /** Activates the circle drawing tool. */
    @FXML
    private void useCircle() {
        unequipTool();
        equipTool(Tool.CIRCLE);
    }

    /** Activates the eraser tool. */
    @FXML
    private void useEraser() {
        unequipTool();
        equipTool(Tool.ERASER);
    }

    /** Clears all nodes from the whiteboard and thoughts, equivalent to a “trash” action. */
    @FXML
    private void userTrash() {
        if(TextInputDialogClass.confirmDialog()){
            ThoughtManager.clearThoughts();
            whiteboard.getChildren().clear();
        }
    }

    @FXML
    private void userQuery() {
        System.out.println("user query");
        TextInputDialogClass.queryDialog();
    }
}