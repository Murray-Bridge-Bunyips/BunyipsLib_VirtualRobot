## TeamCode

This is a reflected copy of the core components of https://github.com/Murray-Bridge-Bunyips/BunyipsFTC.<br><br>
BunyipsLib is under `bunyipslib/`, and there is
a bot under `imposter/` to test a virtual robot configuration, as other robots won't work with this simulator due to
configuration differences. Features from the SDK that aren't supported have been replicated to return null or similar,
and may cause null exceptions if used. This includes all the Vision systems (VisionPortal will not build any cameras).

### Porting BunyipsLib
When copying BunyipsLib to org.murraybridgebunyips, you will need to do the following:
1. Copy the `bunyipslib` folder to `TeamCode/src/org/murraybridgebunyips/`
2. Import `deps/BuildConfig` in `BunyipsOpMode.kt`, `Exceptions.kt` and `Scheduler.kt`
3. Remove import from `BunyipsLib.kt` for `BuildConfig` and replace it with the one from `deps/BuildConfig`

BunyipsLib will now compile, although missing some features and stubbing others.
An alternative & faster method of converting changes can be accomplished via the Patch system via JetBrains IDEs.
