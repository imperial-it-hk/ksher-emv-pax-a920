package com.evp.eos.device.printer;

import com.evp.eos.device.DeviceException;

public class PrinterException extends DeviceException {
    /**
     * Printer busy
     */
    public static final int BUSY = 1;
    /**
     * Out of paper
     */
    public static final int OUT_OF_PAPER = 2;
    /**
     * Overheating
     */
    public static final int OVERHEAT = 3;
    /**
     * Low battery
     */
    public static final int LOW_VOLTAGE = 4;
    /**
     * Other exception
     */
    public static final int OTHER = 5;

    public PrinterException(int code, String message) {
        super(code, message);
    }
}
