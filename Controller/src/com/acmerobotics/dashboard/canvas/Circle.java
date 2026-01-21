package com.acmerobotics.dashboard.canvas;

public class Circle extends CanvasOp {
    public double x;
    public double y;
    public double radius;
    public boolean stroke;

    public Circle(double x, double y, double radius, boolean stroke) {
        super(Type.CIRCLE);

        this.x = x;
        this.y = y;
        this.radius = radius;
        this.stroke = stroke;
    }
}
