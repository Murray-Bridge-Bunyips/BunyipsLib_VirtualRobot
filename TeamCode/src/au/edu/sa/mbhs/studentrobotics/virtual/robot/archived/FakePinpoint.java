package au.edu.sa.mbhs.studentrobotics.virtual.robot.archived;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.BunyipsLib;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.Hook;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.GoBildaPinpointDriver;
import au.edu.sa.mbhs.studentrobotics.bunyipslib.external.Mathf;
import com.qualcomm.hardware.sparkfun.SparkFunOTOS;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchSimple;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class FakePinpoint implements I2cDeviceSynchSimple {
    private static final float CONVERSIONATION = 1000f;
    float accumX = 0;
    float accumY = 0;
    float accumR = 0;
    public FakePinpoint() {
    }

    @Hook(on = Hook.Target.PRE_INIT, priority = 3)
    private static void injectPinpoint() {
        BunyipsLib.getOpMode().hardwareMap.put("pinpoint", new GoBildaPinpointDriver(new FakePinpoint(), true));
    }
    // assume velocity is 0 since it auto resets

    @Override
    public void setI2cAddress(I2cAddr a) {

    }

    @Override
    public void write(Object a, Object b) {
        // yeah every write will reset the thing but oh well
        accumX = (float) SparkFunOTOS.rawPoseMR.x * CONVERSIONATION;
        accumY = (float) SparkFunOTOS.rawPoseMR.y * CONVERSIONATION;
        accumR = (float) SparkFunOTOS.rawPoseMR.h;
    }

    @Override
    public byte[] read(Object a, Object b) {
        ByteBuffer buffer = ByteBuffer.allocate(40).order(ByteOrder.LITTLE_ENDIAN);

        // do a bit of a sneaky
        float xPosition = (float) Mathf.round(SparkFunOTOS.rawPoseMR.x * CONVERSIONATION, 2) - accumX;
        float yPosition = (float) Mathf.round(SparkFunOTOS.rawPoseMR.y * CONVERSIONATION, 2) - accumY;
        float hOrientation = (float) Mathf.wrapDeltaRadians(Mathf.round(SparkFunOTOS.rawPoseMR.h, 2) - accumR);
        float xVelocity = (float) Mathf.round(SparkFunOTOS.rawVelMR.x * CONVERSIONATION, 2);
        float yVelocity = (float) Mathf.round(SparkFunOTOS.rawVelMR.y * CONVERSIONATION, 2);
        float hVelocity = (float) Mathf.round(SparkFunOTOS.rawVelMR.h, 2);

        buffer.putInt(1); // deviceStatus
        buffer.putInt(1000); // loopTime
        buffer.putInt((int) yPosition); // xEncoderValue
        buffer.putInt((int) -xPosition); // yEncoderValue
        buffer.putFloat(yPosition); // xPosition
        buffer.putFloat(-xPosition); // yPosition
        buffer.putFloat(hOrientation); // hOrientation
        buffer.putFloat(xVelocity); // xVelocity
        buffer.putFloat(yVelocity); // yVelocity
        buffer.putFloat(hVelocity); // hVelocity

        while (buffer.hasRemaining()) {
            buffer.put((byte) 0);
        }

        return buffer.array();

    }
}
