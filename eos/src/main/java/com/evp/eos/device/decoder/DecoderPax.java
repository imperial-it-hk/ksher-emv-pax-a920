package com.evp.eos.device.decoder;

import com.evp.eos.EosService;
import com.pax.dal.IDAL;
import com.pax.dal.IScanCodec;
import com.pax.dal.entity.DecodeResult;

public class DecoderPax implements IDecoder {

    private static DecoderPax instance;

    private IScanCodec iScanCodec;

    private DecoderPax(IScanCodec iScanCodec) {
        this.iScanCodec = iScanCodec;
    }

    public static DecoderPax getInstance(IDAL idal) {
        if (instance == null) {
            instance = new DecoderPax(idal.getScanCodec());
        }
        return instance;
    }

    @Override
    public void init(int width, int height) {
        iScanCodec.init(EosService.getContext(), width, height);
    }

    @Override
    public String decode(byte[] data) {
        DecodeResult decodeResult = iScanCodec.decode(data);
        if (decodeResult != null) {
            return decodeResult.getContent();
        }
        return null;
    }

    @Override
    public void release() {
        iScanCodec.release();
    }
}
