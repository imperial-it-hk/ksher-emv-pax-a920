package com.evp.eos.device;

public class DeviceException extends Exception {

    private int code;

    public DeviceException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public DeviceException(String msg) {
        super(msg);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
