package com.evp.eos.device.decoder;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import com.evp.eos.utils.qrcode.QRCodeDecoder;

public class DecoderCommon implements IDecoder {

    private static DecoderCommon instance;

    private int width;
    private int height;
    private MultiFormatReader mMultiFormatReader;

    public static DecoderCommon getInstance() {
        if (instance == null) {
            instance = new DecoderCommon();
        }
        return instance;
    }

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;

        mMultiFormatReader = new MultiFormatReader();
        mMultiFormatReader.setHints(QRCodeDecoder.getAllHintMap());
    }

    @Override
    public String decode(byte[] data) {
        Result rawResult = null;

        try {
            PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false);

            rawResult = mMultiFormatReader.decodeWithState(new BinaryBitmap(new GlobalHistogramBinarizer(source)));
            if (rawResult == null) {
                rawResult = mMultiFormatReader.decodeWithState(new BinaryBitmap(new HybridBinarizer(source)));
            }
        } catch (Exception ignore) {
        } finally {
            mMultiFormatReader.reset();
        }

        if (rawResult == null) {
            return null;
        }

        return rawResult.getText();
    }

    @Override
    public void release() {
    }
}
