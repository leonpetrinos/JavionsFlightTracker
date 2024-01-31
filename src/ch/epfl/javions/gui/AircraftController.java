package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;
import ch.epfl.javions.WebMercator;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.*;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.*;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

import java.util.Iterator;

import static javafx.scene.paint.CycleMethod.NO_CYCLE;
import static ch.epfl.javions.gui.ObservableAircraftState.AirbornePos;

/**
 * Manages the view of the aircraft
 *
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public final class AircraftController {
    private final MapParameters mapParams;
    private final ObjectProperty<ObservableAircraftState> selectedAircraftState;
    private final Pane pane;
    private static final String INVALID_CHARACTER = "?";
    private static final String EMPTY = "";
    private static final String LABEL_STYLE_CLASS = "label";
    private static final String ICON_STYLE_CLASS = "aircraft";
    private static final String TRAJECTORY_STYLE_CLASS = "trajectory";
    private static final int MIN_ZOOM_FOR_LABEL = 11;
    private static final double APPROXIMATE_MAX_ALTITUDE = 12000d;

    /**
     * @param mapParams             (MapParameters) : map parameters
     * @param aircraftStates        (ObservableSet<ObservableAircraftState>) : set containing all aircraft states
     * @param selectedAircraftState (ObjectProperty<ObservableAircraftState>) : selected aircraft state, with the mouse
     */
    public AircraftController(MapParameters mapParams, ObservableSet<ObservableAircraftState> aircraftStates,
                              ObjectProperty<ObservableAircraftState> selectedAircraftState) {
        this.mapParams = mapParams;
        this.selectedAircraftState = selectedAircraftState;
        pane = new Pane();
        pane.setPickOnBounds(false);
        pane.getStylesheets().add("aircraft.css");

        aircraftStates.addListener((SetChangeListener<ObservableAircraftState>) change -> {
            if (change.wasAdded()) {
                ObservableAircraftState added = change.getElementAdded();
                pane.getChildren().add(aircraft(added));
            } else if (change.wasRemoved()) {
                ObservableAircraftState removed = change.getElementRemoved();
                pane.getChildren().removeIf(n -> n.getId().equals(removed.getIcaoAddress().string()));
            }
        });

    }

    /**
     * Returns the pone on which all aircraft are on
     *
     * @return pane on which all aircraft are on
     */
    public Pane pane() {
        return pane;
    }

    /**
     * Node containing the annotated aircraft
     *
     * @param oas (ObservableAircraftState) : aircraft state
     * @return node containing the annotated aircraft
     */
    private Node aircraft(ObservableAircraftState oas) {
        Group aircraft = new Group(trajectory(oas), labelAndIcon(oas));
        aircraft.setId(oas.getIcaoAddress().string());
        aircraft.viewOrderProperty().bind(oas.altitudeProperty().negate());
        return aircraft;
    }

    /**
     * Node containing the annotated aircraft
     *
     * @param oas (ObservableAircraftState) : aircraft state
     * @return node containing the annotated aircraft
     */
    private Node trajectory(ObservableAircraftState oas) {
        Group trajectory = new Group();
        trajectory.getStyleClass().add(TRAJECTORY_STYLE_CLASS);

        if (trajectory.visibleProperty().get()) {
            ObservableList<AirbornePos> trajectoryList = oas.trajectory();

            trajectoryList.addListener((ListChangeListener<AirbornePos>) change ->
                    addLinesToTrajectory(trajectoryList, trajectory));

            mapParams.zoomProperty().addListener(o ->
                    addLinesToTrajectory(trajectoryList, trajectory));
        }

        trajectory.layoutXProperty().bind(mapParams.minXProperty().negate());
        trajectory.layoutYProperty().bind(mapParams.minYProperty().negate());

        trajectory.visibleProperty().bind(Bindings.createBooleanBinding(() ->
                selectedAircraftState.get() != null && oas.equals(selectedAircraftState.get()), selectedAircraftState)
        );

        return trajectory;
    }

    /**
     * Adds lines to the trajectory of an aircraft (to create the trajectory)
     *
     * @param trajectoryList (ObservableList<ObservableAircraftState.AirbornePos>) :
     *                       list of all positions of the aircraft
     * @param trajectory     (Group) : group of the trajectory of the aircraft
     */
    private void addLinesToTrajectory(ObservableList<AirbornePos> trajectoryList, Group trajectory) {
        trajectory.getChildren().clear();

        Iterator<AirbornePos> it = trajectoryList.iterator();
        ObservableAircraftState.AirbornePos ap1 = null;
        while (it.hasNext()) {
            ObservableAircraftState.AirbornePos ap2 = it.next();
            if (ap1 != null) {
                GeoPos pos1 = ap1.position();
                GeoPos pos2 = ap2.position();
                int zoom = mapParams.getZoom();

                Line line = new Line(
                        WebMercator.x(zoom, pos1.longitude()),
                        WebMercator.y(zoom, pos1.latitude()),
                        WebMercator.x(zoom, pos2.longitude()),
                        WebMercator.y(zoom, pos2.latitude()));

                double alt1 = ap1.altitude();
                double alt2 = ap2.altitude();

                Paint paint;
                if (alt1 == alt2) {
                    paint = getColor(alt1);
                } else {
                    Color c1 = getColor(alt1), c2 = getColor(alt2);
                    Stop s1 = new Stop(0, c1), s2 = new Stop(1, c2);
                    paint = new LinearGradient(
                            0, 0, 1, 0, true, NO_CYCLE, s1, s2);
                }

                line.setStroke(paint);
                trajectory.getChildren().add(line);
            }

            ap1 = ap2;
        }

    }

    /**
     * Node containing the label and icon of the aircraft
     *
     * @param oas (ObservableAircraftState) : aircraft state
     * @return node containing the label and icon of the aircraft
     */
    private Node labelAndIcon(ObservableAircraftState oas) {
        Group labelAndIcon = new Group(label(oas), icon(oas));

        ReadOnlyObjectProperty<GeoPos> posProp = oas.positionProperty();
        ReadOnlyIntegerProperty zoomProp = mapParams.zoomProperty();

        labelAndIcon.layoutXProperty().bind(Bindings.createDoubleBinding(() ->
                        WebMercator.x(mapParams.getZoom(), oas.getPosition().longitude()) - mapParams.getMinX(),
                mapParams.minXProperty(), posProp, zoomProp
        ));

        labelAndIcon.layoutYProperty().bind(Bindings.createDoubleBinding(() ->
                        WebMercator.y(mapParams.getZoom(), oas.getPosition().latitude()) - mapParams.getMinY(),
                mapParams.minYProperty(), posProp, zoomProp
        ));

        return labelAndIcon;
    }

    /**
     * Node containing the icon of the aircraft
     *
     * @param oas (ObservableAircraftState) : aircraft state
     * @return node containing the icon of the aircraft
     */
    private Node icon(ObservableAircraftState oas) {
        SVGPath svg = new SVGPath();
        svg.getStyleClass().add(ICON_STYLE_CLASS);

        AircraftData data = oas.getAircraftData();

        ObservableValue<AircraftIcon> iconProperty = Bindings.createObjectBinding(() ->
                        (data == null) ?
                                AircraftIcon.iconFor(
                                        new AircraftTypeDesignator(EMPTY),
                                        new AircraftDescription(EMPTY),
                                        oas.getCategory(),
                                        WakeTurbulenceCategory.UNKNOWN) :
                                AircraftIcon.iconFor(
                                        data.typeDesignator(),
                                        data.description(),
                                        oas.getCategory(),
                                        data.wakeTurbulenceCategory())
                , oas.categoryProperty()
        );

        svg.contentProperty().bind(Bindings.createStringBinding(() ->
                iconProperty.getValue().svgPath(), iconProperty)
        );

        svg.rotateProperty().bind(Bindings.createDoubleBinding(() ->
                        iconProperty.getValue().canRotate() ?
                                Units.convertTo(oas.getTrackOrHeading(), Units.Angle.DEGREE) :
                                0
                , oas.trackOrHeadingProperty(), iconProperty)
        );

        svg.fillProperty().bind(Bindings.createObjectBinding(() ->
                getColor(oas.getAltitude()), oas.altitudeProperty())
        );

        svg.setOnMouseClicked(e -> selectedAircraftState.set(oas));

        return svg;
    }

    /**
     * Node containing the label of the aircraft
     *
     * @param oas (ObservableAircraftState) : aircraft state
     * @return node containing the label of the aircraft
     */
    private Node label(ObservableAircraftState oas) {
        Text t = new Text();

        AircraftData data = oas.getAircraftData();
        t.textProperty().bind(Bindings.createStringBinding(() -> {
                    AircraftRegistration ar = (data != null) ? data.registration() : null;
                    CallSign cs = oas.getCallSign();
                    IcaoAddress icao = oas.getIcaoAddress();
                    String line1 = (ar != null) ? ar.string() : (cs != null) ? cs.string() : icao.string();

                    double vel = oas.getVelocity(), alt = oas.getAltitude();
                    String velocity = (!Double.isInfinite(vel)) ?
                            String.valueOf((int) Math.rint(Units.convertTo(vel, Units.Speed.KILOMETER_PER_HOUR))) :
                            INVALID_CHARACTER;

                    String altitude = (!Double.isInfinite(alt)) ?
                            String.valueOf((int) Math.rint(alt)) :
                            INVALID_CHARACTER;

                    return String.format("%s\n%s km/h\u2002%s m", line1, velocity, altitude);
                }, oas.velocityProperty(), oas.altitudeProperty(), oas.callSignProperty())
        );

        Rectangle r = new Rectangle();
        r.widthProperty().bind(t.layoutBoundsProperty().map(b -> b.getWidth() + 4));
        r.heightProperty().bind(t.layoutBoundsProperty().map(b -> b.getHeight() + 4));

        Group label = new Group(r, t);
        label.getStyleClass().add(LABEL_STYLE_CLASS);

        label.visibleProperty().bind(Bindings.createBooleanBinding(() -> {
                    ObservableAircraftState selected = selectedAircraftState.get();
                    return (mapParams.getZoom() >= MIN_ZOOM_FOR_LABEL) || (selected != null && selected.equals(oas));
                }, mapParams.zoomProperty(), selectedAircraftState
        ));

        return label;
    }

    /**
     * Method to get the color of an aircraft icon and its trajectory given the altitude
     *
     * @param altitude (double) : altitude of aircraft
     * @return he color of an aircraft icon and its trajectory given the altitude
     */
    private Color getColor(double altitude) {
        double c = Math.pow(altitude / APPROXIMATE_MAX_ALTITUDE, 1d / 3d);
        return ColorRamp.PLASMA.at(c);
    }


}
