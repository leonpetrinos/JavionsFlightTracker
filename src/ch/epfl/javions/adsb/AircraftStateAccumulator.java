package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;

import java.util.Objects;

/**
 * An object accumulating ADS-B messages from a single aircraft to determine its status over time.
 *
 * @param <T> (AircraftStateSetter) : state setter
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public final class AircraftStateAccumulator<T extends AircraftStateSetter> {
    private final T stateSetter;
    private final AirbornePositionMessage[] previousApm;
    private static final double NANO_DIFF = 10e9;

    /**
     * Constructs an Aircraft State Accumulator
     *
     * @param stateSetter (T) : state setter
     * @throws NullPointerException if stateSetter is null
     */
    public AircraftStateAccumulator(T stateSetter) {
        Objects.requireNonNull(stateSetter);
        this.stateSetter = stateSetter;
        previousApm = new AirbornePositionMessage[2];
    }

    /**
     * Returns the stateSetter passed to the constructor
     *
     * @return : stateSetter
     */
    public T stateSetter() {
        return stateSetter;
    }

    /**
     * Depending on the type of the message received, this method sets different attributes of this message
     *
     * @param message (Message) : message received (one of three types)
     */
    public void update(Message message) {
        stateSetter.setLastMessageTimeStampNs(message.timeStampNs());
        if (message instanceof AircraftIdentificationMessage currentAim) {
            stateSetter.setCategory(currentAim.category());
            stateSetter.setCallSign(currentAim.callSign());
        } else if (message instanceof AirborneVelocityMessage currentAvm) {
            stateSetter.setVelocity(currentAvm.speed());
            stateSetter.setTrackOrHeading(currentAvm.trackOrHeading());
        } else if (message instanceof AirbornePositionMessage currentApm) {
            int currentParity = currentApm.parity();
            stateSetter.setAltitude(currentApm.altitude());

            switch (currentParity) {

                case 0 -> {
                    previousApm[0] = currentApm;
                    if (positionCanBeSet(currentApm, previousApm[1])) {
                        GeoPos pos = CprDecoder.decodePosition(currentApm.x(), currentApm.y(),
                                previousApm[1].x(), previousApm[1].y(), currentParity);
                        if (pos != null) stateSetter.setPosition(pos);
                    }
                }

                case 1 -> {
                    previousApm[1] = currentApm;
                    if (positionCanBeSet(currentApm, previousApm[0])) {
                        GeoPos pos = CprDecoder.decodePosition(previousApm[0].x(), previousApm[0].y(),
                                currentApm.x(), currentApm.y(), currentParity);
                        if (pos != null) stateSetter.setPosition(pos);
                    }
                }
            }
        }

        // had to comment this because of change of computer and I don't have the correct language level
        /*
        switch (message) {

            case AircraftIdentificationMessage currentAim -> {
                stateSetter.setCategory(currentAim.category());
                stateSetter.setCallSign(currentAim.callSign());
            }

            case AirborneVelocityMessage currentAvm -> {
                stateSetter.setVelocity(currentAvm.speed());
                stateSetter.setTrackOrHeading(currentAvm.trackOrHeading());
            }

            case AirbornePositionMessage currentApm -> {
                int currentParity = currentApm.parity();
                stateSetter.setAltitude(currentApm.altitude());

                switch (currentParity) {

                    case 0 -> {
                        previousApm[0] = currentApm;
                        if (positionCanBeSet(currentApm, previousApm[1])) {
                            GeoPos pos = CprDecoder.decodePosition(currentApm.x(), currentApm.y(),
                                    previousApm[1].x(), previousApm[1].y(), currentParity);
                            if (pos != null) stateSetter.setPosition(pos);
                        }
                    }

                    case 1 -> {
                        previousApm[1] = currentApm;
                        if (positionCanBeSet(currentApm, previousApm[0])) {
                            GeoPos pos = CprDecoder.decodePosition(previousApm[0].x(), previousApm[0].y(),
                                    currentApm.x(), currentApm.y(), currentParity);
                            if (pos != null) stateSetter.setPosition(pos);
                        }
                    }
                }

            }

            default -> System.out.println("Other type of message.");

        }

         */

    }

    /**
     * Determines if a position can be set
     *
     * @param currentApm  (AirbornePositionMessage) : current position message
     * @param previousApm (AirbornePositionMessage) : previous position message of opposite parity to the current message
     * @return : true if a position can be set
     */
    private boolean positionCanBeSet(AirbornePositionMessage currentApm, AirbornePositionMessage previousApm) {
        return (previousApm != null) && (currentApm.timeStampNs() - previousApm.timeStampNs() <= NANO_DIFF);
    }

}




