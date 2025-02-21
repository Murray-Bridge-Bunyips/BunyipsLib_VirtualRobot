package com.qualcomm.robotcore.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TypeConversion {
    public static int byteArrayToInt(byte[] a, ByteOrder b) {
        return ByteBuffer.wrap(a).order(b).getInt();
    }
    
    public static byte[] intToByteArray(int a, ByteOrder b) {
        return ByteBuffer.allocate(4).order(b).putInt(a).array();
    }
}
