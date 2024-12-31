package au.edu.sa.mbhs.studentrobotics.bunyipslib.util;

import androidx.annotation.NonNull;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.RobotConfig;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.localization.Localizer;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.transforms.StartingConfiguration;
import com.acmerobotics.roadrunner.Pose2d;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Global filesystem and volatile storage utilities for robot operation.
 *
 * @author Lucas Bubner, 2024
 * @since 3.2.0
 */
public final class Storage {
    private static Memory memory = null;
    private static Filesystem filesystem = null;

    private Storage() {
        throw new AssertionError("This is a utility class");
    }

    /**
     * Get the global volatile (cleared after restart) memory storage for the robot.
     *
     * @return Instance for volatile memory storage
     */
    public static Memory memory() {
        if (memory == null)
            memory = new Memory();
        return memory;
    }

    /**
     * Get the global persistent (saved after restart) filesystem storage for the robot.
     * Storage information is saved in the robot controller internal storage.
     *
     * @return Instance for persistent storage
     */
    public static Filesystem filesystem() {
        if (filesystem == null)
            filesystem = new Filesystem();
        return filesystem;
    }

    /**
     * Represents in-memory storage for the robot.
     */
    public static class Memory {
        /**
         * Static array of hardware errors stored via hardware name.
         *
         * @see RobotConfig
         */
        public final ArrayList<String> hardwareErrors = new ArrayList<>();
        private final HashMap<String, Object> store = new HashMap<>();
        /**
         * The last known player Alliance.
         *
         * @see StartingConfiguration
         */
        public StartingConfiguration.Alliance lastKnownAlliance = null;
        /**
         * The last known position of the robot from odometry localization.
         * Defaults to the origin.
         *
         * @see Localizer
         */
        @NonNull
        public Pose2d lastKnownPosition = Geometry.zeroPose();

        private Memory() {
        }

        /**
         * Clear all volatile memory related to the robot.
         */
        public void clear() {
            store.clear();
            hardwareErrors.clear();
            lastKnownAlliance = null;
            lastKnownPosition = Geometry.zeroPose();
        }

        /**
         * Clear the volatile HashMap.
         */
        public void clearVolatile() {
            store.clear();
        }

        /**
         * Get a volatile value from memory stored by key in {@link #setVolatile(String, Object)}.
         *
         * @param key the key to search for
         * @return the value associated with the key
         * @throws IllegalArgumentException if key not found
         */
        public Object getVolatile(String key) throws IllegalArgumentException {
            if (!store.containsKey(key))
                throw new IllegalArgumentException("Key not found in memory: " + key);
            return store.get(key);
        }

        /**
         * Set a volatile value in memory stored by key.
         *
         * @param key   the key to store the value under
         * @param value the value to store
         */
        public void setVolatile(String key, Object value) {
            store.put(key, value);
        }
    }

    /**
     * Represents persistent, file-saved storage for the robot.
     * Note: In this virtual configuration, filesystem storage is not available and the store is infact volatile.
     */
    public static class Filesystem implements Closeable {
        private final HashMap<String, Object> store = new HashMap<>();

        private Filesystem() {
        }

        /**
         * Access the HashMap of all stored values in the filesystem.
         * When this resource is closed, the values are saved to the file, so ensure to use
         * a try-with-resources block or call {@link #close()} when done.
         * <b>Warning</b>: Trying to add objects to the HashMap that are not serializable by Gson will throw an exception on write,
         * and may cause in the corruption of the storage file (other valid objects may be lost)
         *
         * @return the stored values
         */
        public HashMap<String, Object> access() {
            return store;
        }

        /**
         * Delete all persistent storage related to the robot.
         * This will close the stream and save the empty store to the file.
         *
         * @throws RuntimeException if the storage file cannot be deleted
         */
        public void delete() {
            store.clear();
            filesystem = null;
        }

        @Override
        public void close() {

        }
    }
}
