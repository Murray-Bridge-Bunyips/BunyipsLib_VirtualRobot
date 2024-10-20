## TeamCode

This is a reflected copy of the core components of https://github.com/Murray-Bridge-Bunyips/BunyipsFTC.<br><br>
BunyipsLib is under `bunyipslib/`, default testing virtual_robot OpModes are under `virtual_robot/samples/`, and there is
a bot under `imposter/` to test a virtual robot configuration, as other robots won't work with this simulator due to
configuration differences. Features from the SDK that aren't supported have been replicated to return null or similar,
and may cause null exceptions if used. This includes all the Vision systems (VisionPortal will not build any cameras).

### Porting BunyipsLib
When copying BunyipsLib to org.murraybridgebunyips, you will need to do the following:
1. Copy the `bunyipslib` folder to `TeamCode/src/org/murraybridgebunyips/`
2. Import `deps/BuildConfig` in `bunyipslib/BunyipsOpMode.kt`, `bunyipslib/Text.java`, `bunyipslib/Scheduler.java`
3. Delete `bunyipslib/roadrunner/util/android/`, ignore warnings
4. Remove deleted import from `bunyipslib/roadrunner/drive/tuning/opmodes/AutomaticFeedforwardTuner.java` and import `deps/LoggingUtil`
5. Remove deleted import from `bunyipslib/roadrunner/trajectorysequence/TrajectorySequenceRunner.java` and import `deps/LogFiles`
6. Revert any changes done to `bunyipslib/Storage.java`
7. Revert any changes done to `bunyipslib/ProfiledServo.java`

BunyipsLib will now compile, although missing some features.
An alternative & faster method of converting changes can be accomplished via the Patch system via JetBrains IDEs.
