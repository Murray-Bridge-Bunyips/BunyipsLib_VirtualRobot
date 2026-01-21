package com.acmerobotics.dashboard.canvas;

public class Polyline extends CanvasOp {
    public double[] xPoints;
    public double[] yPoints;

    public Polyline(double[] xPoints, double[] yPoints) {
        super(Type.POLYLINE);

        this.xPoints = xPoints;
        this.yPoints = yPoints;
    }
}
