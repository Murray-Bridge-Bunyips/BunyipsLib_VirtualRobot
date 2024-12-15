## TeamCode

This is a reflected copy of the core components of https://github.com/Murray-Bridge-Bunyips/BunyipsFTC.<br><br>
BunyipsLib is under `bunyipslib/`, and there is
a bot under `imposter/` to test a virtual robot configuration, as other robots won't work with this simulator due to
configuration differences. Features from the SDK that aren't supported have been replicated to return null or similar,
and may cause null exceptions if used. This includes all the Vision systems (VisionPortal will not build any cameras).

### Porting BunyipsLib
When copying BunyipsLib to org.murraybridgebunyips, you will need to do the following:
1. Copy the `bunyipslib` folder to `TeamCode/src/org/murraybridgebunyips/`
2. Remove the `throwIfModulesAreOutdated(hardwareMap)` invocation and import from `bunyipslib/BunyipsOpMode.kt`
3. Import `deps/BuildConfig` in `bunyipslib/BunyipsOpMode.kt`, `bunyipslib/Exceptions.java`, `bunyipslib/Scheduler.java`
4. Comment out instances of `FtcDashboard.getInstance()` accesses in `bunyipslib/DualTelemetry.kt`, `bunyipslib/integrated/VisionTest.java`, `bunyipslib/vision/SwitchableVisionSender.java`
5. Comment out the entire `bunyipslib/roadrunner/tuning/RoadRunnerTuningOpMode.java` file
6. Change the `bunyipslib/RobotConfig.kt` `getLazyIMU` method to return null
7. Comment out instances of `FlightRecorder` in `bunyipslib/localization/TwoWheelLocalizer.java`, `bunyipslib/localization/ThreeWheelLocalizer.java`, `bunyipslib/localization/TankLocalizer.java`, `bunyipslib/localization/MecanumLocalizer.java`
8. Comment out instances of `FlightRecorder` and `DownsampledWriter` in `bunyipslib/subsystems/drive/MecanumDrive.java` and `bunyipslib/subsystems/drive/TankDrive.java`
9. Comment out instances of `DownsampledWriter` in `bunyipslib/localization/accumulators/Accumulator.java`
10. Revert any changes done to `bunyipslib/Storage.java`
11. Revert any changes done to `bunyipslib/ServoEx.java`
12. Revert any changes done to `bunyipslib/integrated/HardwareTest.java`
13. Comment out various uses of `ReflectionConfig` across BunyipsLib
14. Replace `user` with `getUser()` in `bunyipslib/Scheduler.kt` errors
15. Comment out further errors

BunyipsLib will now compile, although missing some features and stubbing others.
An alternative & faster method of converting changes can be accomplished via the Patch system via JetBrains IDEs.
