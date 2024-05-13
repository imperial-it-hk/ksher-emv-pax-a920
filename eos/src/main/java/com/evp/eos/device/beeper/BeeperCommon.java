package com.evp.eos.device.beeper;

import io.reactivex.Completable;

public class BeeperCommon implements IBeeper {

    private static BeeperCommon instance;

    public static BeeperCommon getInstance() {
        if (instance == null) {
            instance = new BeeperCommon();
        }
        return instance;
    }

    @Override
    public Completable beep(int frequenceLevel, int duration) {
        return Completable.complete();
    }
}
