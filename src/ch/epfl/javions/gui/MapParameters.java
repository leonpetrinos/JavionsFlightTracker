package ch.epfl.javions.gui;

import ch.epfl.javions.Math2;
import ch.epfl.javions.Preconditions;
import javafx.beans.property.*;

/**
 * Represents the parameters of the portion of the map visible in the graphical interface
 *
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public final class MapParameters {
    private final IntegerProperty zoomProperty;
    private final DoubleProperty minXProperty;
    private final DoubleProperty minYProperty;
    /**
     * Minimum zoom level
     */
    public static final int MIN_ZOOM_LEVEL = 6;
    /**
     * Maximum zoom level
     */
    public static final int MAX_ZOOM_LEVEL = 19;

    /**
     * Checks if the zoom level is valid and if so, initializes the classes properties
     *
     * @param initialZoom (int) : initial zoom level
     * @param initialMinX (double) : initial x-coordinate from the top-left corner of the visible portion of the map
     * @param initialMinY (double) : initial y-coordinate from the top-left corner of the visible portion of the map
     * @throws IllegalArgumentException if the zoom level is not bounded by 6 (incl) and 19 (incl)
     */
    public MapParameters(int initialZoom, double initialMinX, double initialMinY) {
        Preconditions.checkArgument(MIN_ZOOM_LEVEL <= initialZoom && initialZoom <= MAX_ZOOM_LEVEL);
        this.zoomProperty = new SimpleIntegerProperty(initialZoom);
        this.minXProperty = new SimpleDoubleProperty(initialMinX);
        this.minYProperty = new SimpleDoubleProperty(initialMinY);
    }

    /**
     * Returns the read-only property that represents the zoom level
     *
     * @return read-only property that represents the zoom level
     */
    public ReadOnlyIntegerProperty zoomProperty() {
        return zoomProperty;
    }

    /**
     * Returns the zoom level
     *
     * @return zoom level
     */
    public int getZoom() {
        return zoomProperty.get();
    }

    /**
     * Returns the read-only property that represents the x-coordinate of the top left corner of the visible portion of the map
     *
     * @return read-only property that represents the x-coordinate of the top left corner of the visible portion of the map
     */
    public ReadOnlyDoubleProperty minXProperty() {
        return minXProperty;
    }

    /**
     * Returns the x-coordinate of the top left corner of the visible portion of the map
     *
     * @return the x-coordinate of the top left corner of the visible portion of the map
     */
    public double getMinX() {
        return minXProperty.get();
    }

    /**
     * Returns the read-only property that represents the y-coordinate of the top left corner of the visible portion of the map
     *
     * @return read-only property that represents the y-coordinate of the top left corner of the visible portion of the map
     */
    public ReadOnlyDoubleProperty minYProperty() {
        return minYProperty;
    }

    /**
     * Returns the y-coordinate of the top left corner of the visible portion of the map
     *
     * @return the y-coordinate of the top left corner of the visible portion of the map
     */
    public double getMinY() {
        return minYProperty.get();
    }

    /**
     * Translates the top-left corner of the displayed map portion of the given vector (x, y)
     *
     * @param x (double) : x-coordinate of translation
     * @param y (double) : y-coordinate of translation
     */
    public void scroll(double x, double y) {
        minXProperty.set(getMinX() + x);
        minYProperty.set(getMinY() + y);
    }

    /**
     * Changes the zoom level by a zoom difference
     *
     * @param zoomDifference (int) : Change (positive, null, or negative) to be added to the current zoom level
     */
    public void changeZoomLevel(int zoomDifference) {
        int newZoom = Math2.clamp(MIN_ZOOM_LEVEL, getZoom() + zoomDifference, MAX_ZOOM_LEVEL);
        double factor = Math.scalb(1, zoomDifference);

        zoomProperty.set(newZoom);
        minXProperty.set(getMinX() * factor);
        minYProperty.set(getMinY() * factor);

    }


}
