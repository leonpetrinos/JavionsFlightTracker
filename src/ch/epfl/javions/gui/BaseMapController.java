package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.WebMercator;
import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import java.io.IOException;

/**
 * Manages the display and interaction with the base map
 *
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public final class BaseMapController {
    private final TileManager tileManager;
    private final MapParameters mapParams;
    private final Canvas canvas;
    private final Pane pane;
    private boolean redrawNeeded;
    private final ObjectProperty<Point2D> previousMousePos;

    /**
     * Installs all bindings, handlers and listeners that allow the map to be modified
     *
     * @param tileManager (TileManager) : tile manager
     * @param mapParams   (MapParameters) : map parameters
     */
    public BaseMapController(TileManager tileManager, MapParameters mapParams) {
        this.tileManager = tileManager;
        this.mapParams = mapParams;
        this.canvas = new Canvas();
        this.pane = new Pane(canvas);
        this.previousMousePos = new SimpleObjectProperty<>();

        installBindings();
        installHandlers();
        installListeners();
    }

    /**
     * Returns the pane
     *
     * @return the pane
     */
    public Pane pane() {
        return pane;
    }

    /**
     * Moves the visible portion of the map such that it is centered at the given position
     *
     * @param pos (GeoPos) : position on the surface of Earth
     */
    public void centerOn(GeoPos pos) {
        int zoom = mapParams.getZoom();
        double xPos = WebMercator.x(zoom, pos.longitude()) - mapParams.getMinX() - (canvas.getWidth() / 2d);
        double yPos = WebMercator.y(zoom, pos.latitude()) - mapParams.getMinY() - (canvas.getHeight() / 2d);
        mapParams.scroll(xPos, yPos);
    }

    /**
     * Binds the canvas width and height to the panes width and height
     */
    private void installBindings() {
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());
    }

    /**
     * Adds listeners to all properties that can change such that the map is changed when these modifications are made
     */
    private void installListeners() {
        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });

        canvas.widthProperty().addListener(o -> redrawOnNextPulse());
        canvas.heightProperty().addListener(o -> redrawOnNextPulse());
        mapParams.minXProperty().addListener(o -> redrawOnNextPulse());
        mapParams.minYProperty().addListener(o -> redrawOnNextPulse());
        mapParams.zoomProperty().addListener(o -> redrawOnNextPulse());
    }

    /**
     * Installs handlers that change the necessary properties if we zoom or scroll on the map.
     */
    private void installHandlers() {

        pane.setOnMousePressed(e -> previousMousePos.set(new Point2D(e.getX(), e.getY())));

        pane.setOnMouseDragged(e -> {
            Point2D currentMousePos = new Point2D(e.getX(), e.getY());
            Point2D delta = previousMousePos.get().subtract(currentMousePos);
            mapParams.scroll(delta.getX(), delta.getY());
            previousMousePos.set(currentMousePos);
        });

        pane.setOnMouseReleased(e -> previousMousePos.set(null));

        LongProperty minScrollTime = new SimpleLongProperty();
        pane.setOnScroll(e -> {
            int zoomDelta = (int) Math.signum(e.getDeltaY());
            if (zoomDelta == 0) return;

            long currentTime = System.currentTimeMillis();
            if (currentTime < minScrollTime.get()) return;
            minScrollTime.set(currentTime + 200);

            double x = e.getX(), y = e.getY();
            int zoom = mapParams.getZoom();
            if (!(zoom == MapParameters.MAX_ZOOM_LEVEL && zoomDelta > 0) &&
                    !(zoom == MapParameters.MIN_ZOOM_LEVEL && zoomDelta < 0)) {
                mapParams.scroll(x, y);
                mapParams.changeZoomLevel(zoomDelta);
                mapParams.scroll(-x, -y);
            }
        });

    }

    /**
     * Requests for a re-draw on the next pulse
     */
    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }

    /**
     * Redraws the canvas if a re-draw has been requested
     */
    private void redrawIfNeeded() {
        if (!redrawNeeded) return;
        redrawNeeded = false;
        GraphicsContext canvasGC = canvas.getGraphicsContext2D();

        double canvasWidth = canvas.getWidth(), canvasHeight = canvas.getHeight();
        double minX = mapParams.getMinX(), minY = mapParams.getMinY();
        int tileMinX = getMinTileCoord(minX), tileMinY = getMinTileCoord(minY);
        int tileMaxX = getMaxTileCoord(minX, canvasWidth), tileMaxY = getMaxTileCoord(minY, canvasHeight);
        int zoom = mapParams.getZoom();
        canvasGC.clearRect(0, 0, canvasWidth, canvasHeight);

        for (int x = tileMinX; x <= tileMaxX; ++x) {
            for (int y = tileMinY; y <= tileMaxY; ++y) {
                try {
                    TileManager.TileId id = new TileManager.TileId(zoom, x, y);
                    if (!TileManager.TileId.isValid(zoom, x, y)) continue;

                    Image img = tileManager.imageForTileAt(id);
                    double canvasX = x * TileManager.TileId.TILE_SIZE - minX;
                    double canvasY = y * TileManager.TileId.TILE_SIZE - minY;
                    canvasGC.drawImage(img, canvasX, canvasY);
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * Returns the minimum tile coordinate given the actual minimum coordinate
     *
     * @param minCoord (double) : actual minimum coordinate
     * @return minimum tile coordinate given the actual minimum coordinate
     */
    private int getMinTileCoord(double minCoord) {
        return (int) (minCoord / TileManager.TileId.TILE_SIZE);
    }

    /**
     * Returns the maximum tile coordinate given the actual minimum coordinate
     *
     * @param minCoord   (double) : actual minimum coordinate
     * @param canvasSize (double) : either width or height of the canvas
     * @return maximum tile coordinate given the actual minimum coordinate
     */
    private int getMaxTileCoord(double minCoord, double canvasSize) {
        return (int) ((minCoord + canvasSize) / TileManager.TileId.TILE_SIZE);
    }

}
