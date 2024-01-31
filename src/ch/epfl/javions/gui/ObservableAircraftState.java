package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Represents the state of an aircraft, which is observable in the sense of the Observer Design Pattern.
 *
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public final class ObservableAircraftState implements AircraftStateSetter {
    private final IcaoAddress icaoAddress;
    private final AircraftData aircraftData;
    private final LongProperty timeStampProperty;
    private final IntegerProperty categoryProperty;
    private final ObjectProperty<CallSign> callSignProperty;
    private final ObjectProperty<GeoPos> positionProperty;
    private final DoubleProperty altitudeProperty;
    private final DoubleProperty velocityProperty;
    private final DoubleProperty trackOrHeadingProperty;
    private final ObservableList<AirbornePos> modifiablePositions;
    private final ObservableList<AirbornePos> unmodifiablePositions;

    /**
     * Creates an observable aircraft state and initializes two lists to be filled with airborne positions of
     * the aircraft. One is modifiable and one is unmodifiable.
     *
     * @param icaoAddress  (IcaoAddress) : ICAO address of the aircraft
     * @param aircraftData (AircraftData) : Fixed characteristics of the aircraft
     */
    public ObservableAircraftState(IcaoAddress icaoAddress, AircraftData aircraftData) {
        this.icaoAddress = icaoAddress;
        this.aircraftData = aircraftData;
        this.modifiablePositions = FXCollections.observableArrayList();
        this.unmodifiablePositions = FXCollections.unmodifiableObservableList(modifiablePositions);
        this.timeStampProperty = new SimpleLongProperty();
        this.categoryProperty = new SimpleIntegerProperty();
        this.callSignProperty = new SimpleObjectProperty<>();
        this.positionProperty = new SimpleObjectProperty<>();
        this.altitudeProperty = new SimpleDoubleProperty(Double.NEGATIVE_INFINITY);
        this.velocityProperty = new SimpleDoubleProperty(Double.NEGATIVE_INFINITY);
        this.trackOrHeadingProperty = new SimpleDoubleProperty();
    }

    /**
     * Returns the ICAO address of the aircraft
     *
     * @return ICAO address of the aircraft
     */
    public IcaoAddress getIcaoAddress() {
        return icaoAddress;
    }

    /**
     * Returns the fixed characteristics of the aircraft
     *
     * @return fixed characteristics of the aircraft (AircraftData)
     */
    public AircraftData getAircraftData() {
        return aircraftData;
    }

    /**
     * Sets the time stamp property
     *
     * @param timeStampNs (long) : new timestamp in nanoseconds
     */
    @Override
    public void setLastMessageTimeStampNs(long timeStampNs) {
        timeStampProperty.set(timeStampNs);
    }

    /**
     * Returns the last message time stamps
     *
     * @return last message time stamps
     */
    public long getLastMessageTimeStampNs() {
        return timeStampProperty.get();
    }

    /**
     * Returns the read-only property that represents the timestamp of the last message
     *
     * @return read-only property that represents the timestamp of the last message
     */
    public ReadOnlyLongProperty lastMessageTimeStampNsProperty() {
        return timeStampProperty;
    }

    /**
     * Sets the category property
     *
     * @param category (int) : new aircraft category
     */
    @Override
    public void setCategory(int category) {
        categoryProperty.set(category);
    }

    /**
     * Returns the category of the aircraft
     *
     * @return category of the aircraft
     */
    public int getCategory() {
        return categoryProperty.get();
    }

    /**
     * Returns the read-only property that represents the aircraft category
     *
     * @return the read-only property that represents the aircraft category
     */
    public ReadOnlyIntegerProperty categoryProperty() {
        return categoryProperty;
    }

    /**
     * Sets the call sign property
     *
     * @param callSign (CallSign) : new aircraft call sign
     */
    @Override
    public void setCallSign(CallSign callSign) {
        callSignProperty.set(callSign);
    }

    /**
     * Returns the call sign of the aircraft
     *
     * @return call sign of the aircraft
     */
    public CallSign getCallSign() {
        return callSignProperty.get();
    }

    /**
     * Returns the read-only property that represents the aircraft call sign
     *
     * @return the read-only property that represents the aircraft call sign
     */
    public ReadOnlyObjectProperty<CallSign> callSignProperty() {
        return callSignProperty;
    }

    /**
     * Sets the position of the aircraft
     *
     * @param position (GeoPos) : new aircraft position
     */
    @Override
    public void setPosition(GeoPos position) {
        this.positionProperty.set(position);
        if (!Double.isInfinite(getAltitude())) {
            modifiablePositions.add(new AirbornePos(position, getAltitude(), getLastMessageTimeStampNs()));
        }
    }

    /**
     * Returns the position of the aircraft
     *
     * @return position of the aircraft
     */
    public GeoPos getPosition() {
        return positionProperty.get();
    }

    /**
     * Returns the read-only property that represents the aircraft position
     *
     * @return the read-only property that represents the aircraft position
     */
    public ReadOnlyObjectProperty<GeoPos> positionProperty() {
        return positionProperty;
    }

    /**
     * Sets the altitude of the aircraft
     *
     * @param altitude (double) : new aircraft altitude
     */
    @Override
    public void setAltitude(double altitude) {
        altitudeProperty.set(altitude);
        GeoPos pos = getPosition();
        if (pos == null) return;

        AirbornePos currentPosition = new AirbornePos(pos, altitude, getLastMessageTimeStampNs());
        if (modifiablePositions.isEmpty()) {
            modifiablePositions.add(currentPosition);
        }
        AirbornePos lastPosition = modifiablePositions.get(modifiablePositions.size() - 1);
        if (getLastMessageTimeStampNs() == lastPosition.timeStampNs) {
            modifiablePositions.set(modifiablePositions.size() - 1, currentPosition);
        }

    }

    /**
     * Returns the altitude of the aircraft
     *
     * @return altitude of the aircraft
     */
    public double getAltitude() {
        return altitudeProperty.get();
    }

    /**
     * Returns the read-only property that represents the aircraft altitude
     *
     * @return the read-only property that represents the aircraft altitude
     */
    public ReadOnlyDoubleProperty altitudeProperty() {
        return altitudeProperty;
    }

    /**
     * Sets the velocity of the aircraft
     *
     * @param velocity (double) : new aircraft velocity
     */
    @Override
    public void setVelocity(double velocity) {
        velocityProperty.set(velocity);
    }

    /**
     * Returns the velocity of the aircraft
     *
     * @return velocity of the aircraft
     */
    public double getVelocity() {
        return velocityProperty.get();
    }

    /**
     * Returns the read-only property that represents the aircraft velocity
     *
     * @return the read-only property that represents the aircraft velocity
     */
    public ReadOnlyDoubleProperty velocityProperty() {
        return velocityProperty;
    }

    /**
     * Sets the direction of the aircraft
     *
     * @param trackOrHeading (double) : new aircraft direction
     */
    @Override
    public void setTrackOrHeading(double trackOrHeading) {
        trackOrHeadingProperty.set(trackOrHeading);
    }

    /**
     * Returns the direction of the aircraft
     *
     * @return direction of the aircraft
     */
    public double getTrackOrHeading() {
        return trackOrHeadingProperty.get();
    }

    /**
     * Returns the read-only property that represents the aircraft direction
     *
     * @return the read-only property that represents the aircraft direction
     */
    public ReadOnlyDoubleProperty trackOrHeadingProperty() {
        return trackOrHeadingProperty;
    }

    /**
     * Returns an unmodifiable observable list containing the aircraft trajectory
     *
     * @return an unmodifiable observable list containing the aircraft trajectory
     */
    public ObservableList<AirbornePos> trajectory() {
        return unmodifiablePositions;
    }

    /**
     * Record that has the position, altitude and timestamps of the aircraft. This allows us to get these attributes.
     *
     * @param position    (GeoPos) : aircraft position
     * @param altitude    (double) : aircraft altitude
     * @param timeStampNs (altitude) : aircraft timestamp
     */
    public record AirbornePos(GeoPos position, double altitude, double timeStampNs) {
    }

}