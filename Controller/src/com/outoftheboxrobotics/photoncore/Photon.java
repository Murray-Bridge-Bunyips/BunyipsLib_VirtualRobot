package com.outoftheboxrobotics.photoncore;

import java.lang.annotation.*;
/**
 * Add this to a {@link com.qualcomm.robotcore.eventloop.opmode.OpMode} in order to apply PhotonCore optimizations
 */
@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Photon {
    int maximumParallelCommands() default 8;

    boolean singleThreadOptimized() default true;
}
