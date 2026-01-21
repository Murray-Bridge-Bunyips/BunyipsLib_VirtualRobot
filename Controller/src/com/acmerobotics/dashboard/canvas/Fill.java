package com.acmerobotics.dashboard.canvas;

public class Fill extends CanvasOp {
    public String color;

    public Fill(String color) {
        super(Type.FILL);

        this.color = color;
    }
}
