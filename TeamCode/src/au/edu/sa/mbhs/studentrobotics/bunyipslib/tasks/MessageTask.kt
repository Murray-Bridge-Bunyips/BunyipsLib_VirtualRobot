package au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks

import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.Mathf
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Measure
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Time
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.units.Units.Seconds
import au.edu.sa.mbhs.studentrobotics.bunyipslib.tasks.bases.Task
import au.edu.sa.mbhs.studentrobotics.bunyipslib.util.Text
import org.firstinspires.ftc.robotcore.external.Telemetry.Item

/**
 * Relay a message in telemetry for a specific amount of time.
 * @since 1.0.0-pre
 */
class MessageTask(time: Measure<Time>, private val message: String) : Task(time) {
    private var item: Item? = null

    init {
        withName("Message")
    }

    private fun buildString(): String {
        return Text.format("%/%s: %", Mathf.round(deltaTime.`in`(Seconds), 1), timeout.`in`(Seconds), message)
    }

    override fun init() {
        item = opMode?.telemetry?.addRetained(buildString())?.item
    }

    override fun periodic() {
        item?.setValue(buildString())
    }

    override fun isTaskFinished(): Boolean {
        return false
    }

    override fun onFinish() {
        if (item != null)
            require(opMode).telemetry.remove(item!!)
    }
}