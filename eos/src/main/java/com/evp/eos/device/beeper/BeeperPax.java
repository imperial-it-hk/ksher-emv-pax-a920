package com.evp.eos.device.beeper;

import com.pax.dal.IDAL;
import com.pax.dal.entity.EBeepMode;

import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;

public class BeeperPax implements IBeeper {

    private static BeeperPax instance;

    private IDAL idal;

    private BeeperPax(IDAL idal) {
        this.idal = idal;
    }

    public static BeeperPax getInstance(IDAL idal) {
        if (instance == null) {
            instance = new BeeperPax(idal);
        }
        return instance;
    }

    @Override
    public Completable beep(int frequenceLevel, int duration) {
        EBeepMode level;
        switch (frequenceLevel) {
            case BeepFrequenceLevel.LEVEL_1:
                level = EBeepMode.FREQUENCE_LEVEL_1;
                break;
            case BeepFrequenceLevel.LEVEL_2:
                level = EBeepMode.FREQUENCE_LEVEL_2;
                break;
            case BeepFrequenceLevel.LEVEL_3:
                level = EBeepMode.FREQUENCE_LEVEL_3;
                break;
            case BeepFrequenceLevel.LEVEL_4:
                level = EBeepMode.FREQUENCE_LEVEL_4;
                break;
            case BeepFrequenceLevel.LEVEL_5:
                level = EBeepMode.FREQUENCE_LEVEL_5;
                break;
            case BeepFrequenceLevel.LEVEL_6:
                level = EBeepMode.FREQUENCE_LEVEL_6;
                break;
            default:
                level = EBeepMode.FREQUENCE_LEVEL_0;
        }
        return Completable.fromAction(() -> idal.getSys().beep(level, duration))
                .subscribeOn(Schedulers.io());
    }
}
