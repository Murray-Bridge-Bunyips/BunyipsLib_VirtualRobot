package org.murraybridgebunyips.bunyipslib.roadrunner.trajectorysequence;

import androidx.annotation.Nullable;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.canvas.Canvas;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.control.PIDCoefficients;
import com.acmerobotics.roadrunner.control.PIDFController;
import com.acmerobotics.roadrunner.drive.DriveSignal;
import com.acmerobotics.roadrunner.followers.TrajectoryFollower;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.profile.MotionState;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.acmerobotics.roadrunner.trajectory.TrajectoryMarker;
import com.acmerobotics.roadrunner.util.NanoClock;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import org.murraybridgebunyips.bunyipslib.DualTelemetry;
import org.murraybridgebunyips.bunyipslib.roadrunner.trajectorysequence.sequencesegment.SequenceSegment;
import org.murraybridgebunyips.bunyipslib.roadrunner.trajectorysequence.sequencesegment.TrajectorySegment;
import org.murraybridgebunyips.bunyipslib.roadrunner.trajectorysequence.sequencesegment.TurnSegment;
import org.murraybridgebunyips.bunyipslib.roadrunner.trajectorysequence.sequencesegment.WaitSegment;
import org.murraybridgebunyips.bunyipslib.roadrunner.util.DashboardUtil;
import org.murraybridgebunyips.deps.LogFiles;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * RoadRunner trajectory sequence runner.
 *
 * @since 1.0.0-pre
 */
@Config
public class TrajectorySequenceRunner {
    /**
     * FtcDashboard inactivity color for trajectory segments.
     */
    public static String COLOR_INACTIVE_TRAJECTORY = "#4caf507a";
    /**
     * FtcDashboard inactivity color for turn segments.
     */
    public static String COLOR_INACTIVE_TURN = "#7c4dff7a";
    /**
     * FtcDashboard inactivity color for wait segments.
     */
    public static String COLOR_INACTIVE_WAIT = "#dd2c007a";

    /**
     * FtcDashboard activity color for trajectory segments.
     */
    public static String COLOR_ACTIVE_TRAJECTORY = "#4CAF50";
    /**
     * FtcDashboard activity color for turn segments.
     */
    public static String COLOR_ACTIVE_TURN = "#7c4dff";
    /**
     * FtcDashboard activity color for wait segments.
     */
    public static String COLOR_ACTIVE_WAIT = "#dd2c00";

    /**
     * The maximum number of poses to store in the history.
     */
    public static int POSE_HISTORY_LIMIT = 1000;

    private final TrajectoryFollower follower;

    private final PIDFController turnController;
    private final DualTelemetry telemetry;

    private final NanoClock clock;
    private final FtcDashboard dashboard;
    private final LinkedList<Pose2d> poseHistory = new LinkedList<>();
    private final VoltageSensor voltageSensor;
    private final List<Integer> lastDriveEncPositions;
    private final List<Integer> lastDriveEncVels;
    private final List<Integer> lastTrackingEncPositions;
    private final List<Integer> lastTrackingEncVels;
    private final boolean driveConstantsRunUsingEncoder;
    List<TrajectoryMarker> remainingMarkers = new ArrayList<>();
    private TrajectorySequence currentTrajectorySequence;
    private double currentSegmentStartTime;
    private int currentSegmentIndex;
    private int lastSegmentIndex;
    private Pose2d lastPoseError = new Pose2d();

    /**
     * Create a new trajectory sequence runner.
     *
     * @param telemetry                     The (optional) DualTelemetry instance to use for telemetry.
     * @param driveConstantsRunUsingEncoder Whether the drive constants are run using encoders.
     * @param follower                      The trajectory follower to use.
     * @param headingPIDCoefficients        The PID coefficients for the heading controller.
     * @param voltageSensor                 The voltage sensor to use.
     * @param lastDriveEncPositions         The last drive encoder positions.
     * @param lastDriveEncVels              The last drive encoder velocities.
     * @param lastTrackingEncPositions      The last tracking encoder positions.
     * @param lastTrackingEncVels           The last tracking encoder velocities.
     */
    public TrajectorySequenceRunner(
            @Nullable DualTelemetry telemetry, boolean driveConstantsRunUsingEncoder, TrajectoryFollower follower, PIDCoefficients headingPIDCoefficients, VoltageSensor voltageSensor,
            List<Integer> lastDriveEncPositions, List<Integer> lastDriveEncVels, List<Integer> lastTrackingEncPositions, List<Integer> lastTrackingEncVels
    ) {
        this.telemetry = telemetry;
        this.follower = follower;
        this.driveConstantsRunUsingEncoder = driveConstantsRunUsingEncoder;

        turnController = new PIDFController(headingPIDCoefficients);
        turnController.setInputBounds(0, 2 * Math.PI);

        this.voltageSensor = voltageSensor;

        this.lastDriveEncPositions = lastDriveEncPositions;
        this.lastDriveEncVels = lastDriveEncVels;
        this.lastTrackingEncPositions = lastTrackingEncPositions;
        this.lastTrackingEncVels = lastTrackingEncVels;

        clock = NanoClock.system();

        dashboard = FtcDashboard.getInstance();
        dashboard.setTelemetryTransmissionInterval(25);
    }

