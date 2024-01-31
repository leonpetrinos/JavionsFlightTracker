package ch.epfl.javions.aircraft;

import java.util.Objects;

/**
 * Collects the fixed data of an aircraft
 *
 * @param registration           (AircraftRegistration) : Registration number
 * @param typeDesignator         (AircraftTypeDesignator) : Type designator number
 * @param model                  (String) : Aircraft model
 * @param description            (AircraftDescription) : Aircraft description
 * @param wakeTurbulenceCategory (WakeTurbulenceCategory) : Wake turbulence category (LIGHT, MEDIUM, HEAVY, UNKNOWN)
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public record AircraftData(AircraftRegistration registration, AircraftTypeDesignator typeDesignator, String model,
                           AircraftDescription description, WakeTurbulenceCategory wakeTurbulenceCategory) {

    /**
     * Checks if one of the arguments of the record is null
     *
     * @param registration           (AircraftRegistration) : Registration number
     * @param typeDesignator         (AircraftTypeDesignator) : Type designator number
     * @param model                  (String) : Aircraft model
     * @param description            (AircraftDescription) : Aircraft description
     * @param wakeTurbulenceCategory (WakeTurbulenceCategory) : Wake turbulence category (LIGHT, MEDIUM, HEAVY, UNKNOWN)
     * @throws NullPointerException if one of parameters is null
     */
    public AircraftData {
        Objects.requireNonNull(registration);
        Objects.requireNonNull(typeDesignator);
        Objects.requireNonNull(model);
        Objects.requireNonNull(description);
        Objects.requireNonNull(wakeTurbulenceCategory);
    }

}
