package com.evp.eos.device.decoder;

public interface IDecoder {

    void init(int width, int height);

    String decode(byte[] data);

    void release();

}
