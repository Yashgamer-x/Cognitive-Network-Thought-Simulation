package com.yashgamerx.cognitive_thought_network_simulation;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import lombok.Getter;

/**
 * Represents an arrow composed of a line and a triangular head.
 * <p>
 * Usage Example:
 * <pre>
 * Arrow arrow = new Arrow(startX, startY, endX, endY);
 * </pre>
 */
@Getter
public class Arrow extends Group {

    /** Main shaft of the arrow. */
    private final Line line;

    /** Triangular tip of the arrow. */
    private Polygon arrowHead;

    /**
     * Constructs an Arrow between the given coordinates.
     *
     * @param startX starting x-coordinate
     * @param startY starting y-coordinate
     * @param endX   ending x-coordinate
     * @param endY   ending y-coordinate
     */
    public Arrow(double startX, double startY, double endX, double endY) {
        this.line = new Line(startX, startY, endX, endY);
        this.line.setStrokeWidth(2);

        this.arrowHead = createArrowHead(startX, startY, endX, endY);
        getChildren().addAll(line, arrowHead);
        setMouseTransparent(true);

        this.setOnMousePressed(_ -> detachThis());
    }

    /**
     * Detaches the arrow from the whiteboard if eraser tool is active.
     */
    private void detachThis() {
        if (Whiteboard.getInstance().getCurrentTool() == Tool.ERASER) {
            Whiteboard.getInstance().removeChildrenObject(this);
        }
    }

    /**
     * Creates the triangular arrowhead based on the line direction.
     *
     * @param startX line start x-coordinate
     * @param startY line start y-coordinate
     * @param endX   line end x-coordinate
     * @param endY   line end y-coordinate
     * @return a Polygon representing the arrowhead
     */
    private Polygon createArrowHead(double startX, double startY, double endX, double endY) {
        double arrowLength = 10;
        double arrowWidth = 7;

        double dx = endX - startX;
        double dy = endY - startY;
        double angle = Math.atan2(dy, dx);

        double sin = Math.sin(angle);
        double cos = Math.cos(angle);

        double x1 = endX;
        double y1 = endY;
        double x2 = endX - arrowLength * cos + arrowWidth * sin;
        double y2 = endY - arrowLength * sin - arrowWidth * cos;
        double x3 = endX - arrowLength * cos - arrowWidth * sin;
        double y3 = endY - arrowLength * sin + arrowWidth * cos;

        Polygon arrowHead = new Polygon(x1, y1, x2, y2, x3, y3);
        arrowHead.setFill(Color.BLACK);
        return arrowHead;
    }

    /**
     * Sets a new starting point for the arrow.
     *
     * @param x new start x-coordinate
     * @param y new start y-coordinate
     */
    public void setStart(double x, double y) {
        line.setStartX(x);
        line.setStartY(y);
        updateArrowHead();
    }

    /**
     * Sets a new ending point for the arrow.
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
     * Recalculates and replaces the current arrowhead.
     */
    public void updateArrowHead() {
        getChildren().remove(arrowHead);
        arrowHead = createArrowHead(
                line.getStartX(),
                line.getStartY(),
                line.getEndX(),
                line.getEndY()
        );
        getChildren().add(arrowHead);
    }
}