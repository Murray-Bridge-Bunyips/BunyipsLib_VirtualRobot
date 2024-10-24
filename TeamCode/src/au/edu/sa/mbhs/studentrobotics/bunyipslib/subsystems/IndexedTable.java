package au.edu.sa.mbhs.studentrobotics.bunyipslib.subsystems;


import androidx.annotation.NonNull;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.BunyipsSubsystem;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.EmergencyStop;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.RunTask;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.bases.Task;

/**
 * Multi-purpose index-based table with increment and decrement functionality.
 *
 * @author Lucas Bubner, 2024
 * @since 1.0.0-pre
 */
public class IndexedTable extends BunyipsSubsystem {
    /**
     * Tasks for IndexedTable.
     */
    public final Tasks tasks = new Tasks();

    private final double[] tableValues;
    private int index = 0;

    /**
     * Create a new IndexedTable.
     *
     * @param tableValues the values to use, when the index is selected the corresponding value is used
     */
    public IndexedTable(@NonNull double... tableValues) {
        this.tableValues = tableValues;
    }

    /**
     * Set the default index of the indexed table. Implicit default of zero.
     *
     * @param defaultIndex the index to set
     * @return this
     */
    @NonNull
    public IndexedTable withDefaultIndex(int defaultIndex) {
        if (defaultIndex < 0 || defaultIndex >= tableValues.length)
            throw new EmergencyStop("Default index out of bounds");
        index = defaultIndex;
        return this;
    }

    /**
     * Increment the table index.
     */
    public void increment() {
        if (index >= tableValues.length - 1) return;
        index++;
    }

    /**
     * Decrement the table index.
     */
    public void decrement() {
        if (index <= 0) return;
        index--;
    }

    /**
     * Set the table index.
     *
     * @param index the index to set
     */
    public void set(int index) {
        if (index < 0 || index >= tableValues.length) {
            throw new IllegalArgumentException("Index out of bounds");
        }
        this.index = index;
    }

    /**
     * Get the applied value from the index.
     *
     * @return the table value
     */
    public double get() {
        return tableValues[index];
    }

    /**
     * Optionally update telemetry with the current value and index.
     */
    @Override
    protected void periodic() {
        opMode(o -> o.telemetry.add(
                "%: % <font color='gray'>(%/%)</font>",
                name,
                tableValues[index],
                index + 1,
                tableValues.length
        ));
    }

    /**
     * Tasks for IndexedTable, access with {@link #tasks}.
     */
    public class Tasks {
        /**
         * Create a task to decrement the table index.
         *
         * @return the task
         */
        @NonNull
        public Task decrement() {
            return new RunTask(IndexedTable.this::decrement)
                    .onSubsystem(IndexedTable.this, false)
                    .withName("Decrement Index");
        }

        /**
         * Create a task to increment the table index.
         *
         * @return the task
         */
        @NonNull
        public Task increment() {
            return new RunTask(IndexedTable.this::increment)
                    .onSubsystem(IndexedTable.this, false)
                    .withName("Increment Index");
        }
    }
}
