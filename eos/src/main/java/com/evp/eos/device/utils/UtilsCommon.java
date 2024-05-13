package com.evp.eos.device.utils;

public class UtilsCommon implements IUtils {

    private static UtilsCommon instance;

    public static UtilsCommon getInstance() {
        if (instance == null) {
            instance = new UtilsCommon();
        }
        return instance;
    }

    @Override
    public void setSystemTime(String timestamp) {

    }

    @Override
    public boolean isStatusBarEnabled() {
        return true;
    }

    @Override
    public void enableStatusBar(boolean enable) {

    }

    @Override
    public boolean isNavigationBarShow() {
        return true;
    }

    @Override
    public void showNavigationBar(boolean show) {

    }

    @Override
    public boolean isNavigationBackKeyEnabled() {
        return true;
    }

    @Override
    public void enableNavigationBackKey(boolean enable) {

    }

    @Override
    public boolean isNavigationHomeKeyEnabled() {
        return true;
    }

    @Override
    public void enableNavigationHomeKey(boolean enable) {

    }

    @Override
    public boolean isNavigationRecentKeyEnabled() {
        return true;
    }

    @Override
    public void enableNavigationRecentKey(boolean enable) {

    }

    @Override
    public boolean isPowerKeyEnabled() {
        return true;
    }

    @Override
    public void enablePowerKey(boolean enable) {
        
    }
}
