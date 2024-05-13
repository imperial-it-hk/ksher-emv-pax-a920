package com.evp.eos.device.beeper;

import io.reactivex.Completable;

public interface IBeeper {

    Completable beep(int frequenceLevel, int duration);

}
