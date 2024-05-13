package com.evp.eos;

import android.content.Context;
import android.os.Build;

import com.evp.eos.device.DeviceCommon;
import com.evp.eos.device.DeviceException;
import com.evp.eos.device.DevicePax;
import com.evp.eos.device.IDevice;

/**
 * Hardware module
 */
public class EosService {

    private static Context context;

    private static IDevice device;

    /**
     * Device interface module initialize
     */
    public static void initDevice(Context ctx) throws DeviceException {
        if (context != null && device != null) return;

        context = ctx;
        if (IDevice.MANUFACTURER_PAX.equalsIgnoreCase(Build.MANUFACTURER) || IDevice.MANUFACTURER_lephone.equalsIgnoreCase(Build.MANUFACTURER)) {
            device = DevicePax.getInstance(context);
        } else {
            device = DeviceCommon.getInstance(context);
        }
        device.init();
    }

    public static Context getContext() {
        return context;
    }

    public static IDevice getDevice() {
        return device;
    }

}
