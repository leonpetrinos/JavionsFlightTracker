package ch.epfl.javions.gui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

/**
 * Manages the status line
 *
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public final class StatusLineController {
    private final Pane statusLine;
    private final IntegerProperty aircraftCountProperty;
    private final LongProperty messageCountProperty;

    /**
     * Creates the status line BorderPane
     */
    public StatusLineController() {
        aircraftCountProperty = new SimpleIntegerProperty();
        messageCountProperty = new SimpleLongProperty();

        Text right = new Text(), left = new Text();
        installBindings(right, left);

        statusLine = new BorderPane(null, null, right, null, left);
        statusLine.getStylesheets().add("status.css");
    }

    private void installBindings(Text right, Text left) {
        right.textProperty().bind(Bindings.createStringBinding(() ->
                        String.format("Messages reçus : %s", messageCountProperty.getValue()),
                messageCountProperty));

        left.textProperty().bind(Bindings.createStringBinding(() ->
                        String.format("Aéronefs visibles : %s", aircraftCountProperty.getValue()),
                aircraftCountProperty));
    }

    /**
     * Returns the status line
     *
     * @return status line
     */
    public Pane pane() {
        return statusLine;
    }

    /**
     * Returns a modifiable property containing the aircraft count
     *
     * @return a modifiable property containing the aircraft count
     */
    public IntegerProperty aircraftCountProperty() {
        return aircraftCountProperty;
    }

    /**
     * Returns a modifiable property containing the message count
     *
     * @return a modifiable property containing the aircraft count
     */
    public LongProperty messageCountProperty() {
        return messageCountProperty;
    }


}
