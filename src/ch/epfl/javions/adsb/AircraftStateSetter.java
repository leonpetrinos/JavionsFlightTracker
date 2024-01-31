package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;

/**
 * Implemented by all classes representing the (modifiable) state of an aircraft
 *
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public interface AircraftStateSetter {

    /**
     * Changes the timestamp of the last message received from the aircraft
     *
     * @param timeStampNs (long) : new timestamp in nanoseconds
     */
    void setLastMessageTimeStampNs(long timeStampNs);

    /**
     * Changes the aircraft category
     *
     * @param category (int) : new aircraft category
     */
    void setCategory(int category);

    /**
     * Changes the aircraft callsign
     *
     * @param callSign (CallSign) : new aircraft callsign
     */
    void setCallSign(CallSign callSign);

    /**
     * Changes the position of the aircraft
     *
     * @param position (GeoPos) : new aircraft position
     */
    void setPosition(GeoPos position);

    /**
     * Changes the altitude of the aircraft
     *
     * @param altitude (double) : new aircraft altitude
     */
    void setAltitude(double altitude);

    /**
     * Changes the speed of the aircraft
     *
     * @param velocity (double) : new aircraft speed
     */
    void setVelocity(double velocity);

    /**
     * Changes the direction of the aircraft
     *
     * @param trackOrHeading (double) : new aircraft direction
     */
    void setTrackOrHeading(double trackOrHeading);


}
