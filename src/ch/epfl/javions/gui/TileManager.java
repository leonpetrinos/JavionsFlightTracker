package ch.epfl.javions.gui;

import javafx.scene.image.Image;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;

/**
 * Represents an open street map (OSM) tile manager. Its role is to get the tiles from a tile server and stores
 * them in a memory cache and in a disk cache.
 *
 * @author Leon Petrinos (357588)
 * @author Andrea Trugenberger (357615)
 */
public final class TileManager {
    private static final int MEMORY_CACHE_MAX_SIZE = 100;
    private final LinkedHashMap<TileId, Image> memoryCache;
    private final Path pathToDiskCache;
    private final String tileServerName;

    /**
     * Creates a Tile Manager and initializes the memory cache to a LinkedHashMap.
     *
     * @param pathToCacheDisk (Path) : Path to the folder that is the cache disk
     * @param tileServerName  (String) : Name of server from which we obtain the tiles
     */
    public TileManager(Path pathToCacheDisk, String tileServerName) {
        this.memoryCache = new LinkedHashMap<>(MEMORY_CACHE_MAX_SIZE, 0.75f, true);
        this.pathToDiskCache = pathToCacheDisk;
        this.tileServerName = tileServerName;
    }

    /**
     * Returns the image corresponding to the given tile ID
     *
     * @param tileId (TileId) : tile ID
     * @return image corresponding to the given tile ID
     * @throws IOException if an input/output error occurs
     */
    public Image imageForTileAt(TileId tileId) throws IOException {
        if (memoryCache.containsKey(tileId)) {
            return memoryCache.get(tileId); // automatically moved to the most recent entry of the LinkedHasMap
        }

        Path imgPath = tileId.createPath(pathToDiskCache);
        return (Files.exists(imgPath)) ?
                getImageFromFile(imgPath, tileId) :
                getImageFromUrl(imgPath, tileId);
    }

    /**
     * Returns the image coming from a URL
     */
    private Image getImageFromUrl(Path imgPath, TileId tileId) throws IOException {
        Files.createDirectories(imgPath.getParent());
        URL tileUrl = new URL(
                "https", tileServerName, "/" + tileId.zoom + "/" + tileId.x + "/" + tileId.y + ".png");
        URLConnection urlConnection = tileUrl.openConnection();
        urlConnection.setRequestProperty("User-Agent", "Javions");

        try (InputStream in = urlConnection.getInputStream();
             OutputStream out = new FileOutputStream(imgPath.toFile())) {
            byte[] bytes = in.readAllBytes();
            out.write(bytes);
            Image img = new Image(new ByteArrayInputStream(bytes));
            addImageToCache(tileId, img);
            return img;
        }
    }

    /**
     * Returns the image coming from a file
     */
    private Image getImageFromFile(Path imgPath, TileId tileId) throws IOException {
        try (InputStream in = new FileInputStream(imgPath.toFile())) {
            Image img = new Image(in);
            addImageToCache(tileId, img);
            return img;
        }
    }

    /**
     * Adds a new image to the cache memory
     */
    private void addImageToCache(TileId id, Image image) {
        if (memoryCache.size() == MEMORY_CACHE_MAX_SIZE) {
            memoryCache.remove(memoryCache.keySet().iterator().next());
        }
        memoryCache.put(id, image);
    }

    /**
     * Contains the components of a tile
     *
     * @param zoom (int) : zoom level
     * @param x    (int) : index x of the tile
     * @param y    (int) : index y of the tile
     */
    public record TileId(int zoom, int x, int y) {

        /**
         * Size of a tile (number of pixels)
         */
        public static int TILE_SIZE = 256;

        /**
         * Checks if the x index and y index of the tile is valid
         *
         * @param zoom (int) : zoom level
         * @param x    (int) : index x of the tile
         * @param y    (int) : index y of the tile
         * @return true if the indexes are valid and false otherwise
         */
        public static boolean isValid(int zoom, int x, int y) {
            int constant = 1 << zoom;
            return (0 <= x && x < constant) && (0 <= y && y < constant);
        }

        /**
         * Creates a path dependent on zoom, x and y given the cache disk path
         *
         * @param cacheDiskPath (Path) : path of the cache disk
         * @return the path of a tileId
         */
        private Path createPath(Path cacheDiskPath) {
            return cacheDiskPath
                    .resolve(Integer.toString(zoom))
                    .resolve(Integer.toString(x))
                    .resolve(y + ".png");
        }

    }


}
