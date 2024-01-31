package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

/**
 * Represents a flight speed message
 *
 * @param timeStampNs    (long) : timestamp of the message, in nanoseconds
 * @param icaoAddress    (IcaoAddress) : ICAO address of the sender of the message
 * @param speed          (double) : aircraft speed in m/s
 * @param trackOrHeading (double) : direction of travel of the aircraft, in radians
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public record AirborneVelocityMessage(long timeStampNs, IcaoAddress icaoAddress, double speed,
                                      double trackOrHeading) implements Message {
    private final static double TWO_POWER_10 = 1 << 10;

    /**
     * Compact constructor that checks if the parameters of the record are valid
     *
     * @param timeStampNs    (long) : timestamp of the message, in nanoseconds
     * @param icaoAddress    (IcaoAddress) : ICAO address of the sender of the message
     * @param speed          (double) : aircraft speed in m/s
     * @param trackOrHeading (double) : direction of travel of the aircraft, in radians
     * @throws NullPointerException     if icao address is null
     * @throws IllegalArgumentException if at least one of timeStampNs, speed or trackOrHeading is strictly less than zero
     */
    public AirborneVelocityMessage {
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument(timeStampNs >= 0 && speed >= 0 && trackOrHeading >= 0);
    }

    /**
     * Decodes the velocity of an aircraft (norm and direction)
     *
     * @param rawMessage (RawMessage) : raw ADS-B message
     * @return : the decoded velocity of the aircraft
     */
    public static AirborneVelocityMessage of(RawMessage rawMessage) {

        long payload = rawMessage.payload();
        int subType = Bits.extractUInt(payload, 48, 3);
        long subTypeDependents = Bits.extractUInt(payload, 21, 22);

        double angle, speedNorm;

        switch (subType) {

            case 1, 2 -> {

                double vNS = Bits.extractUInt(subTypeDependents, 0, 10);
                double vEW = Bits.extractUInt(subTypeDependents, 11, 10);

                if (vNS != 0 && vEW != 0) {

                    vEW = calculateVelocity(subTypeDependents, 21, vEW);
                    vNS = calculateVelocity(subTypeDependents, 10, vNS);

                    speedNorm = Math.hypot(vEW, vNS);

                    angle = Math.atan2(vEW, vNS);
                    angle = (angle < 0) ? angle + Units.Angle.TURN : angle; // measured from north, clockwise (in [0, 2*pi])

                    speedNorm = convertSpeed(subType, speedNorm);
                    return new AirborneVelocityMessage(rawMessage.timeStampNs(),
                            rawMessage.icaoAddress(), speedNorm, angle);

                }

            }

            case 3, 4 -> {

                if (Bits.testBit(subTypeDependents, 21)) {

                    int hdg = Bits.extractUInt(subTypeDependents, 11, 10);
                    angle = Units.convertFrom(hdg / TWO_POWER_10, Units.Angle.TURN);
                    int as = Bits.extractUInt(subTypeDependents, 0, 10);

                    if (as != 0) {
                        speedNorm = convertSpeed(subType, as - 1);
                        return new AirborneVelocityMessage(rawMessage.timeStampNs(),
                                rawMessage.icaoAddress(), speedNorm, angle);
                    }

                }

            }

            default -> {
                return null;
            }

        }

        return null;

    }

    /**
     * Calculates the velocity of the aircraft (i.e. with a sign to it)
     *
     * @param subTypeDependants (long) : sequence of 22 bits from which we extract the information on velocity
     * @param directionIndex (int) : index to calculate direction (either for DNS or DEW)
     * @param speed     (double) : speed of the aircraft
     * @return : the velocity of the aircraft
     */
    private static double calculateVelocity(long subTypeDependants, int directionIndex, double speed) {
        speed -= 1;
        return (Bits.testBit(subTypeDependants, directionIndex)) ? speed * -1 : speed;
    }

    /**
     * Converts speed from knots to m/s (if subsonic speed) and from 4 * knots to m/s (if supersonic speed)
     *
     * @param subType   (int) : Bits at index 48 (inclusive) to 51 (inclusive) of the messages payload
     * @param speedNorm (double) : norm of speed of the aircraft
     * @return : aircraft speed in m/s
     */
    private static double convertSpeed(int subType, double speedNorm) {
        return (subType == 1 || subType == 3) ?
                Units.convertFrom(speedNorm, Units.Speed.KNOT) :
                Units.convertFrom(speedNorm, Units.Speed.KNOT * 4);
    }

}
