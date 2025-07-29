package com.yashgamerx.cognitive_thought_network_simulation.ui;

import com.yashgamerx.cognitive_thought_network_simulation.MainApplication;
import com.yashgamerx.cognitive_thought_network_simulation.dialogbox.TextInputDialogClass;
import com.yashgamerx.cognitive_thought_network_simulation.enums.Tool;
import com.yashgamerx.cognitive_thought_network_simulation.individuals.Arrow;
import com.yashgamerx.cognitive_thought_network_simulation.controller.CircleController;
import com.yashgamerx.cognitive_thought_network_simulation.manager.MySQLManager;
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

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

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

    /** Padding beyond the click point before expanding the canvas. */
    private static final double BUFFER = 200;

    /** Increment by which to grow the canvas when needed. */
    private static final double GROWTH_CHUNK = 1000;

    /** Upper bound on canvas size to prevent unbounded growth. */
    private static final double MAX_CANVAS_SIZE = 100_000;

    private static final Logger log = Logger.getLogger(Whiteboard.class.getName());

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
     * Loads thought nodes and their connecting arrows from the database
     * and instantiates their UI representations on the whiteboard.
     *
     * <p>First, retrieves circle node records via
     * MySQLManager.getCircleNodeResultSet() and for each record:
     * <ul>
     *   <li>Extracts the label text, layout X, and layout Y coordinates.</li>
     *   <li>Loads the Circle_Label.fxml component with FXMLLoader.</li>
     *   <li>Adds the resulting StackPane to the whiteboard.</li>
     *   <li>Positions the node and sets its label text.</li>
     * </ul>
     *
     * <p>Then, retrieves arrow records via MySQLManager.getArrowResultSet() and for each record:
     * <ul>
     *   <li>Reads the source and target node labels.</li>
     *   <li>Obtains the corresponding CircleController instances from ThoughtManager.</li>
     *   <li>Registers the logical connection via ThoughtManager.connectThought().</li>
     *   <li>On the JavaFX Application Thread, draws the arrow start and end
     *       using startArrowDraw() and loadEndArrow().</li>
     * </ul>
     *
     * @throws SQLException if a database access error occurs while fetching nodes or arrows
     * @throws IOException  if loading the Circle_Label.fxml resource fails
     */
    public void loadNodes() throws SQLException, IOException {
        try(var circleNodeResultSet  = MySQLManager.getCircleNodeResultSet()){
            while(circleNodeResultSet.next()){
                var labelText = circleNodeResultSet.getString("label_text");
                var layoutX = circleNodeResultSet.getDouble("layout_x");
                var layoutY = circleNodeResultSet.getDouble("layout_y");

                FXMLLoader loader = new FXMLLoader(
                        MainApplication.class.getResource("fxml/Circle_Label.fxml")
                );
                StackPane pane = loader.load();
                CircleController circleController = loader.getController();
                whiteboard.getChildren().add(pane);
                circleController.getStackPane().setLayoutX(layoutX);
                circleController.getStackPane().setLayoutY(layoutY);
                circleController.setLabelText(labelText);
            }
        }
        try(var arrowResultSet  = MySQLManager.getArrowResultSet()){
            while(arrowResultSet.next()){
                var sourceNodeLabel = arrowResultSet.getString("source_node_label");
                var targetNodeLabel = arrowResultSet.getString("target_node_label");
                var sourceNodeController = ThoughtManager.getThoughtNode(sourceNodeLabel);
                var targetNodeController = ThoughtManager.getThoughtNode(targetNodeLabel);
                ThoughtManager.connectThought(sourceNodeLabel, targetNodeLabel);
                Platform.runLater(()->{
                    startArrowDraw(sourceNodeController.getCircleController());
                    loadEndArrow(targetNodeController.getCircleController());
                });
            }
        }
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
        MySQLManager.createArrow(currentArrow);
        currentArrow = null;
        arrowing = false;
    }

    private void loadEndArrow(CircleController circleController) {
        var stackPane = circleController.getStackPane();
        double endX = stackPane.getLayoutX() + stackPane.getWidth() / 2;
        double endY = stackPane.getLayoutY() + stackPane.getHeight() / 2;
        setCurrentArrowTransparency(false);
        currentArrow.setEnd(endX, endY);
        bindEndArrow(circleController);
        currentArrow = null;
        arrowing = false;
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
            CircleController circleController = loader.getController();

            // Prompt for label text; if canceled, abort
            if (!TextInputDialogClass.circleDialog(circleController)) {
                return;
            }

            // Expand the whiteboard if clicking near the edge
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
            MySQLManager.createCircleNode(circleController);
            whiteboard.getChildren().add(pane);

        } catch (Exception ex) {
            log.severe("Unable to load Circle_Label.fxml");
        }
    }

    /**
     * Captures the initial mouse press position to prepare for panning.
     *
     * <p>This FXML event handler activates when the middle or secondary
     * (right) mouse button is pressed. It records the current scene X and Y
     * coordinates into {@code lastMouseX} and {@code lastMouseY}, which are
     * then used as reference points for subsequent drag-based scrolling.</p>
     *
     * @param e the MouseEvent representing the mouse press with scene coordinates
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
     * Pans the ScrollPane content in response to mouse drag movement.
     *
     * <p>This method computes the horizontal and vertical deltas between
     * the current mouse coordinates and the last recorded mouse position.
     * It then updates the last mouse coordinates and, if the content
     * dimensions exceed the viewport dimensions, adjusts the scrollPane’s
     * horizontal (hvalue) and vertical (vvalue) scroll positions proportionally.</p>
     *
     * @param x the current mouse X position in scene coordinates
     * @param y the current mouse Y position in scene coordinates
     */
    private void manageScrollOnDrag(double x, double y) {
        Point2D curr = new Point2D(x, y);
        double deltaX = lastMouseX - curr.getX();
        double deltaY = lastMouseY - curr.getY();

        lastMouseX = curr.getX();
        lastMouseY = curr.getY();

        double contentWidth   = scrollPane.getContent().getBoundsInLocal().getWidth();
        double viewportWidth  = scrollPane.getViewportBounds().getWidth();
        double contentHeight  = scrollPane.getContent().getBoundsInLocal().getHeight();
        double viewportHeight = scrollPane.getViewportBounds().getHeight();

        if (contentWidth > viewportWidth) {
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
     * Enables panning of the whiteboard when dragging with the middle or right mouse button.
     *
     * <p>If the user drags the mouse while holding either the middle
     * (MouseButton.MIDDLE) or secondary/right button
     * (MouseButton.SECONDARY), this event handler delegates to
     * {@link #manageScrollOnDrag(double, double)} to adjust the view
     * based on the current scene coordinates.</p>
     *
     * @param e the MouseEvent containing the current drag coordinates in scene space
     */
    @FXML
    private void handleMouseDragged(MouseEvent e) {
        if (e.getButton() == MouseButton.MIDDLE ||
                e.getButton() == MouseButton.SECONDARY) {
            manageScrollOnDrag(e.getSceneX(), e.getSceneY());
        }
    }

    /**
     * Follows the mouse pointer to update the current arrow’s end point during drawing.
     *
     * <p>This FXML event handler is active only when the LINE tool is selected,
     * an arrow drawing is in progress (arrowing == true), and a currentArrow exists.
     * As the mouse moves, the arrow’s end coordinates are updated to the cursor position.</p>
     *
     * @param e MouseEvent containing the current cursor coordinates
     */
    @FXML
    private void handleMouseMoved(MouseEvent e) {
        if (currentTool == Tool.LINE &&
                arrowing &&
                currentArrow != null) {
            currentArrow.setEnd(e.getX(), e.getY());
        }
    }

    /**
     * Retrieves the ImageView icon for the specified drawing tool from the main scene.
     *
     * <p>The lookup uses the tool’s fx:id selector:
     * <ul>
     *   <li>LINE → "#line"</li>
     *   <li>CIRCLE → "#circle"</li>
     *   <li>ERASER → "#eraser"</li>
     * </ul>
     * If the node is not found or isn’t an ImageView, this method returns null.</p>
     *
     * @param tool the Tool enum identifying which icon to fetch
     * @return the ImageView for the given tool, or null if no matching node exists
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
     * Deselects the currently active tool, aborts any in-progress drawing,
     * and resets the tool’s icon scale back to its default.
     *
     * <p>If there is no tool equipped, this method does nothing.</p>
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
     * Toggles the specified tool as the active tool.
     * <ul>
     *   <li>If the tool passed in is already selected, it will be unequipped.</li>
     *   <li>If a new tool is selected, its icon is scaled up to indicate activation.</li>
     * </ul>
     *
     * @param tool the Tool to equip or toggle off if it’s already the current tool
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
            MySQLManager.deleteAllCircleNodes();
            MySQLManager.deleteAllArrows();
        }
    }

    /**
     * Opens a dialog for the user to enter and execute a custom query.
     *
     * <p>This method is bound to an FXML control (e.g., a menu item or button)
     * and delegates to the TextInputDialogClass to display a text input
     * dialog. User input handling and actual query execution are performed
     * within the dialog class.</p>
     *
     * @see TextInputDialogClass#queryDialog()
     */
    @FXML
    private void userQuery() {
        TextInputDialogClass.queryDialog();
    }

    /**
     * Loads all nodes from the database into the application and
     * disables the "load" icon once complete.
     *
     * <p>This method is bound to an FXML control (e.g., a button or toolbar
     * icon). It calls {@link #loadNodes()} to fetch and render nodes,
     * then locates the ImageView with fx:id="load" in the current scene,
     * disabling and hiding it to prevent repeated loads.</p>
     *
     * @throws RuntimeException if loading nodes fails due to a
     *                          {@link java.sql.SQLException} or
     *                          {@link java.io.IOException}
     */
    @FXML
    private void databaseLoad() {
        try {
            loadNodes();
            var scene = MainApplication.getStage().getScene();
            var imageView  =(ImageView) scene.lookup("#load");
            imageView.setDisable(true);
            imageView.setVisible(false);
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}