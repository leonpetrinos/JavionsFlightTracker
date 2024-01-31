package ch.epfl.javions.gui;

import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.CallSign;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import static ch.epfl.javions.Units.Speed.KILOMETER_PER_HOUR;
import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY_SUBSEQUENT_COLUMNS;

/**
 * Display of the aircraft table, which shows different information about them.
 *
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public class AircraftTableController {
    private final ObservableSet<ObservableAircraftState> aircraftStates;
    private final ObjectProperty<ObservableAircraftState> selectedAircraftState;
    private final TableView<ObservableAircraftState> tableView;

    private static final int ICAO_WIDTH = 60;
    private static final int CALL_SIGN_WIDTH = 70;
    private static final int REGISTRATION_WIDTH = 90;
    private static final int MODEL_WIDTH = 230;
    private static final int TYPE_WIDTH = 50;
    private static final int DESCRIPTION_WIDTH = CALL_SIGN_WIDTH;
    private static final int NUMERICAL_WIDTH = 85;

    private static final String ICAO_TITLE = "OACI";
    private static final String CALL_SIGN_TITLE = "Indicatif";
    private static final String REGISTRATION_TITLE = "Immatriculation";
    private static final String MODEL_TITLE = "Modèle";
    private static final String TYPE_TITLE = "Type";
    private static final String DESCRIPTION_TITLE = "Description";
    private static final String LONGITUDE_TITLE = "Longitude (°)";
    private static final String LATITUDE_TITLE = "Latitude (°)";
    private static final String ALTITUDE_TITLE = "Altitude (m)";
    private static final String Velocity_TITLE = "Vitesse (km/h)";

    private static final int LONG_LAT_DECIMALS = 4;
    private static final int ALT_VEL_DECIMALS = 0;

    private static final String NUMERIC_STYLE_CLASS = "numeric";
    private static final String EMPTY = "";
    private static final ObservableValue<String> WRAPPED_EMPTY = new ReadOnlyObjectWrapper<>(EMPTY);
    private static final NumberFormat FORMAT = NumberFormat.getInstance();

    /**
     * Creates the table controller by adding the required listeners and initializing each column
     *
     * @param aircraftStates        (ObservableSet<ObservableAircraftState>) :
     * @param selectedAircraftState (ObjectProperty<ObservableAircraftState>) :
     */
    public AircraftTableController(ObservableSet<ObservableAircraftState> aircraftStates,
                                   ObjectProperty<ObservableAircraftState> selectedAircraftState) {
        this.aircraftStates = FXCollections.unmodifiableObservableSet(aircraftStates);
        this.selectedAircraftState = selectedAircraftState;
        tableView = new TableView<>();
        tableView.getStylesheets().add("table.css");
        tableView.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_SUBSEQUENT_COLUMNS);
        tableView.setTableMenuButtonVisible(true);

        installListeners();
        setAllColumns();

    }

    /**
     * Returns the table view
     *
     * @return the table view
     */
    public Node pane() {
        return tableView;
    }

    /**
     * calls the consumer's accept method when a double click is performed on the table
     *
     * @param consumer (Consumer<ObservableAircraftState>) : consumer
     */
    public void setOnDoubleClick(Consumer<ObservableAircraftState> consumer) {
        tableView.setOnMouseClicked(e -> {
            if (selectedAircraftState.get() != null &&
                    e.getClickCount() == 2 &&
                    e.getButton().equals(MouseButton.PRIMARY)) {
                consumer.accept(selectedAircraftState.get());
            }
        });
    }

    /**
     * Installs the listeners on the table view,
     */
    private void installListeners() {

        aircraftStates.addListener((SetChangeListener<ObservableAircraftState>) change -> {
            if (change.wasAdded()) {
                tableView.getItems().add(change.getElementAdded());
                tableView.sort();
            } else if (change.wasRemoved()) {
                tableView.getItems().remove(change.getElementRemoved());
            }
        });

        selectedAircraftState.addListener((o, oldVal, newVal) -> {
            ObservableAircraftState tableItem = tableView.getSelectionModel().getSelectedItem();
            if ((oldVal == null || !oldVal.equals(newVal)) && (tableItem == null || !tableItem.equals(newVal))) {
                tableView.scrollTo(newVal);
                tableView.getSelectionModel().select(newVal);
            }
        });

        tableView.getSelectionModel().selectedItemProperty().addListener((o, oldVal, newVal) -> {
            if (oldVal == null || !oldVal.equals(newVal)) {
                selectedAircraftState.set(newVal);
            }
        });

    }

    /**
     * Creates a column that contains string values
     *
     * @param title (String) : column title
     * @param width (int) : column width
     * @param func  (Function<ObservableAircraftState, ObservableValue<String>> func) : function common to all columns
     * @return a string value column
     */
    private TableColumn<ObservableAircraftState, String> createStringColumn(
            String title, int width, Function<ObservableAircraftState, ObservableValue<String>> func) {
        TableColumn<ObservableAircraftState, String> col = new TableColumn<>(title);
        col.setPrefWidth(width);
        col.setCellValueFactory(f -> func.apply(f.getValue()));
        return col;
    }

    /**
     * Creates a column that contains numeric values
     *
     * @param title         (String) : column title
     * @param decimalDigits (String) : number of digits after decimal point
     * @param func          (Function<ObservableAircraftState, ObservableValue<String>> func) :
     *                      function common to all columns
     * @return a numeric value column
     */
    private TableColumn<ObservableAircraftState, String> createNumericColumn(
            String title, int decimalDigits, Function<ObservableAircraftState, ObservableValue<String>> func) {
        TableColumn<ObservableAircraftState, String> col = new TableColumn<>(title);
        col.getStyleClass().add(NUMERIC_STYLE_CLASS);
        col.setPrefWidth(NUMERICAL_WIDTH);
        setColumnComparator(col);

        col.setCellValueFactory(f -> {
            FORMAT.setMaximumFractionDigits(decimalDigits);
            FORMAT.setMinimumFractionDigits(decimalDigits);
            return func.apply(f.getValue());
        });

        return col;
    }

    /**
     * Defines the comparator for the numeric columns
     *
     * @param col (TableColumn<ObservableAircraftState, String>) : table column
     */
    private void setColumnComparator(TableColumn<ObservableAircraftState, String> col) {
        col.setComparator((s1, s2) -> {
            try {
                return (s1.isEmpty() || s2.isEmpty()) ?
                        s1.compareTo(s2) :
                        Double.compare(FORMAT.parse(s1).doubleValue(), FORMAT.parse(s2).doubleValue());
            } catch (ParseException e) {
                throw new Error();
            }
        });
    }

    /**
     * Creates all the columns and sets them to the table view
     */
    private void setAllColumns() {

        TableColumn<ObservableAircraftState, String> icao =
                createStringColumn(ICAO_TITLE, ICAO_WIDTH, oas ->
                        new ReadOnlyObjectWrapper<>(oas.getIcaoAddress().string()));

        TableColumn<ObservableAircraftState, String> callSign =
                createStringColumn(CALL_SIGN_TITLE, CALL_SIGN_WIDTH, oas ->
                        oas.callSignProperty().map(CallSign::string));

        TableColumn<ObservableAircraftState, String> registration =
                createStringColumn(REGISTRATION_TITLE, REGISTRATION_WIDTH, oas ->
                        !Objects.isNull(oas.getAircraftData()) ?
                                new ReadOnlyObjectWrapper<>(oas.getAircraftData().registration().string()) :
                                WRAPPED_EMPTY);

        TableColumn<ObservableAircraftState, String> model =
                createStringColumn(MODEL_TITLE, MODEL_WIDTH, oas ->
                        !Objects.isNull(oas.getAircraftData()) ?
                                new ReadOnlyObjectWrapper<>(oas.getAircraftData().model()) :
                                WRAPPED_EMPTY);

        TableColumn<ObservableAircraftState, String> type =
                createStringColumn(TYPE_TITLE, TYPE_WIDTH, oas ->
                        !Objects.isNull(oas.getAircraftData()) ?
                                new ReadOnlyObjectWrapper<>(oas.getAircraftData().typeDesignator().string()) :
                                WRAPPED_EMPTY);

        TableColumn<ObservableAircraftState, String> description =
                createStringColumn(DESCRIPTION_TITLE, DESCRIPTION_WIDTH, oas ->
                        !Objects.isNull(oas.getAircraftData()) ?
                                new ReadOnlyObjectWrapper<>(oas.getAircraftData().description().string()) :
                                WRAPPED_EMPTY);

        TableColumn<ObservableAircraftState, String> longitude =
                createNumericColumn(LONGITUDE_TITLE, LONG_LAT_DECIMALS, oas ->
                        oas.positionProperty().map(pos ->
                                FORMAT.format(Units.convertTo(pos.longitude(), Units.Angle.DEGREE))));

        TableColumn<ObservableAircraftState, String> latitude =
                createNumericColumn(LATITUDE_TITLE, LONG_LAT_DECIMALS, oas ->
                        oas.positionProperty().map(pos ->
                                FORMAT.format(Units.convertTo(pos.latitude(), Units.Angle.DEGREE))));

        TableColumn<ObservableAircraftState, String> altitude =
                createNumericColumn(ALTITUDE_TITLE, ALT_VEL_DECIMALS, oas ->
                        oas.altitudeProperty().map(alt ->
                                (alt.doubleValue() > Double.NEGATIVE_INFINITY) ?
                                        FORMAT.format(Units.convertTo(alt.doubleValue(), Units.Length.METER)) :
                                        EMPTY));

        TableColumn<ObservableAircraftState, String> velocity =
                createNumericColumn(Velocity_TITLE, ALT_VEL_DECIMALS, oas ->
                        oas.velocityProperty().map(vel ->
                                (vel.doubleValue() > Double.NEGATIVE_INFINITY) ?
                                        FORMAT.format(Units.convertTo(vel.doubleValue(), KILOMETER_PER_HOUR)) :
                                        EMPTY));

        tableView.getColumns().setAll(List.of(
                icao, callSign, registration, model, type, description, longitude, latitude, altitude, velocity));

    }


}
