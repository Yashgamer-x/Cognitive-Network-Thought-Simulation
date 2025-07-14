package com.yashgamerx.cognitive_thought_network_simulation;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import lombok.Getter;

/**This Class creates Arrow <br/>
 * Usage:
 * <PRE>
 * import com.yashgamerx.cognitive_thought_network_simulation.*;
 *
 * Arrow arrow = new Arrow(double startX, double startY, double endX, double endY);
 *
 * </PRE> */
@Getter
public class Arrow extends Group {
    private final Line line;
    private Polygon arrowHead;

    private final DoubleProperty startX;
    private final DoubleProperty startY;
    private final DoubleProperty endX;
    private final DoubleProperty endY;

    /**Constructor setting the line's starting and ending point, width is set to 2px and creates arrowhead*/
    public Arrow(double startX, double startY, double endX, double endY) {
        //Initialization of observable property
        this.startX = new SimpleDoubleProperty(startX);
        this.startY = new SimpleDoubleProperty(startY);
        this.endX = new SimpleDoubleProperty(endX);
        this.endY = new SimpleDoubleProperty(endY);

        //Initialization of Line
        this.line = new Line();
        this.line.setStrokeWidth(2);

        // Bind line endpoints to properties
        line.startXProperty().bind(this.startX);
        line.startYProperty().bind(this.startY);
        line.endXProperty().bind(this.endX);
        line.endYProperty().bind(this.endY);

        this.arrowHead = createArrowHead(startX, startY, endX, endY);
        getChildren().addAll(line, arrowHead);
        setMouseTransparent(true);
        this.setOnMousePressed(_ -> {
            detachThis();
        });

        // Auto-update arrowhead when any endpoint changes
        this.startX.addListener(_-> updateArrowHead());
        this.startY.addListener(_-> updateArrowHead());
        this.endX.addListener(_ -> updateArrowHead());
        this.endY.addListener(_ -> updateArrowHead());
    }

    private void detachThis() {
        if(Whiteboard.getInstance().getCurrentTool() == Tool.ERASER)
            Whiteboard.getInstance().removeChildrenObject(this);
    }

    /**Creates triangular arrowhead*/
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


    // Optional: setters if you want to change position dynamically
    public void setStart(double x, double y) {
        line.setStartX(x);
        line.setStartY(y);
        updateArrowHead();
    }

    /**Sets the endX value of the line and updates the arrow head position and angle*/
    public void setEnd(double x, double y) {
        line.setEndX(x);
        line.setEndY(y);
        updateArrowHead();
    }

    /**Removes old arrowhead from the list of children and adds new */
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

    public DoubleProperty startXProperty() { return startX; }
    public DoubleProperty startYProperty() { return startY; }
    public DoubleProperty endXProperty() { return endX; }
    public DoubleProperty endYProperty() { return endY; }

}
