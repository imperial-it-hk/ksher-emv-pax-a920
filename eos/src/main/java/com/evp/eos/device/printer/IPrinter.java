package com.evp.eos.device.printer;

import android.graphics.Bitmap;

import io.reactivex.Completable;

/**
 * Printer
 */
public interface IPrinter {

    /**
     * Set print gray
     */
    void setGray(int level);

    /**
     * Print image
     */
    Completable printBitmap(Bitmap bitmap);

}
