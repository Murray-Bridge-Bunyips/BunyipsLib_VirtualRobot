/*
Copyright (c) 2016 Robert Atkinson

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Robert Atkinson nor the names of his contributors may be used to
endorse or promote products derived from this software without specific prior
written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

/*
Modified by FTC Team Beta 8397 for use in the Virtual_Robot Simulator
 */

package com.qualcomm.robotcore.hardware;

import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;

/**
 * DcMotor is an abridged version of the FTC DcMotor interface.
 */
public interface DcMotor extends DcMotorSimple {

    /**
     * Enum of operation modes available for DcMotor.
     * Note: RUN_USING_ENCODER and RUN_WITHOUT_ENCODER will behave the same in this simulator. For real
     *     robot programming, they will behave very differently, and it's essential to choose the appropriate
     *     mode. RUN_TO_POSITION is now implemented in the simulator. Setting mode to STOP_AND_RESET_ENCODER
     *     will set the power to zero and zero the encoder. To run the motor again, the mode must be set to
     *     either RUN_USING_ENCODER, RUN_WITHOUT_ENCODER, or RUN_TO_POSITION.
     */
    enum RunMode
    {
        /** The motor is simply to run at whatever velocity is achieved by apply a particular
         * power level to the motor.
         */
        RUN_WITHOUT_ENCODER,

        /** The motor is to do its best to run at targeted velocity. An encoder must be affixed
         * to the motor in order to use this mode. This is a PID mode.
         */
        RUN_USING_ENCODER,

        /** The motor is to attempt to rotate in whatever direction is necessary to cause the
         * encoder reading to advance or retreat from its current setting to the setting which
         * has been provided through the {@link #setTargetPosition(int) setTargetPosition()} method.
         * An encoder must be affixed to this motor in order to use this mode. This is a PID mode.
         */
        RUN_TO_POSITION,

        /** The motor is to set the current encoder position to zero. In contrast to
         * {@link com.qualcomm.robotcore.hardware.DcMotor.RunMode#RUN_TO_POSITION RUN_TO_POSITION},
         * the motor is not rotated in order to achieve this; rather, the current rotational
         * position of the motor is simply reinterpreted as the new zero value. However, as
         * a side effect of placing a motor in this mode, power is removed from the motor, causing
         * it to stop, though it is unspecified whether the motor enters brake or float mode.
         *
         * Further, it should be noted that setting a motor to{@link RunMode#STOP_AND_RESET_ENCODER
         * STOP_AND_RESET_ENCODER} may or may not be a transient state: motors connected to some motor
         * controllers will remain in this mode until explicitly transitioned to a different one, while
         * motors connected to other motor controllers will automatically transition to a different
         * mode after the reset of the encoder is complete.
         */
        STOP_AND_RESET_ENCODER,

        /** @deprecated Use {@link #RUN_WITHOUT_ENCODER} instead */
        @Deprecated RUN_WITHOUT_ENCODERS,

        /** @deprecated Use {@link #RUN_USING_ENCODER} instead */
        @Deprecated RUN_USING_ENCODERS,

        /** @deprecated Use {@link #STOP_AND_RESET_ENCODER} instead */
        @Deprecated RESET_ENCODERS;

        /** Returns the new new constant corresponding to old constant names.
         * @deprecated Replace use of old constants with new */
        @Deprecated
        public RunMode migrate()
        {
            switch (this)
            {
                case RUN_WITHOUT_ENCODERS: return RUN_WITHOUT_ENCODER;
                case RUN_USING_ENCODERS: return RUN_USING_ENCODER;
                case RESET_ENCODERS: return STOP_AND_RESET_ENCODER;
                default: return this;
            }
        }

        /**
         * Returns whether this RunMode is a PID-controlled mode or not
         * @return whether this RunMode is a PID-controlled mode or not
         */
        public boolean isPIDMode()
        {
            return this==RUN_USING_ENCODER || this==RUN_USING_ENCODERS || this==RUN_TO_POSITION;
        }
    }

    /**
     * Enum of ZeroPowerBehavior
     * Note: this will have no effect on the function of the simulator
     */
    public enum ZeroPowerBehavior {BRAKE, FLOAT, UNKNOWN}

    /**
     * Set operation mode of the motor.
     */
    public void setMode(RunMode mode);

    /**
     * Get the operation mode of the motor.
     * @return Operation Mode
     */
    public RunMode getMode();

    /**
     * Get current motor position (i.e., encoder ticks)
     * @return encoder ticks
     */
    public int getCurrentPosition();

    /**
     * Set the target position (in encoder ticks) for RUN_TO_POSITION mode
     * @param pos target position
     */
    public void setTargetPosition(int pos);

    /**
     * Get the target position (in encoder ticks) for RUN_TO_POSITION mode
     * @return target position
     */
    public int getTargetPosition();

    /**
     * Indicates whether motor is actively approaching a target position in RUN_TO_POSITION mode
     * @return True if actively approaching a target
     */
    public boolean isBusy();

    /**
     * Set the ZeroPowerBehavior of the DcMotor
     * @param zeroPowerBehavior
     */
    public void setZeroPowerBehavior( ZeroPowerBehavior zeroPowerBehavior);

    /**
     * Get the ZeroPowerBehavior of the DcMotor
     * @return the current ZeroPowerBehavior of the DcMotor
     */
    public ZeroPowerBehavior getZeroPowerBehavior();

    /**
     * Get the MotorConfigurationType of the DcMotor
     * @return the MotorConfigurationType
     */
    public MotorConfigurationType getMotorType();

    default void setMotorType(MotorConfigurationType type) {}


    /**
     * Returns the underlying motor controller on which this motor is situated.
     * @return the underlying motor controller on which this motor is situated.
     * @see #getPortNumber()
     */
    DcMotorController getController();

    /**
     * Returns the port number on the underlying motor controller on which this motor is situated.
     * @return the port number on the underlying motor controller on which this motor is situated.
     * @see #getController()
     */
    int getPortNumber();

    @Deprecated
    void setPowerFloat();

    @Deprecated
    boolean getPowerFloat();
}
