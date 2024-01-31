package ch.epfl.javions.adsb;

/**
 * Transform raw ADS-B messages into messages of one of the three message types: identification, position, velocity
 *
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public final class MessageParser {

    private MessageParser() {
    }

    /**
     * Depending on the type code of the given raw message, the method returns the corresponding message type
     *
     * @param rawMessage (RawMessage) : raw ADS-B message
     * @return : the message corresponding to a type code
     */
    public static Message parse(RawMessage rawMessage) {
        long typeCode = rawMessage.typeCode();

        if (typeCode == 19) {
            return AirborneVelocityMessage.of(rawMessage);
        } else if (typeCode >= 1 && typeCode <= 4) {
            return AircraftIdentificationMessage.of(rawMessage);
        } else if ((typeCode >= 9 && typeCode <= 18) || (typeCode >= 20 && typeCode <= 22)) {
            return AirbornePositionMessage.of(rawMessage);
        }
        return null;

    }

}
