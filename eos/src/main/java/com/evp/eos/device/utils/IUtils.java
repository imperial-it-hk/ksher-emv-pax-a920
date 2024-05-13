package com.evp.eos.device.utils;

public interface IUtils {

    /**
     * @param timestamp yyyyMMddHHmmss
     */
    void setSystemTime(String timestamp);

    boolean isStatusBarEnabled();

    void enableStatusBar(boolean enable);

    boolean isNavigationBarShow();

    void showNavigationBar(boolean show);

    boolean isNavigationBackKeyEnabled();

    void enableNavigationBackKey(boolean enable);

    boolean isNavigationHomeKeyEnabled();

    void enableNavigationHomeKey(boolean enable);

    boolean isNavigationRecentKeyEnabled();

    void enableNavigationRecentKey(boolean enable);

    boolean isPowerKeyEnabled();

    void enablePowerKey(boolean enable);

}
