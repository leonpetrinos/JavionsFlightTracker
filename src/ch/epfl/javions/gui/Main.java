package ch.epfl.javions.gui;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

/**
 * Contains the main program "Javions"
 *
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public final class Main extends Application {
    private static final int INITIAL_ZOOM = 8;
    private static final double INITIAL_MINX = 33530;
    private static final double INITIAL_MINY = 23070;
    private static final int STAGE_WIDTH = 800;
    private static final int STAGE_HEIGHT = 600;
    private static final String CACHE_MEMORY_PATH = "tile-cache";
    private static final String TILE_SERVER = "tile.openstreetmap.org";
    private static final String RESOURCE_ZIP = "/aircraft.zip";
    private static final String STAGE_TITLE = "Javions";
    private static final long MILLION = (long) 1e6;
    private static final long SECOND_IN_NS = (long) 1e9;
    private long initialStartTime;
    private long lastPurgeTime = 0L;

    /**
     * Launches the application
     *
     * @param args (String[]) : arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        initialStartTime = System.nanoTime();
        AircraftDatabase database = createDataBase();

        Path tileCache = Path.of(CACHE_MEMORY_PATH);
        TileManager tileManager = new TileManager(tileCache, TILE_SERVER);
        MapParameters mapParams = new MapParameters(INITIAL_ZOOM, INITIAL_MINX, INITIAL_MINY);
        BaseMapController baseMapController = new BaseMapController(tileManager, mapParams);

        AircraftStateManager stateManager = new AircraftStateManager(database);
        ObjectProperty<ObservableAircraftState> selectedAircraftState = new SimpleObjectProperty<>();

        AircraftController aircraftController =
                new AircraftController(mapParams, stateManager.states(), selectedAircraftState);
        AircraftTableController aircraftTableController =
                new AircraftTableController(stateManager.states(), selectedAircraftState);
        StatusLineController statusLineController = new StatusLineController();

        aircraftTableController.setOnDoubleClick(oas -> baseMapController.centerOn(oas.getPosition()));
        statusLineController.aircraftCountProperty().bind(Bindings.size(stateManager.states()));

        ConcurrentLinkedQueue<Message> messageQueue = new ConcurrentLinkedQueue<>();
        Supplier<Message> messageSupplier = (getParameters().getRaw().isEmpty()) ?
                airspyMessageSupplier() :
                fileMessageSupplier();

        createThread(messageQueue, messageSupplier);
        createAnimationTimer(messageQueue, stateManager, statusLineController);

        SplitPane root = createMainPane(
                baseMapController,
                aircraftController,
                aircraftTableController,
                statusLineController
        );

        setStage(primaryStage, root);
    }

    /**
     * Creates the main pane with the map, table and aircraft
     */
    private SplitPane createMainPane(BaseMapController bmc, AircraftController ac, AircraftTableController atc,
                                     StatusLineController slc) {
        Pane aircraftMap = new StackPane(bmc.pane(), ac.pane());
        Pane aircraftTable = new BorderPane(atc.pane(), slc.pane(), null, null, null);
        SplitPane root = new SplitPane(aircraftMap, aircraftTable);
        root.setOrientation(Orientation.VERTICAL);
        return root;
    }

    /**
     * Creates the database of the aircraft
     */
    private AircraftDatabase createDataBase() throws Exception {
        URL u = getClass().getResource(RESOURCE_ZIP);
        assert u != null;
        Path p = Path.of(u.toURI());
        return new AircraftDatabase(p.toString());
    }

    /**
     * Creates the scene and sets the mane pane to the stage
     */
    private void setStage(Stage primaryStage, SplitPane root) {
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(STAGE_WIDTH);
        primaryStage.setMinHeight(STAGE_HEIGHT);
        primaryStage.setTitle(STAGE_TITLE);
        primaryStage.show();
    }

    /**
     * Creates the second thread used to read the messages
     */
    private void createThread(ConcurrentLinkedQueue<Message> messageQueue, Supplier<Message> messageSupplier) {
        Thread messageThread = new Thread(() -> {
            for (; ; ) {
                Message m = messageSupplier.get();
                if (m != null) {
                    messageQueue.add(m);
                }
            }
        });
        messageThread.setDaemon(true);
        messageThread.start();
    }

    /**
     * Creates the animation timer that updates the messages
     */
    private void createAnimationTimer(
            ConcurrentLinkedQueue<Message> messageQueue, AircraftStateManager asm, StatusLineController slc) {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                try {
                    while (!messageQueue.isEmpty()) {
                        Message m = messageQueue.poll();
                        if (m != null) {
                            asm.updateWithMessage(m);
                            slc.messageCountProperty().set(slc.messageCountProperty().getValue() + 1);
                        }
                        if ((now - lastPurgeTime >= SECOND_IN_NS)) {
                            asm.purge();
                            lastPurgeTime = now;
                        }
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }.start();
    }

    /**
     * Supplies the messages that come from the air spy (System.in
     */
    private Supplier<Message> airspyMessageSupplier() throws IOException {
        AdsbDemodulator demodulator = new AdsbDemodulator(System.in);
        return () -> {
            for (; ; ) {
                try {
                    RawMessage rawMessage;
                    if ((rawMessage = demodulator.nextMessage()) != null) {
                        Message m = MessageParser.parse(rawMessage);
                        if (m != null) {
                            return m;
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    /**
     * Supplies messages from a given file
     */
    private Supplier<Message> fileMessageSupplier() throws IOException {
        List<Message> allMessages = readAllMessages(getParameters().getRaw().get(0));
        Iterator<Message> it = allMessages.iterator();
        return () -> {
            if (it.hasNext()) {
                Message m = it.next();
                sleepIfNeeded(m);
                return m;
            }
            return null;
        };
    }

    /**
     * Method checking if the program should wait to show an aircraft depending on its timestamp
     */
    private void sleepIfNeeded(Message m) throws RuntimeException {
        long timeElapsedNs = m.timeStampNs() - (System.nanoTime() - initialStartTime);
        if (timeElapsedNs >= 0) {
            try {
                Thread.sleep(timeElapsedNs / MILLION);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * reads all the messages from a given file
     */
    private static List<Message> readAllMessages(String fileName) throws IOException {
        List<Message> messages = new ArrayList<>();
        try (DataInputStream s = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(fileName)))) {
            byte[] bytes = new byte[RawMessage.LENGTH];
            for (; ; ) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                ByteString byteString = new ByteString(bytes);
                Message m = MessageParser.parse(new RawMessage(timeStampNs, byteString));
                if (m != null) {
                    messages.add(m);
                }
            }
        } catch (EOFException ignored) {
        }
        return messages;
    }


}
