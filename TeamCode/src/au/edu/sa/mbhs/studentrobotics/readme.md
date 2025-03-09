## TeamCode

This is a reflected copy of the core components of https://github.com/Murray-Bridge-Bunyips/BunyipsFTC.<br><br>
BunyipsLib is under `bunyipslib/`, and there is
a bot under `imposter/` to test a virtual robot configuration, as other robots won't work with this simulator due to
configuration differences. Features from the SDK that aren't supported have been replicated to return null or similar,
and may cause null exceptions if used. This includes all the Vision systems (VisionPortal will not build any cameras).

### Porting BunyipsLib
When copying BunyipsLib to org.murraybridgebunyips, you will need to do the following:
1. Replace the `bunyipslib` directory in `TeamCode/src/au/edu/sa/mbhs/studentrobotics`
2. Copy and paste `deps/BuildConfig` into the pasted `bunyipslib` directory

BunyipsLib will now compile, although missing some features and stubbing others.
Alternatively, porting diff changes can be accomplished via the Patch system in JetBrains IDEs.
