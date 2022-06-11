package figures;

import java.util.logging.Logger;

import figures.enums.LineType;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import utils.ColorFactory;

/**
 * Circle Figure containing a {@link javafx.scene.shape.Circle} as its
 * {@link Figure#shape}
 * @warning Since This class is also named "Circle", you'll need to use
 * (javafx.scene.shape.Circle) each time you need to acces to internal
 * {@link Figure#shape} casted as a {@link javafx.scene.shape.Circle}
 * @implSpec It is assumed that {@link Figure#shape} will always be non null
 * during the life cycle of a Circle.
 * @author davidroussel
 */
public class RoundedRectangle extends Figure
{
    /**
     * Instances counter (to be used in {@link Figure#instanceNumber}) of each
     * Circle.
     * @implNote No need to decrease {@link Figure#instanceNumber} in
     * {@link #finalize()}
     */
    private static int counter = 0;

    /**
     * Valued constructor to build a zero size Circle at point (x, y).
     * Used during Cicle construction with {@link MouseEvent}s
     * Calls super-constructor, sets {@link Figure#instanceNumber} then
     * {@link #createShape(double, double)} and attach {@link Figure#shape} to
     * {@link Figure#root}.
     * @param fillColor the fill color (or null if there is no fill color).
     * The fill color set in this circle shall be set from {@link ColorFactory}.
     * @param edgeColor the edge color (or null if there is no edge color)
     * The edge color set in this circle shall be set from {@link ColorFactory}.
     * @param lineType line type (Either {@link LineType#SOLID},
     * {@link LineType#DASHED} or {@link LineType#NONE}). If there is no edge
     * color provided the internal {@link #lineType} shall be set to
     * {@link LineType#NONE}
     * @param lineWidth line width of this circle. If there is no edge
     * color provided the internal {@link #lineType} shall be set to 0
     * @param parentLogger a parent logger used to initialize the current logger
     * @param x the initial x coordinate in the drawing panel where to create this circle
     * @param y the initial y coordinate in the drawing panel where to create this circle
     * @throws IllegalStateException if we try to set both fillColor and
     * edgecolor as nulls
     */
    public RoundedRectangle(Color fillColor,
                     Color edgeColor,
                     LineType lineType,
                     double lineWidth,
                     Logger parentLogger,
                     double x,
                     double y)
            throws IllegalStateException
    {
        super(fillColor, edgeColor, lineType, lineWidth, parentLogger);
        instanceNumber = counter++;
        createShape(x, y);
        root.getChildren().add(shape);
    }

    /**
     * Valued constructor to build a Circle at point (x, y) with specified radius
     * Calls super-constructor, sets {@link Figure#instanceNumber} then
     * {@link #createShape(double, double)} and attach {@link Figure#shape} to
     * {@link Figure#root}.
     * @param fillColor the fill color (or null if there is no fill color).
     * The fill color set in this circle shall be set from {@link ColorFactory}.
     * @param edgeColor the edge color (or null if there is no edge color)
     * The edge color set in this circle shall be set from {@link ColorFactory}.
     * @param lineType line type (Either {@link LineType#SOLID},
     * {@link LineType#DASHED} or {@link LineType#NONE}). If there is no edge
     * color provided the internal {@link #lineType} shall be set to
     * {@link LineType#NONE}
     * @param lineWidth line width of this circle. If there is no edge
     * color provided the internal {@link #lineType} shall be set to 0
     * @param parentLogger a parent logger used to initialize the current logger
     * @param x the initial x coordinate in the drawing panel where to create this circle
     * @param y the initial y coordinate in the drawing panel where to create this circle
     * @param width the initial width of the rectangle
     * @param height the initial height of the rectangle
     * @throws IllegalStateException if we try to set both fillColor and
     * edgecolor as nulls
     */
    public RoundedRectangle(Color fillColor,
                     Color edgeColor,
                     LineType lineType,
                     double lineWidth,
                     Logger parentLogger,
                     double x,
                     double y,
                     double width,
                     double height)
            throws IllegalStateException
    {
        this(fillColor, edgeColor, lineType, lineWidth, parentLogger, x, y);
        javafx.scene.shape.Rectangle rectangle = (javafx.scene.shape.Rectangle) shape;
        rectangle.setWidth(width);
        rectangle.setHeight(height);
        setArc(5.0);
    }

