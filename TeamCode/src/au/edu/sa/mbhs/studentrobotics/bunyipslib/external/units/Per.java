// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units;

import androidx.annotation.NonNull;

import java.util.Objects;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.collections.LongToObjectHashMap;

/**
 * Generic combinatory unit type that represents the proportion of one unit to another, such as
 * Meters per Second or Radians per Celsius.
 *
 * <p>Note: {@link Velocity} is used to represent the velocity dimension, rather than {@code
 * Per<Distance, Time>}.
 *
 * @param <N> the type of the numerator unit
 * @param <D> the type of the denominator unit
 * @since 1.0.0-pre
 */
public class Per<N extends Unit<N>, D extends Unit<D>> extends Unit<Per<N, D>> {
    /**
     * Keep a cache of created instances so expressions like Volts.per(Meter) don't do any allocations
     * after the first.
     */
    @SuppressWarnings("rawtypes")
    private static final LongToObjectHashMap<Per> cache = new LongToObjectHashMap<>();
    private final N numerator;
    private final D denominator;

    /**
     * Creates a new proportional unit derived from the ratio of one unit to another. Consider using
     * {@link #combine} instead of manually calling this constructor.
     *
     * @param numerator   the numerator unit
     * @param denominator the denominator unit
     */
    protected Per(N numerator, D denominator) {
        super(numerator.isBaseUnit() && denominator.isBaseUnit() ? null : combine(numerator.baseUnit, denominator.baseUnit),
                numerator.toBaseUnits(1) / denominator.toBaseUnits(1),
                numerator.name() + " per " + denominator.name(),
                numerator.symbol() + "/" + denominator.symbol());
        this.numerator = numerator;
        this.denominator = denominator;
    }

    Per(Per<N, D> baseUnit, UnaryFunction toBaseConverter, UnaryFunction fromBaseConverter, String name, String symbol) {
        super(baseUnit, toBaseConverter, fromBaseConverter, name, symbol);
        numerator = baseUnit.numerator();
        denominator = baseUnit.denominator();
    }

    /**
     * Creates a new Per unit derived from an arbitrary numerator and time denominator units. Using a
     * denominator with a unit of time is discouraged; use {@link Velocity} instead.
     *
     * <pre>
     *   Per.combine(Volts, Meters) // possible PID constant
     * </pre>
     *
     * <p>It's recommended to use the convenience function {@link Unit#per(Unit)} instead of calling
     * this factory directly.
     *
     * @param <N>         the type of the numerator unit
     * @param <D>         the type of the denominator unit
     * @param numerator   the numerator unit
     * @param denominator the denominator for unit time
     * @return the combined unit
     */
    @NonNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <N extends Unit<N>, D extends Unit<D>> Per<N, D> combine(N numerator, D denominator) {
        long key = ((long) numerator.hashCode()) << 32L | (denominator.hashCode() & 0xFFFFFFFFL);

        Per existing = cache.get(key);
        if (existing != null) {
            return existing;
        }

        Per newUnit = new Per<>(numerator, denominator);
        cache.put(key, newUnit);
        return newUnit;
    }

    /**
     * Gets the numerator unit. For a {@code Per<A, B>}, this will return the {@code A} unit.
     *
     * @return the numerator unit
     */
    public N numerator() {
        return numerator;
    }

    /**
     * Gets the denominator unit. For a {@code Per<A, B>}, this will return the {@code B} unit.
     *
     * @return the denominator unit
     */
    public D denominator() {
        return denominator;
    }

    /**
     * Returns the reciprocal of this Per.
     *
     * @return the reciprocal
     */
    @NonNull
    public Per<D, N> reciprocal() {
        return denominator.per(numerator);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        if (!super.equals(other)) {
            return false;
        }
        Per<?, ?> per = (Per<?, ?>) other;
        return Objects.equals(numerator, per.numerator) && Objects.equals(denominator, per.denominator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), numerator, denominator);
    }
}
