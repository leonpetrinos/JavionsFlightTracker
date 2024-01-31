package ch.epfl.javions.adsb;

import ch.epfl.javions.aircraft.IcaoAddress;

/**
 * Implemented by all classes representing analyzed ADS-B messages
 *
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public interface Message {

    /**
     * Returns the timestamp of the message, in nanoseconds
     *
     * @return : the timestamp of the message, in nanoseconds
     */
    long timeStampNs();

    /**
     * Returns the ICAO address of the sender of the message
     *
     * @return : the ICAO address of the sender of the message
     */
    IcaoAddress icaoAddress();
}
