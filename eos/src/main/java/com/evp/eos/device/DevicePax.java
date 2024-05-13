package com.evp.eos.device;

import android.content.Context;

import com.evp.eos.R;
import com.evp.eos.device.beeper.BeeperPax;
import com.evp.eos.device.beeper.IBeeper;
import com.evp.eos.device.decoder.DecoderCommon;
import com.evp.eos.device.decoder.IDecoder;
import com.evp.eos.device.printer.IPrinter;
import com.evp.eos.device.printer.PrinterPax;
import com.evp.eos.device.utils.IUtils;
import com.evp.eos.device.utils.UtilsPax;
import com.evp.eos.utils.LogUtil;
import com.pax.dal.IDAL;
import com.pax.neptunelite.api.NeptuneLiteUser;

public class DevicePax implements IDevice {

    private static final String TAG = "DevicePax";

    private static DevicePax instance;

    private Context context;

    private IDAL iDal;

    private DevicePax(Context context) {
        this.context = context;
    }

    public static DevicePax getInstance(Context context) {
        if (instance == null) {
            instance = new DevicePax(context);
        }
        return instance;
    }

    @Override
    public void init() throws DeviceException {
        try {
            iDal = NeptuneLiteUser.getInstance().getDal(context);
        } catch (Exception e) {
            LogUtil.e(TAG, e);
            throw new DeviceException(context.getString(R.string.device_initialization_failed));
        }
    }

    @Override
    public IPrinter getPrinter() {
        return PrinterPax.getInstance(iDal);
    }

    @Override
    public IDecoder getDecoder() {
        return DecoderCommon.getInstance();
    }

    @Override
    public IBeeper getBeeper() {
        return BeeperPax.getInstance(iDal);
    }

    @Override
    public IUtils getUtils() {
        return UtilsPax.getInstance(iDal);
    }

}
