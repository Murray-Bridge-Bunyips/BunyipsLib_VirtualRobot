package au.edu.sa.mbhs.studentrobotics.deps;

import au.edu.sa.mbhs.studentrobotics.bunyipslib.annotations.Hook;
import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.canvas.*;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import virtual_robot.controller.VirtualField;
import virtual_robot.controller.VirtualRobotController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

// some useful dashboard/ftcdashboard replication tools
public class DashboardOverlay {
    /**
     * Whether to show a mimic of FTC dashboard overlays on top of the main field view. Doesn't support every call, but most essential ones.
     */
    public static final boolean SHOW_FTC_DASHBOARD_OVERLAYS = true;
    /**
     * Whether to hide the standard Virtual Robot (for example to instead only show the FTC dashboard robot overlay)
     */
    public static final boolean HIDE_STANDARD_ROBOT = false;
    /**
     * Update interval for FTC dashboard overlays (if enabled)
     */
    public static final long UPDATE_INTERVAL_MS = 25;
    
    
    private static Canvas overlayCanvas;
    
    // All (supported) FtcDashboard calls will be mimicked on top of the field when init() has been called. We just
    // use a Hook since it is convenient and this method only ever needs 1 invocation per simulator instance
    @SuppressWarnings("SuspiciousNameCombination")
    @Hook(on = Hook.Target.PRE_INIT, priority = Integer.MAX_VALUE)
    public static void init() {
        if (!SHOW_FTC_DASHBOARD_OVERLAYS || overlayCanvas != null) 
            return;
        Platform.runLater(() -> {
            Pane fieldPane = VirtualRobotController.instance.getFieldPane();
            overlayCanvas = new Canvas(VirtualField.FIELD_WIDTH, VirtualField.FIELD_WIDTH);
            fieldPane.getChildren().add(overlayCanvas);
        });
        FtcDashboard.getInstance().core.attachDodgySubscriptionToAllIncomingTelemetryData(it -> {
            for (TelemetryPacket packet : it) {
                List<CanvasOp> allOperations = Stream.concat(packet.field().getOperations().stream(),
                        packet.fieldOverlay().getOperations().stream()).toList();
                Platform.runLater(() -> {
                    if (overlayCanvas == null) return;
                    GraphicsContext gc = overlayCanvas.getGraphicsContext2D();
                    gc.clearRect(0, 0, VirtualField.FIELD_WIDTH, VirtualField.FIELD_WIDTH);

                    gc.save();
                    gc.translate(VirtualField.HALF_FIELD_WIDTH, VirtualField.HALF_FIELD_WIDTH);
                    gc.rotate(-90); // patch since it is the wrong way
                    gc.translate(-VirtualField.HALF_FIELD_WIDTH, -VirtualField.HALF_FIELD_WIDTH);

                    for (CanvasOp operation : allOperations) {
                        switch (operation.type) {
                            case STROKE_WIDTH ->
                                    gc.setLineWidth(((StrokeWidth) operation).width * 4); // arbitrary normalisation
                            case STROKE -> gc.setStroke(Color.web(((Stroke) operation).color));
                            case FILL -> gc.setFill(Color.web(((Fill) operation).color));
                            case POLYLINE -> {
                                double[] xPoints = ((Polyline) operation).xPoints;
                                double[] yPoints = ((Polyline) operation).yPoints;
                                gc.strokePolyline(convertXInchesToPx(xPoints), convertYInchesToPx(yPoints), xPoints.length);
                            }
                            case CIRCLE -> {
                                Circle c = (Circle) operation;
                                double cx = convertXInchesToPx(c.x);
                                double cy = convertYInchesToPx(c.y);
                                double r = c.radius * VirtualField.PIXELS_PER_INCH;
                                if (c.stroke) {
                                    gc.strokeOval(cx - r, cy - r, r * 2, r * 2);
                                } else {
                                    gc.fillOval(cx - r, cy - r, r * 2, r * 2);
                                }
                            }
                            default -> {
//                                System.out.println("unsupported op: " + operation.type);
                            }
                        }
                    }

                    gc.restore();
                });
            }
        });
    }

    private static double convertXInchesToPx(double fieldX) {
        return VirtualField.HALF_FIELD_WIDTH + fieldX * VirtualField.PIXELS_PER_INCH;
    }

    private static double convertYInchesToPx(double fieldY) {
        return VirtualField.HALF_FIELD_WIDTH - fieldY * VirtualField.PIXELS_PER_INCH;
    }

    private static double[] convertXInchesToPx(double[] fieldX) {
        return Arrays.stream(fieldX).map(DashboardOverlay::convertXInchesToPx).toArray();
    }

    private static double[] convertYInchesToPx(double[] fieldY) {
        return Arrays.stream(fieldY).map(DashboardOverlay::convertYInchesToPx).toArray();
    }
}