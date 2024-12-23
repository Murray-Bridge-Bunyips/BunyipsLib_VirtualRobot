package au.edu.sa.mbhs.studentrobotics.bunyipslib.external;

import java.util.Map;
import java.util.TreeMap;

/**
 * A lookup table.
 * <p>
 * Utility from FTCLib
 * <a href="https://github.com/FTCLib/FTCLib/blob/master/core/src/main/java/com/arcrobotics/ftclib/util/LUT.java">Source</a>
 *
 * @param <T> the input type
 * @param <R> the output type
 * @since 1.0.0-pre
 */
public class LookupTable<T extends Number, R> extends TreeMap<T, R> {
    /**
     * Adds a key-value pair to the lookup table.
     *
     * @param key the input key
     * @param out the output value
     */
    public void add(T key, R out) {
        put(key, out);
    }

    /**
     * Returns the closest possible value for the given key.
     *
     * @param key the input key
     * @return the closest value to the input key
     */
    public R get(T key) {
        Map.Entry<T, R> ceil = ceilingEntry(key);
        Map.Entry<T, R> floor = floorEntry(key);

        if (ceil != null && floor != null) {
            double keyVal = key.doubleValue();
            double ceilDiff = Math.abs(ceil.getKey().doubleValue() - keyVal);
            double floorDiff = Math.abs(floor.getKey().doubleValue() - keyVal);
            return floorDiff < ceilDiff ? floor.getValue() : ceil.getValue();
        } else if (ceil != null) {
            return ceil.getValue();
        } else if (floor != null) {
            return floor.getValue();
        } else {
            return null;
        }
    }
}