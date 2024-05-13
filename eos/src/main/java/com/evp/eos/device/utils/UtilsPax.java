package com.evp.eos.device.utils;

import com.pax.dal.IDAL;
import com.pax.dal.entity.ENavigationKey;

public class UtilsPax implements IUtils {

    private static UtilsPax instance;

    private IDAL idal;

    public UtilsPax(IDAL idal) {
        this.idal = idal;
    }

    public static UtilsPax getInstance(IDAL idal) {
        if (instance == null) {
            instance = new UtilsPax(idal);
        }
        return instance;
    }

    @Override
    public void setSystemTime(String timestamp) {
        idal.getSys().setDate(timestamp);
    }

    @Override
    public boolean isStatusBarEnabled() {
        return idal.getSys().isStatusBarEnabled();
    }

    @Override
    public void enableStatusBar(boolean enable) {
        idal.getSys().enableStatusBar(enable);
    }

    @Override
    public boolean isNavigationBarShow() {
        return idal.getSys().isNavigationBarVisible();
    }

    @Override
    public void showNavigationBar(boolean show) {
        idal.getSys().showNavigationBar(show);
    }

    @Override
    public boolean isNavigationBackKeyEnabled() {
        return idal.getSys().isNavigationKeyEnabled(ENavigationKey.BACK);
    }

    @Override
    public void enableNavigationBackKey(boolean enable) {
        idal.getSys().enableNavigationKey(ENavigationKey.BACK, enable);
    }

    @Override
    public boolean isNavigationHomeKeyEnabled() {
        return idal.getSys().isNavigationKeyEnabled(ENavigationKey.HOME);
    }

    @Override
    public void enableNavigationHomeKey(boolean enable) {
        idal.getSys().enableNavigationKey(ENavigationKey.HOME, enable);
    }

    @Override
    public boolean isNavigationRecentKeyEnabled() {
        return idal.getSys().isNavigationKeyEnabled(ENavigationKey.RECENT);
    }

    @Override
    public void enableNavigationRecentKey(boolean enable) {
        idal.getSys().enableNavigationKey(ENavigationKey.RECENT, enable);
    }

    @Override
    public boolean isPowerKeyEnabled() {
        return idal.getSys().isPowerKeyEnabled();
    }

    @Override
    public void enablePowerKey(boolean enable) {
        idal.getSys().enablePowerKey(enable);
    }
}
