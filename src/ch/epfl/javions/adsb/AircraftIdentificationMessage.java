package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

/**
 * Represents an ADS-B message of identification and category
 *
 * @param timeStampNs (long) : timestamp of the message, in nanoseconds
 * @param icaoAddress (IcaoAddress) : ICAO address of the sender of the message
 * @param category    (int) : sender's aircraft category
 * @param callSign    (CallSign) : sender's callsign
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public record AircraftIdentificationMessage(long timeStampNs, IcaoAddress icaoAddress, int category,
                                            CallSign callSign) implements Message {
    private static final String ALPHABET = "?ABCDEFGHIJKLMNOPQRSTUVWXYZ????? ???????????????0123456789??????";
    private static final char INVALID_CHARACTER = '?';

    /**
     * Compact constructor that checks if the parameters of the record are valid
     *
     * @param timeStampNs (long) : timestamp of the message, in nanoseconds
     * @param icaoAddress (IcaoAddress) : ICAO address of the sender of the message
     * @param category    (int) : sender's aircraft category
     * @param callSign    (CallSign) : sender's callsign
     * @throws NullPointerException     if the icao address or callsign is null
     * @throws IllegalArgumentException if the timestamp is strictly less than zero
     */
    public AircraftIdentificationMessage {
        Objects.requireNonNull(icaoAddress);
        Objects.requireNonNull(callSign);
        Preconditions.checkArgument(timeStampNs >= 0);
    }

    /**
     * Extracts different groups from the payload (ME) and decodes them
     *
     * @param rawMessage (RawMessage) : raw message
     * @return null if the message is not valid. If valid, it returns an AircraftIdentificationMessage whose
     * parameters are different decoded groups of the payload
     */
    public static AircraftIdentificationMessage of(RawMessage rawMessage) {
        long payload = rawMessage.payload();
        int CA = Bits.extractUInt(payload, 48, 3);
        int typeCode = rawMessage.typeCode();

        int msb = (RawMessage.LENGTH - typeCode);
        int category = ((msb << 4) | (CA));
        StringBuilder callSign = new StringBuilder();

        for (int i = 42; i >= 0; i -= 6) {
            int index = Bits.extractUInt(payload, i, 6);
            char charAtIndex = ALPHABET.charAt(index);
            if (charAtIndex != INVALID_CHARACTER) {
                callSign.append(charAtIndex);
            } else {
                return null;
            }
        }

        String cs = callSign.toString().stripTrailing();
        return new AircraftIdentificationMessage(rawMessage.timeStampNs(),
                rawMessage.icaoAddress(), category, new CallSign(cs));
    }

}
