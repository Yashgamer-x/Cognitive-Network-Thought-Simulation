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

public class Whiteboard {
    @Getter
    private static Whiteboard instance;
    @FXML private ScrollPane scrollPane;
    @FXML private Pane whiteboard;
    @Getter
    private Tool currentTool;
    private double lastMouseX, lastMouseY;
    @Getter
    private boolean arrowing;
    private Arrow currentArrow;
    private final double BUFFER = 200;
    private final double GROWTH_CHUNK = 1000;
    private final double MAX_CANVAS_SIZE = 100_000;

    @FXML
    public void initialize(){
        whiteboard.setMinSize(4000, 4000); // Large whiteboard
        currentTool = null;
        arrowing = false;
        currentArrow = null;
        instance = this;
    }

    /**
     * <PRE>
     * Options:
     * Draw Line
     * Draw Circle (requires name of thought)</PRE>*/
    @FXML
    private void handleMouseClicked(MouseEvent e) {
        if(currentTool == null) return;
        var startX = e.getX();
        var startY = e.getY();
        switch (currentTool){
//            case LINE -> onLineDraw(startX, startY);
            case CIRCLE -> onCircleDraw(startX, startY);
        }
    }

    private void bindStartArrow(CircleController circleController){
        var stackPane = circleController.getStackPane();
        var centerWidth = stackPane.getWidth()/2;
        var centerHeight = stackPane.getHeight()/2;
        var line = currentArrow.getLine();
        line.startXProperty().bind(stackPane.layoutXProperty().add(centerWidth));
        line.startYProperty().bind(stackPane.layoutYProperty().add(centerHeight));
    }

    private void bindEndArrow(CircleController circleController){
        var stackPane = circleController.getStackPane();
        var centerWidth = stackPane.getWidth()/2;
        var centerHeight = stackPane.getHeight()/2;
        var line = currentArrow.getLine();
        line.endXProperty().bind(stackPane.layoutXProperty().add(centerWidth));
        line.endYProperty().bind(stackPane.layoutYProperty().add(centerHeight));
    }

    public void startArrowDraw(double startX, double startY, CircleController circleController){
        currentArrow = new Arrow(startX, startY, startX, startY);
        bindStartArrow(circleController);
        whiteboard.getChildren().addFirst(currentArrow);
        arrowing = true;
    }

    public void setCurrentArrowTransparency(boolean transparency){
        currentArrow.setMouseTransparent(transparency);
    }

    public void removeChildrenObject(Object object){
        whiteboard.getChildren().remove(object);
    }

    public void endArrowDraw(double endX, double endY, CircleController circleController){
        currentArrow.setEnd(endX, endY);
        bindEndArrow(circleController);
        currentArrow = null;
        arrowing = false;
    }

    private void onCircleDraw(double startX, double startY){
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(Whiteboard.class.getResource("fxml/Circle_Label.fxml"));
            var stackPane= (StackPane)fxmlLoader.load();
            var controller = (CircleController)fxmlLoader.getController();
            if(!TextInputDialogForCircleClass.dialog(controller)) return;

            // 🌟 Expand pane if needed
            double requiredWidth = startX + BUFFER;
            double requiredHeight = startY + BUFFER;

            if (requiredWidth > whiteboard.getPrefWidth()) {
                double newWidth = Math.min(MAX_CANVAS_SIZE, requiredWidth + GROWTH_CHUNK);
                whiteboard.setPrefWidth(newWidth);
            }
            if (requiredHeight > whiteboard.getPrefHeight()) {
                double newHeight = Math.min(MAX_CANVAS_SIZE, requiredHeight + GROWTH_CHUNK);
                whiteboard.setPrefHeight(newHeight);
            }

            stackPane.setLayoutX(startX);
            stackPane.setLayoutY(startY);

            whiteboard.getChildren().add(stackPane);
        } catch (Exception _) {
            System.out.println("Unable to load Whiteboard.fxml");
        }
    }

    /**Stores the X and Y value when the mouse button is pressed*/
    @FXML
    private void handleMousePressed(MouseEvent e){
        if(e.getButton() == MouseButton.MIDDLE || e.getButton() == MouseButton.SECONDARY){
            lastMouseX = e.getSceneX();
            lastMouseY = e.getSceneY();
        }
    }

    private void manageScrollOnDrag(double x, double y){
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
     * Handles Mouse Drag events <br>
     * Drag using secondary button or middle button will be considered as scrolling
     * */
    @FXML
    private void handleMouseDragged(MouseEvent e){
        if(e.getButton() == MouseButton.MIDDLE || e.getButton() == MouseButton.SECONDARY){
            manageScrollOnDrag(e.getSceneX(), e.getSceneY());
        }
    }

    /**Directs the arrow when mouse is moved*/
    @FXML
    private void handleMouseMoved(MouseEvent e){
        if (currentTool == Tool.LINE && arrowing && currentArrow != null) {
            currentArrow.setEnd(e.getX(), e.getY()); // Follow mouse
        }
    }

    /** Looks up for the ImageView based on current used tool
     * @param tool The tool users want to search for
     * @return ImageView*/
    private ImageView imageViewLookup(Tool tool){
        var scene = MainApplication.getStage().getScene();
        switch (tool) {
            case LINE -> {
                return (ImageView) scene.lookup("#line");
            }
            case CIRCLE -> {
                return (ImageView) scene.lookup("#circle");
            }
            case ERASER -> {
                return (ImageView) scene.lookup("#eraser");
            }
            default -> {
                return null;
            }
        }
    }

    /**To unequip the tool, it shrinks the ImageView scale back to 1.0*/
    private void unequipTool(){
        if(currentTool!=null){
            stopDrawing();
            var imageView = imageViewLookup(currentTool);
            imageView.setScaleX(1.0);
            imageView.setScaleY(1.0);
        }
    }

    /**Equips the mentioned tool if It's not currently equipped.<br/>
     *  Clicking the same equipped tool again will unequip it*/
    private void equipTool(Tool tool){
        if(currentTool == tool) {
            currentTool = null;
            return;
        }
        var imageView = imageViewLookup(tool);
        if(imageView!=null){
            imageView.setScaleX(1.2);
            imageView.setScaleY(1.2);
        }
        currentTool = tool;
    }

    private void stopDrawing(){
        if(currentTool == Tool.LINE) {
            arrowing = false;
            if(currentArrow != null) {
                whiteboard.getChildren().remove(currentArrow);
                currentArrow = null;
            }
        }
    }

    /**This sets the current tool to line. <br>
     * If the current tool is line, and the arrow is being drawn, clicking this tool will remove arrow. <br>
     * Explanation: This allows user to unselect the accidental drawing.
     * */
    @FXML
    private void useLine() {
        unequipTool();
        stopDrawing();
        equipTool(Tool.LINE);
    }

    /**Unequips other tools and equips circle*/
    @FXML
    private void useCircle() {
        unequipTool();
        equipTool(Tool.CIRCLE);
    }

    /**Unequips other tools and equips eraser*/
    @FXML
    private void useEraser() {
        unequipTool();
        equipTool(Tool.ERASER);
    }

    /**Unequips other tools and equips trash*/
    @FXML
    private void userTrash() {
        whiteboard.getChildren().clear();
    }
}