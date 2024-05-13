package com.evp.payment.ksher.customview;

import android.content.Context;
import android.util.AttributeSet;

import com.evp.eos.EosService;
import com.icg.scancode.QRCodeView;

public class ScanCodeView extends QRCodeView {

    public ScanCodeView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public ScanCodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void setupReader() {
        EosService.getDevice().getDecoder().init(640, 480);
    }

    @Override
    protected String processData(byte[] data, int width, int height) {
        return EosService.getDevice().getDecoder().decode(data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EosService.getDevice().getDecoder().release();
    }
}
