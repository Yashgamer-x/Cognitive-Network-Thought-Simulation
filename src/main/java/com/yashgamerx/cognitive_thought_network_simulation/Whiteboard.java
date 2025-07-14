package com.yashgamerx.cognitive_thought_network_simulation;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import lombok.Getter;

/**
 * Manages an interactive whiteboard UI that supports drawing arrows, circles,
 * scrolling, and tool selection for cognitive thought network simulations.
 */
public class Whiteboard {

    /** Singleton instance of Whiteboard. */
    @Getter
    private static Whiteboard instance;

    @FXML private ScrollPane scrollPane;
    @FXML private Pane whiteboard;

    /** Currently selected drawing tool. */
    @Getter
    private Tool currentTool;

    private double lastMouseX, lastMouseY;

    /** Indicates if an arrow is actively being drawn. */
    @Getter
    private boolean arrowing;

    private Arrow currentArrow;

    private final double BUFFER = 200;
    private final double GROWTH_CHUNK = 1000;
    private final double MAX_CANVAS_SIZE = 100_000;

    /** Initializes whiteboard properties and sets up instance reference. */
    @FXML
    public void initialize() {
        whiteboard.setMinSize(4000, 4000);
        currentTool = null;
        arrowing = false;
        currentArrow = null;
        instance = this;
    }

    /**
     * Handles mouse click events based on the selected tool.
     *
     * @param e MouseEvent containing click information
     */
    @FXML
    private void handleMouseClicked(MouseEvent e) {
        if (currentTool == null) return;

        var startX = e.getX();
        var startY = e.getY();
        switch (currentTool) {
            case CIRCLE -> onCircleDraw(startX, startY);
        }
    }

    private void bindStartArrow(CircleController circleController) {
        var stackPane = circleController.getStackPane();
        var line = currentArrow.getLine();
        line.startXProperty().bind(stackPane.layoutXProperty().add(stackPane.getWidth() / 2));
        line.startYProperty().bind(stackPane.layoutYProperty().add(stackPane.getHeight() / 2));
    }

    private void bindEndArrow(CircleController circleController) {
        var stackPane = circleController.getStackPane();
        var line = currentArrow.getLine();
        line.endXProperty().bind(stackPane.layoutXProperty().add(stackPane.getWidth() / 2));
        line.endYProperty().bind(stackPane.layoutYProperty().add(stackPane.getHeight() / 2));
    }

    /** Starts arrow drawing on the whiteboard and binds its start to a thought circle. */
    public void startArrowDraw(double startX, double startY, CircleController circleController) {
        currentArrow = new Arrow(startX, startY, startX, startY);
        bindStartArrow(circleController);
        whiteboard.getChildren().addFirst(currentArrow);
        arrowing = true;
    }

    /** Sets the transparency behavior of the current arrow. */
    public void setCurrentArrowTransparency(boolean transparency) {
        currentArrow.setMouseTransparent(transparency);
    }

    /** Removes a graphical object from the whiteboard. */
    public void removeChildrenObject(Object object) {
        whiteboard.getChildren().remove(object);
    }

    /** Ends the arrow drawing and binds its end to a thought circle. */
    public void endArrowDraw(double endX, double endY, CircleController circleController) {
        currentArrow.setEnd(endX, endY);
        bindEndArrow(circleController);
        currentArrow = null;
        arrowing = false;
    }

