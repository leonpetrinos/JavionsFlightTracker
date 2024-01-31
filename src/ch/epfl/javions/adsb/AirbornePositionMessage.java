package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

/**
 * Represents an ADS-B in-flight positioning message
 *
 * @param timeStampNs (long) : timestamp of the message, in nanoseconds
 * @param icaoAddress (IcaoAddress) : ICAO address of the sender of the message
 * @param altitude    (double) : altitude, in meters, at which the aircraft was at the time the message was sent
 * @param parity      (int) : the parity of the message (0 if even, 1 if odd)
 * @param x           (double) : the local and normalized longitude, between 0 and 1, at which the aircraft was when the message was sent
 * @param y           (double) : the local and normalized latitude, between 0 and 1, at which the aircraft was when the message was sent
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public record AirbornePositionMessage(long timeStampNs, IcaoAddress icaoAddress, double altitude, int parity, double x,
                                      double y) implements Message {
    private final static double NORMALIZING_CONSTANT = Math.scalb(1, -17);
    private final static int BASE_ALTITUDE_Q1 = 1000;
    private final static int FEET_MULTIPLIER_Q1 = 25;
    private final static int BASE_ALTITUDE_Q0 = 1300;
    private final static int FEET_MULTIPLIER_Q0_MSB = 500;
    private final static int FEET_MULTIPLIER_Q0_LSB = 100;
    private final static int D1_POSITION = 4;
    private final static int D4_POSITION = D1_POSITION - 4;
    private final static int B1_POSITION = 5;
    private final static int B4_POSITION = B1_POSITION - 4;
    private final static int A1_POSITION = 10;
    private final static int A4_POSITION = A1_POSITION - 4;
    private final static int C1_POSITION = 11;
    private final static int C4_POSITION = C1_POSITION - 4;
    private final static int LSB_GROUP_SIZE = 3;
    private final static int MSB_GROUP_SIZE = 9;

    /**
     * Compact constructor that checks if the parameters of the record are valid
     *
     * @param timeStampNs (long) : timestamp of the message, in nanoseconds
     * @param icaoAddress (IcaoAddress) : ICAO address of the sender of the message
     * @param altitude    (double) : altitude, in meters, at which the aircraft was at the time the message was sent
     * @param parity      (int) : the parity of the message (0 if even, 1 if odd)
     * @param x           (double) : the local and normalized longitude, between 0 and 1, at which the aircraft was when the message was sent
     * @param y           (double) : the local and normalized latitude, between 0 and 1, at which the aircraft was when the message was sent
     * @throws NullPointerException     if the icao address is null
     * @throws IllegalArgumentException if the timestamp is stictly less than 0, parity is not equal to 0 or 1, or if x and y are not between 0 (inclusive) and 1 (exclusive)
     */
    public AirbornePositionMessage {
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument(
                (timeStampNs >= 0) &&
                        (parity == 0 || parity == 1) &&
                        (x >= 0 && x < 1) &&
                        (y >= 0 && y < 1));
    }

    /**
     * Performs multiple operations to decode the altitude of the aircraft.
     *
     * @param rawMessage (RawMessage) : raw message
     * @return : the in-flight positioning message corresponding to the given raw message and return null if the altitude is not valid
     */
    public static AirbornePositionMessage of(RawMessage rawMessage) {

        long payload = rawMessage.payload();
        double longitudeNormalized = (Bits.extractUInt(payload, 0, 17)) * NORMALIZING_CONSTANT;
        double latitudeNormalized = (Bits.extractUInt(payload, 17, 17)) * NORMALIZING_CONSTANT;
        int parity = Bits.extractUInt(payload, 34, 1);

        int altitude = Bits.extractUInt(payload, 36, 12);
        int Q = Bits.extractUInt(altitude, 4, 1);

        double altitudeMeters;
        switch (Q) {

            case 1 -> {

                int lsb = Bits.extractUInt(altitude, 0, 4);
                int msb = Bits.extractUInt(altitude, 5, 7);
                int altitudeWithoutQ = (msb << 4) | lsb;
                altitudeMeters = Units.convert((altitudeWithoutQ * FEET_MULTIPLIER_Q1) - BASE_ALTITUDE_Q1,
                        Units.Length.FOOT, Units.Length.METER);
                return new AirbornePositionMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), altitudeMeters,
                        parity, longitudeNormalized, latitudeNormalized);

            }

            case 0 -> {

                // First we change the order of the message
                int D = extractBitGroups(D1_POSITION, D4_POSITION, altitude);
                int A = extractBitGroups(A1_POSITION, A4_POSITION, altitude);
                int B = extractBitGroups(B1_POSITION, B4_POSITION, altitude);
                int C = extractBitGroups(C1_POSITION, C4_POSITION, altitude);

                int orderedMessage = D << 9 | A << 6 | B << 3 | C;

                // Now we convert two groups of this message from Grey to Binary
                int lsbGroup = Bits.extractUInt(orderedMessage, 0, LSB_GROUP_SIZE);
                int msbGroup = Bits.extractUInt(orderedMessage, 3, MSB_GROUP_SIZE);

                int lsbGroupDecoded = grayDecoder(LSB_GROUP_SIZE, lsbGroup);
                int msbGroupDecoded = grayDecoder(MSB_GROUP_SIZE, msbGroup);

                // Now there are still some transformations to do to the lsb decoded group
                if (lsbGroupDecoded == 0 || lsbGroupDecoded == 5 || lsbGroupDecoded == 6) {
                    return null;
                } else if (lsbGroupDecoded == 7) {
                    lsbGroupDecoded = 5;
                }

                if (msbGroupDecoded % 2 != 0) lsbGroupDecoded = 6 - lsbGroupDecoded;

                altitudeMeters = Units.convert(((lsbGroupDecoded * FEET_MULTIPLIER_Q0_LSB) +
                                (msbGroupDecoded * FEET_MULTIPLIER_Q0_MSB) - BASE_ALTITUDE_Q0),
                        Units.Length.FOOT, Units.Length.METER);

                return new AirbornePositionMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), altitudeMeters,
                        parity, longitudeNormalized, latitudeNormalized);

            }

            default -> {
                return null;
            }

        }

    }

    /**
     * From a given altitude, this method extracts bits and reorders its bits. It reorders them from
     * C1_A1_C2_A2_C4_A4_B1_D1_B2_D2_B4_D4 to D1_D2_D4_A1_A2_A4_B1_B2_B4_C1_C2_C4
     *
     * @param startPos (int) : start position of extraction
     * @param endPos   (int) : end position of extraction
     * @param altitude (int) : altitude from which we extract the bits
     * @return : the reordered version of the altitude
     */
    private static int extractBitGroups(int startPos, int endPos, int altitude) {
        int j = 2;
        int group = 0;
        for (int i = startPos; i >= endPos; i -= 2) {
            group |= (Bits.extractUInt(altitude, i, 1) << j);
            --j;
        }
        return group;
    }

    /**
     * Converts a value that is interpreted in grey code to binary code
     *
     * @param numberOfBits (int) : number of bits of the grey code
     * @param greyCode     (int) : a number interpreted in grey code
     * @return : the corresponding binary number to the one given in grey code
     */
    private static int grayDecoder(int numberOfBits, int greyCode) {
        int decodedCode = greyCode;
        for (int i = 1; i <= numberOfBits; ++i) {
            decodedCode ^= (greyCode >> i);
        }
        return decodedCode;
    }


}