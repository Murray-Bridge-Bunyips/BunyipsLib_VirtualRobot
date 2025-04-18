// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.collections;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * A variant on {@code java.util.HashMap<K, V>} that uses primitive long ints for map keys instead
 * of autoboxed Long objects like would be used for a {@code Map<Long, V>}.
 *
 * @param <V> the type of the values stored in the map
 * @since 1.0.0-pre
 */
public class LongToObjectHashMap<V> {
    private static final int INITIAL_SIZE = 0;
    private static final int INITIAL_CAPACITY = 8; // NOTE: Must be a power of two

    /**
     * The default load factor of the hashmap. If the ratio of the number of entries to the map's
     * capacity exceeds this value, the map will be resized (doubled capacity) in order for more
     * values to be easily inserted.
     */
    private static final double LOAD_FACTOR = 75.00 / 100;

    /**
     * The current number of key-value pairs in the map.
     */
    private int size = INITIAL_SIZE;

    /**
     * The current maximum capacity of the map. Note that it will be resized before size reaches
     * this value.
     */
    private int capacity = INITIAL_CAPACITY;

    /**
     * The keys in the map. This is a sparse array, and the location of a key may not be equal to the
     * result of calling {@link #bucket(long)} on that key. To handle hash collisions, if a bucket is
     * already in use when trying to insert a value, the bucket number is incremented (wrapping around
     * to 0 if it's equal to capacity) and <i>that</i> bucket is checked to see if it's available.
     * This process continues until an empty bucket is found (which is guaranteed because size is
     * always less than capacity).
     */
    private long[] keys = new long[capacity];

    /**
     * Tracks which buckets are actually used (have a key-value mapping).
     */
    private boolean[] uses = new boolean[capacity];

    /**
     * The values in the map. See the documentation for keys for how indexing into this array works.
     */
    @SuppressWarnings("unchecked")
    private V[] values = (V[]) new Object[capacity];

    /**
     * Default constructor.
     */
    public LongToObjectHashMap() {
    }

    /**
     * Puts a value {@code value} corresponding to key {@code key} in the map.
     *
     * @param key   the associated key
     * @param value the value to insert
     * @return the previous value that was mapped to the key, or null if no such value existed
     */
    public V put(long key, V value) {
        int bucket = bucket(key);

        // Increment the bucket until we hit an open space (there's always going to be at least one)
        while (uses[bucket]) {
            if (keys[bucket] == key) {
                // Replace the existing value
                V oldValue = values[bucket];
                values[bucket] = value;
                return oldValue;
            }
            bucket = safeIncrement(bucket);
        }

        uses[bucket] = true;
        keys[bucket] = key;
        values[bucket] = value;
        size++;

        if (size > maxSize()) {
            grow();
        }
        return null;
    }

    /**
     * Gets the value associated with the given key.
     *
     * @param key the key to retrieve the value for
     * @return the value mapped to the key, or null if the key is not in the map
     */
    public V get(long key) {
        int bucket = bucket(key);
        while (uses[bucket]) {
            if (keys[bucket] == key) {
                // Found it
                return values[bucket];
            }
            bucket = safeIncrement(bucket);
        }
        return null;
    }

    /**
     * Removes the value associated with the given key and returns it.
     *
     * @param key the key to remove
     * @return the value corresponding to the key, or null if the key is not in the map
     */
    public V remove(long key) {
        int bucket = bucket(key);
        while (uses[bucket]) {
            if (keys[bucket] == key) {
                // Found it
                size--;
                keys[bucket] = 0L;
                uses[bucket] = false;

                V oldValue = values[bucket];
                values[bucket] = null;
                return oldValue;
            }
            bucket = safeIncrement(bucket);
        }

        return null;
    }

    /**
     * Checks if a key is contained in the map.
     *
     * @param key the key to check
     * @return true if the key has an associated value, false if not
     */
    public boolean containsKey(long key) {
        int bucket = bucket(key);
        while (uses[bucket]) {
            if (keys[bucket] == key) {
                return true;
            }
            bucket = safeIncrement(bucket);
        }
        return false;
    }

    /**
     * Clears and removes all entries from the map.
     */
    public void clear() {
        if (size == 0) {
            // Nothing to do
            return;
        }
        size = 0;

        Arrays.fill(uses, false);
        Arrays.fill(keys, 0L);
        Arrays.fill(values, null);
    }

    /**
     * Gets the number of key-value pairs currently contained in the map.
     *
     * @return the current size of the map
     */
    public int size() {
        return size;
    }