    private void onCircleDraw(double startX, double startY) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Whiteboard.class.getResource("fxml/Circle_Label.fxml"));
            var stackPane = (StackPane) fxmlLoader.load();
            var controller = (CircleController) fxmlLoader.getController();
            if (!TextInputDialogForCircleClass.dialog(controller)) return;

            // Expand whiteboard if needed
            if (startX + BUFFER > whiteboard.getPrefWidth()) {
                whiteboard.setPrefWidth(Math.min(MAX_CANVAS_SIZE, startX + BUFFER + GROWTH_CHUNK));
            }
            if (startY + BUFFER > whiteboard.getPrefHeight()) {
                whiteboard.setPrefHeight(Math.min(MAX_CANVAS_SIZE, startY + BUFFER + GROWTH_CHUNK));
            }

            stackPane.setLayoutX(startX);
            stackPane.setLayoutY(startY);
            whiteboard.getChildren().add(stackPane);
        } catch (Exception _) {
            System.out.println("Unable to load Circle_Label.fxml");
        }
    }

    /** Captures mouse press coordinates for scroll tracking. */
    @FXML
    private void handleMousePressed(MouseEvent e) {
        if (e.getButton() == MouseButton.MIDDLE || e.getButton() == MouseButton.SECONDARY) {
            lastMouseX = e.getSceneX();
            lastMouseY = e.getSceneY();
        }
    }

    private void manageScrollOnDrag(double x, double y) {
        Point2D current = new Point2D(x, y);
        double deltaX = lastMouseX - current.getX();
        double deltaY = lastMouseY - current.getY();
        lastMouseX = current.getX();
        lastMouseY = current.getY();

        // Horizontal scroll
        double scrollableWidth = scrollPane.getContent().getBoundsInLocal().getWidth() - scrollPane.getViewportBounds().getWidth();
        if (scrollableWidth > 0) {
            scrollPane.setHvalue(scrollPane.getHvalue() + deltaX / scrollableWidth);
        }

        // Vertical scroll
        double scrollableHeight = scrollPane.getContent().getBoundsInLocal().getHeight() - scrollPane.getViewportBounds().getHeight();
        if (scrollableHeight > 0) {
            scrollPane.setVvalue(scrollPane.getVvalue() + deltaY / scrollableHeight);
        }
    }

    /**
     * Enables scroll behavior when dragging with middle or secondary mouse button.
     *
     * @param e MouseEvent with drag details
     */
    @FXML
    private void handleMouseDragged(MouseEvent e) {
        if (e.getButton() == MouseButton.MIDDLE || e.getButton() == MouseButton.SECONDARY) {
            manageScrollOnDrag(e.getSceneX(), e.getSceneY());
        }
    }

    /** Updates current arrow position to follow mouse movement. */
    @FXML
    private void handleMouseMoved(MouseEvent e) {
        if (currentTool == Tool.LINE && arrowing && currentArrow != null) {
            currentArrow.setEnd(e.getX(), e.getY());
        }
    }

    /**
     * Retrieves the ImageView associated with a tool from the UI.
     *
     * @param tool tool to look up
     * @return ImageView node
     */
    private ImageView imageViewLookup(Tool tool) {
        var scene = MainApplication.getStage().getScene();
        return switch (tool) {
            case LINE -> (ImageView) scene.lookup("#line");
            case CIRCLE -> (ImageView) scene.lookup("#circle");
            case ERASER -> (ImageView) scene.lookup("#eraser");
            default -> null;
        };
    }

    /** Unequips the current tool and resets its icon scale. */
    private void unequipTool() {
        if (currentTool != null) {
            stopDrawing();
            var imageView = imageViewLookup(currentTool);
            imageView.setScaleX(1.0);
            imageView.setScaleY(1.0);
        }
    }

    /**
     * Equips the given tool and scales its ImageView icon.
     *
     * @param tool tool to equip
     */
    private void equipTool(Tool tool) {
        if (currentTool == tool) {
            currentTool = null;
            return;
        }
        var imageView = imageViewLookup(tool);
        if (imageView != null) {
            imageView.setScaleX(1.2);
            imageView.setScaleY(1.2);
        }
        currentTool = tool;
    }

    /** Cancels arrow drawing and removes the active arrow from whiteboard. */
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

    /** Clears all children from the whiteboard (like using a trash tool). */
    @FXML
    private void userTrash() {
        whiteboard.getChildren().clear();
    }
}