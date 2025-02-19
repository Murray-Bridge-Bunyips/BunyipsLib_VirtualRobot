package com.qualcomm.robotcore.hardware;

public class I2cDeviceSynchDevice<T> implements HardwareDevice {
    protected I2cDeviceSynchSimple deviceClient;
    public I2cDeviceSynchDevice(Object a, Object b) {
        
    }

    protected boolean doInitialize() {
        return true;
    }
    
    public void registerArmingStateCallback(boolean a) {
        
    }
}
