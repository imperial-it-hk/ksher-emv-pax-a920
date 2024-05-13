package com.evp.eos.device.printer;

import android.graphics.Bitmap;

import io.reactivex.Completable;

public class PrinterCommon implements IPrinter {

    private static PrinterCommon instance;

    public synchronized static PrinterCommon getInstance() {
        if (instance == null) {
            instance = new PrinterCommon();
        }
        return instance;
    }

    @Override
    public void setGray(int level) {
    }

    @Override
    public Completable printBitmap(Bitmap bitmap) {
        return Completable.complete();
    }
}
