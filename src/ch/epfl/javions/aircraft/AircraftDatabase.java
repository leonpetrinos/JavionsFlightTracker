package ch.epfl.javions.aircraft;

import java.io.*;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.ZipFile;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Represents the aircraft mictronics database
 *
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public final class AircraftDatabase {
    private final String filename;
    private static final String SEPARATOR = ",";
    private static final int ICAO_SIZE = 6;

    /**
     * Stocks the file name as an attribute of the class
     *
     * @param filename (String) : name of file
     * @throws NullPointerException if the file name is null
     */
    public AircraftDatabase(String filename) {
        this.filename = Objects.requireNonNull(filename);
    }

    /**
     * Returns the AircraftData for an aircraft with a specific IcaoAddress from the database
     *
     * @param address (IcaoAddress) : specific ICAO address
     * @return : the AircraftData for an aircraft
     * @throws IOException if an input/output error occurs
     */
    public AircraftData get(IcaoAddress address) throws IOException {
        try (ZipFile zip = new ZipFile(filename);
             InputStream inpStream = zip.getInputStream(zip.getEntry(address.string().substring(4, 6) + ".csv"));
             Reader reader = new InputStreamReader(inpStream, UTF_8);
             BufferedReader bufferedReader = new BufferedReader(reader)) {

            return bufferedReader
                    .lines()
                    .takeWhile(l -> address.string().compareTo(l.substring(0, ICAO_SIZE)) >= 0)
                    .filter(l -> l.startsWith(address.string()))
                    .findFirst()
                    .map(l -> l.split(SEPARATOR, -1))
                    .map(splitLine -> new AircraftData(
                            new AircraftRegistration(splitLine[1]),
                            new AircraftTypeDesignator(splitLine[2]),
                            splitLine[3],
                            new AircraftDescription(splitLine[4]),
                            WakeTurbulenceCategory.of(splitLine[5])))
                    .orElse(null);
        }
    }

}
