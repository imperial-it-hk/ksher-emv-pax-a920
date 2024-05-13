package com.evp.eos.device;

import com.evp.eos.device.beeper.IBeeper;
import com.evp.eos.device.decoder.IDecoder;
import com.evp.eos.device.printer.IPrinter;
import com.evp.eos.device.utils.IUtils;

public interface IDevice {
    /**
     * Manufacturer: PAX
     */
    String MANUFACTURER_PAX = "PAX";
    String MANUFACTURER_lephone = "lephone";

    /**
     * Interface initialize
     */
    void init() throws DeviceException;

    /**
     * Get printer
     */
    IPrinter getPrinter();

    /**
     * Bar code/QR code decoder
     */
    IDecoder getDecoder();

    /**
     * Beeper
     */
    IBeeper getBeeper();

    /**
     * Utils
     */
    IUtils getUtils();

}
