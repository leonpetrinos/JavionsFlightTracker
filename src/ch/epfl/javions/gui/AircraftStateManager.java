package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.io.IOException;
import java.util.*;

/**
 * Aims to keep the states of a set of aircraft up-to-date according to the messages received from them
 *
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public final class AircraftStateManager {
    private final Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> icaoToAccumulatorMap;
    private final ObservableSet<ObservableAircraftState> aircraftStates;
    private final ObservableSet<ObservableAircraftState> unmodifiableAircraftStates;
    private final AircraftDatabase database;
    private Message m;
    private static final long MINUTE_NS = (long) 60e9;

    /**
     * Creates an AircraftStateManager and initializes an observable set of aicraft states as well as a map that maps
     * an icao address to an aircraft state accumulator
     *
     * @param database (AircraftDatabase) : aircraft database
     *                 (i.e. we can get all the data of an aircraft given an icao address)
     */
    public AircraftStateManager(AircraftDatabase database) {
        this.database = database;
        this.aircraftStates = FXCollections.observableSet();
        this.unmodifiableAircraftStates = FXCollections.unmodifiableObservableSet(aircraftStates);
        this.icaoToAccumulatorMap = new HashMap<>();
    }

    /**
     * Returns the set containing the observable aircraft states as an unmodifiable set
     *
     * @return set containing the observable aircraft states as an unmodifiable set
     */
    public ObservableSet<ObservableAircraftState> states() {
        return unmodifiableAircraftStates;
    }

    /**
     * Updates the state of the aircraft that sent the message
     *
     * @param m (Message) : message received from an aircraft
     * @throws IOException if an input/output error occurs
     */
    public void updateWithMessage(Message m) throws IOException {
        this.m = m;
        IcaoAddress icao = m.icaoAddress();
        AircraftStateAccumulator<ObservableAircraftState> asa = icaoToAccumulatorMap.get(icao);

        if (asa == null) {
            ObservableAircraftState oas = new ObservableAircraftState(icao, database.get(icao));
            asa = new AircraftStateAccumulator<>(oas);
            icaoToAccumulatorMap.put(icao, asa);
        }

        asa.update(m);

        if (icaoToAccumulatorMap.get(icao).stateSetter().getPosition() != null) {
            aircraftStates.add(asa.stateSetter());
        }

    }

    /**
     * Deletes from the set of observable states all those corresponding to aircraft from which no message has been
     * received in the minute preceding the reception of the last message passed to updateWithMessage as well as the
     * corresponding aircraft state setter
     */
    public void purge() {
        Iterator<ObservableAircraftState> it = aircraftStates.iterator();
        while (it.hasNext()) {
            ObservableAircraftState oas = it.next();
            if (m.timeStampNs() - oas.getLastMessageTimeStampNs() > MINUTE_NS) {
                it.remove();
                icaoToAccumulatorMap.remove(oas.getIcaoAddress());
            }
        }
    }
}
