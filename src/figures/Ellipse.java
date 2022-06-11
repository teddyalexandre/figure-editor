package figures;

import java.util.logging.Logger;

import figures.Figure;
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
public class Ellipse extends Figure
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
    public Ellipse(Color fillColor,
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
     * @param radiusX the initial radius on x axis of the ellipse
     * @param radiusY same on y axis
     * @throws IllegalStateException if we try to set both fillColor and
     * edgecolor as nulls
     */
    public Ellipse(Color fillColor,
                  Color edgeColor,
                  LineType lineType,
                  double lineWidth,
                  Logger parentLogger,
                  double x,
                  double y,
                  double radiusX, double radiusY)
            throws IllegalStateException
    {
        this(fillColor, edgeColor, lineType, lineWidth, parentLogger, x, y);
        javafx.scene.shape.Ellipse ellipse = (javafx.scene.shape.Ellipse) shape;
        ellipse.setRadiusX(Math.abs(radiusX));
        ellipse.setRadiusY(Math.abs(radiusY));
    }

    /**
     * Copy constructor
     * @param figure the figure to be copied
     * @throws IllegalArgumentException if the provided figure is not a Circle
     */
    public Ellipse(Figure figure) throws IllegalArgumentException
    {
        super(figure);
        if (!(figure instanceof figures.Ellipse))
        {
            String message = "provided figure is not an Ellipse: "
                    + figure.getClass().getSimpleName();
            logger.severe(message);
            throw new IllegalArgumentException(message);
        }
        javafx.scene.shape.Ellipse figureEllipse = (javafx.scene.shape.Ellipse) figure.shape;
        shape = new javafx.scene.shape.Ellipse(figureEllipse.getCenterX(),
                figureEllipse.getCenterY(),
                figureEllipse.getRadiusX(), figureEllipse.getRadiusY());
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
    private javafx.scene.shape.Ellipse getEllipseShape()
    {
        return (javafx.scene.shape.Ellipse)shape;
    }

    /**
     * Center Point of this figure
     * @return the center point of this figure
     */
    @Override
    public Point2D getCenter()
    {
        javafx.scene.shape.Ellipse shapeEllipse = getEllipseShape();
        return new Point2D(shapeEllipse.getCenterX(), shapeEllipse.getCenterY());
    }

    /**
     * Width of this figure
     * @return the width of this figure
     */
    @Override
    public double width()
    {
        return getEllipseShape().getRadiusX() * 2.0;
    }

    /**
     * Height of this figure
     * @return the width of this figure
     */
    @Override
    public double height()
    {
        return getEllipseShape().getRadiusY() * 2.0;
    }

    /**
     * Top left corner of this figure
     * @return the top left {@link Point2D} of this figure
     */
    @Override
    public Point2D topLeft()
    {
        double radiusX = getEllipseShape().getRadiusX();
        double radiusY = getEllipseShape().getRadiusY();
        Point2D center = getCenter();

        return new Point2D(center.getX() - radiusX,
                center.getY() - radiusY);
    }

    /**
     * Bottom right corner of this figure
     * @return the bottom right {@link Point2D} of this figure
     */
    @Override
    public Point2D bottomRight()
    {
        double radiusX = getEllipseShape().getRadiusX();
        double radiusY = getEllipseShape().getRadiusY();
        Point2D center = getCenter();

        return new Point2D(center.getX() + radiusX,
                center.getY() + radiusY);
    }

    /**
     * radius accessor of this Circle
     * @return the radius of this Circle
     */
    public double getRadiusX()
    {
        return getEllipseShape().getRadiusX();
    }

    public double getRadiusY() {
        return getEllipseShape().getRadiusY();
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
        shape = new javafx.scene.shape.Ellipse(x, y, 0, 0);
        applyParameters(shape);
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
        getEllipseShape().setRadiusX(lastPoint.getX());
        getEllipseShape().setRadiusY(lastPoint.getY());
    }

    /**
     * Creates a copy of this circle (with the same name and instance number)
     * @return A distinct copy of this circle
     */
    @Override
    public Figure clone()
    {
        return new figures.Ellipse(this);
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
        if (!(figure instanceof figures.Ellipse))
        {
            return false;
        }

        figures.Ellipse ellipse = (figures.Ellipse) figure;

        if (Math.abs(getCenter().distance(ellipse.getCenter())) > Figure.threshold)
        {
            return false;
        }

        if (Math.abs(getRadiusX() - ellipse.getRadiusX()) > Figure.threshold)
        {
            return false;
        }

        if (Math.abs(getRadiusY() - ellipse.getRadiusY()) > Figure.threshold)
        {
            return false;
        }

        return true;
    }
}