    /**
     * Follow a trajectory sequence asynchronously.
     *
     * @param trajectorySequence The trajectory sequence to follow.
     */
    public void followTrajectorySequenceAsync(TrajectorySequence trajectorySequence) {
        currentTrajectorySequence = trajectorySequence;
        currentSegmentStartTime = clock.seconds();
        currentSegmentIndex = 0;
        lastSegmentIndex = -1;
    }

    /**
     * Update the trajectory sequence runner.
     *
     * @param poseEstimate The pose estimate.
     * @param poseVelocity The pose velocity.
     * @return The drive signal to set the motors to.
     */
    @Nullable
    public DriveSignal update(Pose2d poseEstimate, Pose2d poseVelocity) {
        Pose2d targetPose = null;
        DriveSignal driveSignal = null;

        SequenceSegment currentSegment = null;

        if (currentTrajectorySequence != null) {
            if (currentSegmentIndex >= currentTrajectorySequence.size()) {
                for (TrajectoryMarker marker : remainingMarkers) {
                    marker.getCallback().onMarkerReached();
                }

                remainingMarkers.clear();

                currentTrajectorySequence = null;
            }

            if (currentTrajectorySequence == null)
                return new DriveSignal();

            double now = clock.seconds();
            boolean isNewTransition = currentSegmentIndex != lastSegmentIndex;

            currentSegment = currentTrajectorySequence.get(currentSegmentIndex);

            if (isNewTransition) {
                currentSegmentStartTime = now;
                lastSegmentIndex = currentSegmentIndex;

                for (TrajectoryMarker marker : remainingMarkers) {
                    marker.getCallback().onMarkerReached();
                }

                remainingMarkers.clear();

                remainingMarkers.addAll(currentSegment.getMarkers());
                remainingMarkers.sort(Comparator.comparingDouble(TrajectoryMarker::getTime));
            }

            double deltaTime = now - currentSegmentStartTime;

            if (currentSegment instanceof TrajectorySegment) {
                Trajectory currentTrajectory = ((TrajectorySegment) currentSegment).getTrajectory();

                if (isNewTransition)
                    follower.followTrajectory(currentTrajectory);

                if (!follower.isFollowing()) {
                    currentSegmentIndex++;

                    driveSignal = new DriveSignal();
                } else {
                    driveSignal = follower.update(poseEstimate, poseVelocity);
                    lastPoseError = follower.getLastError();
                }

                targetPose = currentTrajectory.get(deltaTime);
            } else if (currentSegment instanceof TurnSegment) {
                MotionState targetState = ((TurnSegment) currentSegment).getMotionProfile().get(deltaTime);

                turnController.setTargetPosition(targetState.getX());

                double correction = turnController.update(poseEstimate.getHeading());

                double targetOmega = targetState.getV();
                double targetAlpha = targetState.getA();

                lastPoseError = new Pose2d(0, 0, turnController.getLastError());

                Pose2d startPose = currentSegment.getStartPose();
                targetPose = startPose.copy(startPose.getX(), startPose.getY(), targetState.getX());

                driveSignal = new DriveSignal(
                        new Pose2d(0, 0, targetOmega + correction),
                        new Pose2d(0, 0, targetAlpha)
                );

                if (deltaTime >= currentSegment.getDuration()) {
                    currentSegmentIndex++;
                    driveSignal = new DriveSignal();
                }
            } else if (currentSegment instanceof WaitSegment) {
                lastPoseError = new Pose2d();

                targetPose = currentSegment.getStartPose();
                driveSignal = new DriveSignal();

                if (deltaTime >= currentSegment.getDuration()) {
                    currentSegmentIndex++;
                }
            }

            while (!remainingMarkers.isEmpty() && deltaTime > remainingMarkers.get(0).getTime()) {
                remainingMarkers.get(0).getCallback().onMarkerReached();
                remainingMarkers.remove(0);
            }
        }

        poseHistory.add(poseEstimate);

        if (POSE_HISTORY_LIMIT > -1 && poseHistory.size() > POSE_HISTORY_LIMIT) {
            poseHistory.removeFirst();
        }

        final double NOMINAL_VOLTAGE = 12.0;
        double voltage = voltageSensor.getVoltage();
        if (driveSignal != null && !driveConstantsRunUsingEncoder) {
            driveSignal = new DriveSignal(
                    driveSignal.getVel().times(NOMINAL_VOLTAGE / voltage),
                    driveSignal.getAccel().times(NOMINAL_VOLTAGE / voltage)
            );
        }

        if (targetPose != null) {
            LogFiles.record(
                    targetPose, poseEstimate, voltage,
                    lastDriveEncPositions, lastDriveEncVels, lastTrackingEncPositions, lastTrackingEncVels
            );
        }

        if (telemetry != null) {
            telemetry.addDashboard("x", poseEstimate.getX());
            telemetry.addDashboard("y", poseEstimate.getY());
            telemetry.addDashboard("heading (deg)", Math.toDegrees(poseEstimate.getHeading()));

            telemetry.addDashboard("xError", lastPoseError.getX());
            telemetry.addDashboard("yError", lastPoseError.getY());
            telemetry.addDashboard("headingError (deg)", Math.toDegrees(lastPoseError.getHeading()));

            draw(telemetry.dashboardFieldOverlay(), currentTrajectorySequence, currentSegment, targetPose, poseEstimate);
        } else {
            // Using normal OpMode, we can send packets directly
            TelemetryPacket packet = new TelemetryPacket();
            Canvas fieldOverlay = packet.fieldOverlay();

            packet.put("x", poseEstimate.getX());
            packet.put("y", poseEstimate.getY());
            packet.put("heading (deg)", Math.toDegrees(poseEstimate.getHeading()));
            packet.put("xError", lastPoseError.getX());
            packet.put("yError", lastPoseError.getY());
            packet.put("headingError (deg)", Math.toDegrees(lastPoseError.getHeading()));

            draw(fieldOverlay, currentTrajectorySequence, currentSegment, targetPose, poseEstimate);
            dashboard.sendTelemetryPacket(packet);
        }

        return driveSignal;
    }