    /**
     * Copy constructor
     * @param figure the figure to be copied
     * @throws IllegalArgumentException if the provided figure is not a Circle
     */
    public RoundedRectangle(Figure figure) throws IllegalArgumentException
    {
        super(figure);
        if (!(figure instanceof Rectangle))
        {
            String message = "provided figure is not a Rectangle: "
                    + figure.getClass().getSimpleName();
            logger.severe(message);
            throw new IllegalArgumentException(message);
        }
        javafx.scene.shape.Rectangle figureRectangle = (javafx.scene.shape.Rectangle) figure.shape;
        shape = new javafx.scene.shape.Rectangle(figureRectangle.getWidth(),
                figureRectangle.getHeight());
        setArc(5.0);
        root.getChildren().add(shape);
        applyParameters(shape);
        setSelected(figure.selected);
    }

    /**
     * Convenience method to get internal {@link Figure#shape} casted as a
     * {@link javafx.scene.shape.Circle}
     * @return the internal {@link Figure#shape} casted as a
     * {@link javafx.scene.shape.Circle}
     */
    private javafx.scene.shape.Rectangle getRectangleShape()
    {
        return (javafx.scene.shape.Rectangle)shape;
    }

    /**
     * Center Point of this figure
     * @return the center point of this figure
     */
    @Override
    public Point2D getCenter()
    {
        javafx.scene.shape.Rectangle shapeRectangle = getRectangleShape();
        return new Point2D(shapeRectangle.getX() + shapeRectangle.getWidth()/2, shapeRectangle.getY() + shapeRectangle.getHeight()/2);
    }

    /**
     * Width of this figure
     * @return the width of this figure
     */
    @Override
    public double width()
    {
        return getRectangleShape().getWidth();
    }

    /**
     * Height of this figure
     * @return the width of this figure
     */
    @Override
    public double height()
    {
        return getRectangleShape().getHeight();
    }

    /**
     * Top left corner of this figure
     * @return the top left {@link Point2D} of this figure
     */
    @Override
    public Point2D topLeft()
    {
        return new Point2D(getRectangleShape().getX(), getRectangleShape().getY());
    }

    /**
     * Bottom right corner of this figure
     * @return the bottom right {@link Point2D} of this figure
     */
    @Override
    public Point2D bottomRight()
    {
        return new Point2D(getRectangleShape().getX() + width(), getRectangleShape().getY() + height());
    }


    /**
     * Creates actual {@link #shape} at specified position and apply
     * parameters
     * @param x the x coordinate of the initial point where to create the new shape
     * @param y the y coordinate of the initial point where to create the new shape
     * @post a new {@link #shape} has been created with a new
     * {@link #instanceNumber} with {@link #fillColor}, {@link #edgeColor},
     * {@link #lineType} & {@link #lineWidth} applied with
     * {@link #applyParameters(Shape)}
     */
    @Override
    public void createShape(double x, double y)
    {
        /*
         * Note: since This class is also named Circle we need to explicitely
         * use "new javafx.scene.shape.Circle(...)" here
         */
        shape = new javafx.scene.shape.Rectangle(x, y, 0.0, 0.0);
        applyParameters(shape);
    }

    public double getArc() {
        return getRectangleShape().getArcHeight();
    }

    public void setArc(double angle) {
        getRectangleShape().setArcWidth(angle);
        getRectangleShape().setArcHeight(angle);
    }

    /**
     * Sets the last point of this figure.
     * Sets the radius of this Circle based on the distance between center and
     * the provided point
     * @param lastPoint the point used to set this Circle's radius
     */
    @Override
    public void setLastPoint(Point2D lastPoint)
    {
        getRectangleShape().setWidth(Math.abs(lastPoint.getX() - getRectangleShape().getX()));
        getRectangleShape().setHeight(Math.abs(lastPoint.getY() - getRectangleShape().getY()));
        setArc(0.1*width());
    }

    /**
     * Creates a copy of this circle (with the same name and instance number)
     * @return A distinct copy of this circle
     */
    @Override
    public Figure clone()
    {
        return new Rectangle(this);
    }

    /**
     * Compare this circle to another figure
     * @return true if the other figure is also a Circle with the same
     * position and size (with {@link Figure#threshold}), false otherwise.
     * Other parameters, such as {@link Figure#fillColor},
     * {@link Figure#edgeColor}, {@link Figure#lineType},
     * {@link Figure#lineWidth}, and transformations
     * are checked in {@link Figure#equals(Object)}
     */
    @Override
    protected boolean equals(Figure figure)
    {
        if (!(figure instanceof Rectangle))
        {
            return false;
        }

        Rectangle rectangle = (Rectangle) figure;

        if (Math.abs(getCenter().distance(rectangle.getCenter())) > Figure.threshold)
        {
            return false;
        }

        if (Math.abs(width() - rectangle.width()) > Figure.threshold)
        {
            return false;
        }

        if (Math.abs(height() - rectangle.height()) > Figure.threshold)
        {
            return false;
        }

        return true;
    }
}
