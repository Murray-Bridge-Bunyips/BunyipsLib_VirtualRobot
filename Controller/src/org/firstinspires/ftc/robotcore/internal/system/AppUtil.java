package org.firstinspires.ftc.robotcore.internal.system;

import org.firstinspires.ftc.robotcore.internal.ui.UILocation;

public class AppUtil {
    public static Object getDefContext() {
        return null;
    }
    
    public static String ROBOT_DATA_DIR = "";
    
    public static AppUtil getInstance() {
        return new AppUtil();
    }
    
    public void showToast(UILocation location, String toast) {
        System.out.println("TOAST: " + toast);
    }
    
    public void ensureDirectoryExists(Object o) {
        
    }
    
    public Object getActivity() {
        return new Object();
    }
}