    /**
     * Gets the current capacity of the map. This is the maximum number of key-value pairs that can be.
     *
     * @return the current capacity of the map
     */
    public int capacity() {
        return capacity;
    }

    /**
     * Checks if the map contains any entries.
     *
     * @return true if at least one entry is present, false otherwise
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Gets the keys contained in the map. Ordering is not guaranteed. The returned set is read-only
     * and immutable. This uses a custom class for primitive long values to avoid unnecessary
     * autoboxing to {@code java.lang.Long}.
     *
     * @return a read-only set of keys
     */
    @NonNull
    public ReadOnlyPrimitiveLongSet keySet() {
        // Copy the sparse key array into a compact array
        long[] keys = new long[size];
        int i = 0;
        for (int bucket = 0; bucket < capacity; bucket++) {
            if (uses[bucket]) {
                keys[i] = keys[bucket];
                i++;
            }
        }

        return new ReadOnlyPrimitiveLongSet(keys);
    }

    /**
     * Gets the values contained in the map. Ordering is not guaranteed. The returned collection is
     * read-only and immutable.
     *
     * @return a read-only collection of values
     */
    @NonNull
    public Collection<V> values() {
        Collection<V> vals = new ArrayList<>();
        for (int bucket = 0; bucket < capacity; bucket++) {
            if (uses[bucket]) {
                vals.add(values[bucket]);
            }
        }
        return new ArrayList<>(vals);
    }

    /**
     * Iterates over every key-value pair in the map and passes them to the given function.
     *
     * @param function the function to apply to every key-value pair.
     */
    public void forEach(@NonNull IteratorFunction<? super V> function) {
        for (int bucket = 0; bucket < capacity; bucket++) {
            if (uses[bucket]) {
                function.accept(keys[bucket], values[bucket]);
            }
        }
    }

    private void grow() {
        int currentSize = size;
        int oldCapacity = capacity;
        if (oldCapacity * LOAD_FACTOR >= currentSize) {
            // We're below the maximum allowed size for the current capacity
            // Nothing to do
            return;
        }

        int newCapacity = oldCapacity * 2;
        int newMask = newCapacity - 1;

        boolean[] oldUses = uses;
        long[] oldKeys = keys;
        V[] oldValues = values;

        boolean[] newUses = new boolean[newCapacity];
        long[] newKeys = new long[newCapacity];
        @SuppressWarnings("unchecked")
        V[] newValues = (V[]) new Object[newCapacity];

        for (int oldBucket = 0; oldBucket < oldCapacity; oldBucket++) {
            if (!oldUses[oldBucket]) {
                // Bucket is empty, skip
                continue;
            }
            long key = oldKeys[oldBucket];
            V value = oldValues[oldBucket];

            int newBucket = (int) (hash(key) & newMask);
            while (newUses[newBucket]) {
                newBucket = (newBucket + 1) & newMask;
            }

            newUses[newBucket] = true;
            newKeys[newBucket] = key;
            newValues[newBucket] = value;
        }

        capacity = newCapacity;
        uses = newUses;
        keys = newKeys;
        values = newValues;
    }

    private int maxSize() {
        return (int) (capacity * LOAD_FACTOR);
    }

    /**
     * Calculates a hashcode for an input key. Does some bit shuffling to account for poor hash
     * functions.
     *
     * @param key the key to hash
     * @return a hashcode for the input key
     */
    private long hash(long key) {
        return 31 + (key ^ (key >>> 15) ^ (key >>> 31) ^ (key << 31));
    }

    /**
     * The mask to use when translating a hashcode to a bucket index. Relies on capacity being a
     * power of two.
     */
    private int mask() {
        return capacity - 1;
    }

    /**
     * Calculates the desired bucket index for a particular key. Does nothing to handle the case where
     * the calculated index is already in use by another key.
     *
     * @param key the key to get the bucket for
     * @return the desired bucket index
     */
    private int bucket(long key) {
        long hash = hash(key);
        return (int) (hash & mask());
    }

    /**
     * Increments a bucket index by 1, wrapping around to 0 if the index is already at the maximum.
     *
     * @param bucket the index to increment
     * @return the incremented bucket index
     */
    private int safeIncrement(int bucket) {
        return (bucket + 1) & mask();
    }

    /**
     * Interface for map iterator function.
     *
     * @param <V> Value type.
     */
    @FunctionalInterface
    public interface IteratorFunction<V> {
        /**
         * Accepts a key-value pair from the map.
         *
         * @param key   The key.
         * @param value The value.
         */
        void accept(long key, V value);
    }
}
