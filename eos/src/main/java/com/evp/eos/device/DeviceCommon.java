package com.evp.eos.device;

import android.content.Context;

import com.evp.eos.device.beeper.BeeperCommon;
import com.evp.eos.device.beeper.IBeeper;
import com.evp.eos.device.decoder.DecoderCommon;
import com.evp.eos.device.decoder.IDecoder;
import com.evp.eos.device.printer.IPrinter;
import com.evp.eos.device.printer.PrinterCommon;
import com.evp.eos.device.utils.IUtils;
import com.evp.eos.device.utils.UtilsCommon;

public class DeviceCommon implements IDevice {

    private static DeviceCommon instance;

    private Context context;

    private DeviceCommon(Context context) {
        this.context = context;
    }

    public static DeviceCommon getInstance(Context context) {
        if (instance == null) {
            instance = new DeviceCommon(context);
        }
        return instance;
    }

    @Override
    public void init() throws DeviceException {

    }

    @Override
    public IPrinter getPrinter() {
        return PrinterCommon.getInstance();
    }

    @Override
    public IDecoder getDecoder() {
        return DecoderCommon.getInstance();
    }

    @Override
    public IBeeper getBeeper() {
        return BeeperCommon.getInstance();
    }

    @Override
    public IUtils getUtils() {
        return UtilsCommon.getInstance();
    }

}
