package com.yashgamerx.cognitive_thought_network_simulation.individuals;

import com.yashgamerx.cognitive_thought_network_simulation.Tool;
import com.yashgamerx.cognitive_thought_network_simulation.Whiteboard;
import com.yashgamerx.cognitive_thought_network_simulation.controller.CircleController;
import com.yashgamerx.cognitive_thought_network_simulation.manager.ThoughtManager;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a directed arrow on the whiteboard.
 * <br>
 * The arrow consists of a straight Line (shaft) and a triangular Polygon (head).
 * It can bind its endpoints dynamically to CircleController nodes, so that
 * the arrow tip always touches the boundary of an ellipse shape.
 * It listens for the eraser tool to detach itself automatically.
 */
@Getter
public class Arrow extends Group {

    /** Main shaft of the arrow. */
    private final Line line;

    /** Triangular tip of the arrow. */
    private Polygon arrowHead;

    /** Controller for the circle at the arrow's start point. */
    @Setter
    private CircleController startNode;

    /** Controller for the circle at the arrow's end point. */
    @Setter
    private CircleController endNode;

    /**
     * Constructs an Arrow between the given coordinates.
     *
     * @param startX starting x-coordinate
     * @param startY starting y-coordinate
     * @param endX   ending x-coordinate
     * @param endY   ending y-coordinate
     */
    public Arrow(double startX, double startY, double endX, double endY) {
        // Initialize the line shaft
        this.line = new Line(startX, startY, endX, endY);
        this.line.setStrokeWidth(2);

        // Create the initial arrowhead polygon
        this.arrowHead = createArrowHead(startX, startY, endX, endY);

        // Add both shapes to this Group
        getChildren().addAll(line, arrowHead);

        // Redraw the arrowhead whenever the line moves
        line.startXProperty().addListener((_,_,_) -> updateArrowHead());
        line.startYProperty().addListener((_,_,_) -> updateArrowHead());
        line.endXProperty().addListener((_,_,_) -> updateArrowHead());
        line.endYProperty().addListener((_,_,_) -> updateArrowHead());

        // Let clicks pass through unless eraser is active
        setMouseTransparent(true);
        setOnMousePressed(_ -> detachCircles());
    }

    /**
     * Fully detaches this arrow from its start and end CircleControllers.
     * Removes itself from both circle's arrow lists.
     */
    public void detachCircles() {
        if(Whiteboard.getInstance().getCurrentTool() == Tool.ERASER){
            Whiteboard.getInstance().removeChildrenObject(this);
            startNode.removeOutgoingArrow(this);
            endNode.removeIncomingArrow(this);
            ThoughtManager.disconnectThought(startNode.getLabel().getText(), endNode.getLabel().getText());
            startNode = endNode = null;
        }
    }

    /**
     * Binds this arrow's end point to the perimeter of the given circle controller.
     * Calculates a dynamic DoubleBinding so that the arrow always touches the ellipse boundary.
     *
     * @param circleController the target CircleController to bind the arrow tip to
     */
    public void bindEndNode(CircleController circleController) {
        this.endNode = circleController;

        // Prevent self-referencing arrow; cancel if start == end
        if (endNode == startNode) {
            var wb = Whiteboard.getInstance();
            wb.removeChildrenObject(this);
            wb.setArrowing(false);
            wb.setCurrentArrow(null);
            return;
        }

        // Local references for clarity
        var pane    = circleController.getStackPane();
        var ellipse = circleController.getEllipse();
        var line    = this.line;

        // 1) Compute the dynamic center of the circle (in parent coordinates)
        DoubleBinding centerX = pane.layoutXProperty()
                .add(pane.widthProperty().divide(2));
        DoubleBinding centerY = pane.layoutYProperty()
                .add(pane.heightProperty().divide(2));

        // 2) Compute the dynamic endX: point on ellipse perimeter along the line direction
        DoubleBinding endX = Bindings.createDoubleBinding(() -> {
                    double sx = line.getStartX();
                    double sy = line.getStartY();
                    double cx = centerX.get();
                    double cy = centerY.get();

                    double dx = cx - sx;
                    double dy = cy - sy;
                    double dist = Math.hypot(dx, dy);
                    if (dist == 0) return cx;  // avoid division by zero

                    // Move from ellipse center toward start point by radiusX
                    return cx - (dx / dist) * ellipse.getRadiusX();
                },
                line.startXProperty(), line.startYProperty(),
                pane.layoutXProperty(), pane.widthProperty(),
                ellipse.radiusXProperty(), centerX, centerY);

        // 3) Compute endY similarly, using radiusY
        DoubleBinding endY = Bindings.createDoubleBinding(() -> {
                    double sx = line.getStartX();
                    double sy = line.getStartY();
                    double cx = centerX.get();
                    double cy = centerY.get();

                    double dx = cx - sx;
                    double dy = cy - sy;
                    double dist = Math.hypot(dx, dy);
                    if (dist == 0) return cy;

                    return cy - (dy / dist) * ellipse.getRadiusY();
                },
                line.startXProperty(), line.startYProperty(),
                pane.layoutYProperty(), pane.heightProperty(),
                ellipse.radiusYProperty(), centerX, centerY);

        // 4) Bind the line’s end properties
        line.endXProperty().bind(endX);
        line.endYProperty().bind(endY);

        // 5) Ensure the arrowhead follows binding updates
        endX.addListener((_, _, _) -> updateArrowHead());
        endY.addListener((_, _, _) -> updateArrowHead());

        // Register this arrow with both circles
        startNode.addOutgoingArrow(this);
        endNode.addIncomingArrow(this);

        ThoughtManager.connectThought(startNode.getLabel().getText(), endNode.getLabel().getText());
    }

    /**
     * Creates a triangular arrowhead Polygon at the given line direction.
     *
     * @param startX the x-coordinate of the line start
     * @param startY the y-coordinate of the line start
     * @param endX   the x-coordinate where the arrowhead should point
     * @param endY   the y-coordinate where the arrowhead should point
     * @return a filled black Polygon for the arrowhead
     */
    private Polygon createArrowHead(double startX, double startY, double endX, double endY) {
        final double arrowLength = 10;
        final double arrowWidth = 7;

        // Calculate angle and direction unit vector
        double dx = endX - startX;
        double dy = endY - startY;
        double angle = Math.atan2(dy, dx);
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);

        // Tip point
        double x1 = endX;
        double y1 = endY;
        // Base corners of the triangle
        double x2 = endX - arrowLength * cos + arrowWidth * sin;
        double y2 = endY - arrowLength * sin - arrowWidth * cos;
        double x3 = endX - arrowLength * cos - arrowWidth * sin;
        double y3 = endY - arrowLength * sin + arrowWidth * cos;

        Polygon head = new Polygon(x1, y1, x2, y2, x3, y3);
        head.setFill(Color.BLACK);
        return head;
    }

    /**
     * Updates the ending coordinates of the arrow shaft.
     *
     * @param x new end x-coordinate
     * @param y new end y-coordinate
     */
    public void setEnd(double x, double y) {
        line.setEndX(x);
        line.setEndY(y);
        updateArrowHead();
    }

    /**
     * Recomputes and replaces the current arrowhead shape
     * based on the current line endpoints.
     */
    public void updateArrowHead() {
        // Remove old head
        getChildren().remove(arrowHead);
        // Create and add a new one
        arrowHead = createArrowHead(
                line.getStartX(),
                line.getStartY(),
                line.getEndX(),
                line.getEndY()
        );
        getChildren().add(arrowHead);
    }
}