    private void draw(
            Canvas fieldOverlay,
            TrajectorySequence sequence, SequenceSegment currentSegment,
            Pose2d targetPose, Pose2d poseEstimate
    ) {
        if (sequence != null) {
            for (int i = 0; i < sequence.size(); i++) {
                SequenceSegment segment = sequence.get(i);

                if (segment instanceof TrajectorySegment) {
                    fieldOverlay.setStrokeWidth(1);
                    fieldOverlay.setStroke(COLOR_INACTIVE_TRAJECTORY);

                    DashboardUtil.drawSampledPath(fieldOverlay, ((TrajectorySegment) segment).getTrajectory().getPath());
                } else if (segment instanceof TurnSegment) {
                    Pose2d pose = segment.getStartPose();

                    fieldOverlay.setFill(COLOR_INACTIVE_TURN);
                    fieldOverlay.fillCircle(pose.getX(), pose.getY(), 2);
                } else if (segment instanceof WaitSegment) {
                    Pose2d pose = segment.getStartPose();

                    fieldOverlay.setStrokeWidth(1);
                    fieldOverlay.setStroke(COLOR_INACTIVE_WAIT);
                    fieldOverlay.strokeCircle(pose.getX(), pose.getY(), 3);
                }
            }
        }

        if (currentSegment != null) {
            if (currentSegment instanceof TrajectorySegment) {
                Trajectory currentTrajectory = ((TrajectorySegment) currentSegment).getTrajectory();

                fieldOverlay.setStrokeWidth(1);
                fieldOverlay.setStroke(COLOR_ACTIVE_TRAJECTORY);

                DashboardUtil.drawSampledPath(fieldOverlay, currentTrajectory.getPath());
            } else if (currentSegment instanceof TurnSegment) {
                Pose2d pose = currentSegment.getStartPose();

                fieldOverlay.setFill(COLOR_ACTIVE_TURN);
                fieldOverlay.fillCircle(pose.getX(), pose.getY(), 3);
            } else if (currentSegment instanceof WaitSegment) {
                Pose2d pose = currentSegment.getStartPose();

                fieldOverlay.setStrokeWidth(1);
                fieldOverlay.setStroke(COLOR_ACTIVE_WAIT);
                fieldOverlay.strokeCircle(pose.getX(), pose.getY(), 3);
            }
        }

        if (targetPose != null) {
            fieldOverlay.setStrokeWidth(1);
            fieldOverlay.setStroke("#4CAF50");
            DashboardUtil.drawRobot(fieldOverlay, targetPose);
        }

        fieldOverlay.setStroke("#3F51B5");
        DashboardUtil.drawPoseHistory(fieldOverlay, poseHistory);

        fieldOverlay.setStroke("#3F51B5");
        DashboardUtil.drawRobot(fieldOverlay, poseEstimate);
    }

    public Pose2d getLastPoseError() {
        return lastPoseError;
    }

    public boolean isBusy() {
        return currentTrajectorySequence != null;
    }

    /**
     * Abort the current trajectory sequence.
     */
    public void cancelTrajectory() {
        currentTrajectorySequence = null;
        remainingMarkers.clear();
    }
}
