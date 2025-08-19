# BunyipsLib Virtual Robot
###### A 2D simulator to help beginning Java programmers learn to program for FTC Robotics with BunyipsLib.
    
![](/readme_image.jpg)


This is a JavaFX application forked from the virtual-robot project.
The repository can be git cloned then opened with IntelliJ. It can also be run using Android Studio 
(see this [video](https://www.youtube.com/watch?v=pmaT9Twbmao)).

Multiple robot configurations are available. MecanumBot has a mecanum drive with a servo-driven arm at the back, 
and with three dead-wheel encoders. XDriveBot has four corner-mounted Omni-wheels, a rear arm driven by a cr-servo, 
and three dead-wheel encoders. ArmBot is a mecanum drive bot with a motor-driven arm and servo-driven grabber. 

In general, BunyipsLib virtual robots will assume the default MecanumBot.

An approximation of the FTC SDK is provided as well as a version of BunyipsLib, with additional changes to support up to SDK v10.3.
Vision and other incompatible features are no-op or return null.
This simulator also has shims and ports of RoadRunner v1.0, Dairy Sloth, and Dashboard for code compatibility.

A BunyipsLib `RobotConfig` for the MecanumBot is available in `au.edu.sa.mbhs.studentrobotics.virtual.robot` package.
This configuration works exactly the same as a real robot running BunyipsLib, and new OpModes can be created in the same way as a normal bot within this package.

Several virtual-robot sample op modes are currently disabled (in the `archived` subpackage of the `virtual.robot` package), as well as a 
number of robot configurations. A robot configuration can be re-enabled by finding its class 
in the `virtual_robot.robots.classes` package, and un-commenting its `@BotConfig` annotation.

## Specifications

Each robot can be thought of as 18 inches wide.  For the two-wheel bot and mecanum wheel bots, the distance between
the centers of the right and left wheels is 16 inches. For the mecanum wheel bots, the distance between the centers
of the front and back wheels is 14 inches, and the mecanum wheels (when viewed from the top) have an "X" configuration.
For the X-Drive bot, the distance between the centers of any two adjacent wheels is 14.5 inches. Each motor has an
encoder. There is a downward-facing color sensor in the center of each robot. A BNO055 IMU is also
included. Each robot also has distance sensors on the front, left, right and back sides. A small green rectangle
indicates the front of each robot. Wheel diameters are all 4 inches. For the robots with dead-wheel encoders
(MecanumBot and XDriveBot), the forward-reverse encoder wheels are mounted 6 inches to the right and left of center,
while the X-direction (i.e., right-left) encoder wheel is mounted at the center. The dead-wheels are two inches in
diameter. Positioning of the dead-wheels can be changed easily in the robot configuration classes.

The field can be thought of as 12 feet wide. The field graphic (currently the INTO THE DEEP field)
is obtained from a bitmap (.bmp) image. The color sensor detects the field color beneath the center of the
robot. The field graphic is easily changed by providing a different .bmp image in the virtual_robot.config.Config class.
The .bmp image is the freight_field648.bmp file in the virtual_robot.assets folder. If a different .bmp image is used,
it must be at least as wide and as tall as the field dimensions (currently 648 x 648 pixels to fit on the screen of
most laptops). The Config class also allows selection between the use of "real" hardware gamepads versus a
"virtual gamepad". You can either use the Virtual Gamepad (currently the default), or use one or two real Gamepads; to use real
gamepads, open Controller/src/virtual_robot/config/Config.java and set USE_VIRTUAL_GAMEPAD to false. Then plug
in your gamepad(s).

In addition to the robot configurations described above, there is an additional configuration called
"ProgrammingBoard". It is meant to emulate the programming board described in the book "Learn Java For FTC", by
Alan Smith.  (The PDF can be [downloaded for free](https://github.com/alan412/LearnJavaForFTC) or you can purchase
the paperback on [Amazon](https://www.amazon.com/dp/B08DBVKXLZ).) It is a board with several hardware devices
attached: DcMotor, Servo, Potentiometer, Touch Sensor, and a Color-Distance Sensor. It also has a BNO055 IMU.
The board doesn't move around the field, but it can be rotated (to test the IMU) by dragging the board chassis.
