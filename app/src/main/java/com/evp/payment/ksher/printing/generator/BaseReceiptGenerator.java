package com.evp.payment.ksher.printing.generator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;

import com.evp.payment.ksher.function.BaseApplication;
import com.evp.payment.ksher.parameter.AbstractParam;
import com.evp.payment.ksher.parameter.PrintParam;
import com.evp.payment.ksher.parameter.ResourceParam;
import com.evp.payment.ksher.parameter.SystemParam;
import com.evp.payment.ksher.utils.BytesUtil;
import com.evp.payment.ksher.utils.StringUtils;
import com.evp.payment.ksher.utils.constant.TransStatus;
import com.evp.payment.ksher.utils.transactions.ETransType;
import com.evp.eos.utils.LogUtil;
import com.evp.eos.utils.qrcode.QRCodeEncoder;
import com.pax.gl.page.IPage;
import com.pax.gl.page.PaxGLPage;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.pax.gl.page.IPage.EAlign.CENTER;
import static com.pax.gl.page.IPage.ILine.IUnit.TEXT_STYLE_BOLD;

public abstract class BaseReceiptGenerator {

    private static final String TAG = "BaseReceiptGenerator";

    protected IPage page;

    protected List<PrintingMarkups> printingMarkupsList = new ArrayList<>();

    private boolean hasTag01;
    private boolean hasTag02;
    private boolean hasTag03;

    public BaseReceiptGenerator() {
        this.page = PaxGLPage.getInstance(BaseApplication.Companion.getAppContext()).createPage();
        this.page.setTypefaceObj(Typeface.createFromAsset(BaseApplication.Companion.getAppContext().getAssets(), PrintParam.FONT_PATH));
    }

    public abstract Bitmap generate();

    protected void feedLine() {
        page.addLine().addUnit(" ", PrintFontSize.FONT_SMALL);
    }

    protected void feedLineSmall() {
        page.addLine().addUnit(" ", PrintFontSize.PADDING_SMALL);
    }

    protected int calMaxLength(String... strs) {
        if (strs == null) {
            return 0;
        }
        int maxLength = 0;
        for (String s : strs) {
            if (maxLength < s.length()) {
                maxLength = s.length();
            }
        }
        return maxLength;
    }

    protected void addDividerLine() {
        page.addLine().addUnit("--------------------------------------------------------------------", PrintFontSize.FONT_NORMAL, CENTER, TEXT_STYLE_BOLD);
    }

    protected void feedEndLine() {
        page.addLine().addUnit("\n\n\n\n\n\n\n", PrintFontSize.FONT_SMALL);
    }

    protected void addDemo() {
//        // Read from folder
        if("demo".equalsIgnoreCase(SystemParam.getCommunicationMode().get())) {
            Bitmap logoBmp = null;

            // Read from assets
            try {
                InputStream in = BaseApplication.Companion.getAppContext().getAssets().open("images/ic_demo.png");
                logoBmp = BitmapFactory.decodeStream(in);
                in.close();
            } catch (IOException e) {
                LogUtil.e(TAG, "", e);
            }
            if (logoBmp != null) {
                page.addLine().addUnit(logoBmp, CENTER);
                feedLine();
            }
        }
    }

    protected void addDuplicateLogo() {
        page.addLine().addUnit("***** DUPLICATE *****",  PrintFontSize.FONT_SUPER_BIG, CENTER, TEXT_STYLE_BOLD);
    }

    protected void addPrintLogo() {
        // Read from folder
//        Bitmap logoBmp = BitmapFactory.decodeFile(DownloadParamManager.getInstance().getSaveFilePath() + ResourceParam.INSTANCE.getPrintLogoFileName().get());
//        if (logoBmp != null) {
//            page.addLine().addUnit(logoBmp, CENTER);
//            return;
//        }
//        Bitmap logoBmp = null;
//        // Read from assets
//        try {
//            InputStream in = BaseApplication.Companion.getAppContext().getAssets().open("images/ic_merchant_logo.png");
//            logoBmp = BitmapFactory.decodeStream(in);
//            in.close();
//        } catch (IOException e) {
//            LogUtil.e(TAG, "", e);
//        }

        if (StringUtils.INSTANCE.getImage(ResourceParam.getPrintLogoFileName().get()) != null) {
            page.addLine().addUnit(StringUtils.INSTANCE.getImage(ResourceParam.getPrintLogoFileName().get()), CENTER);
        }
    }

    protected void addPromotionLogo() {
        if (StringUtils.INSTANCE.getImage(ResourceParam.getPromotionFileName().get()) != null) {
            page.addLine().addUnit(StringUtils.INSTANCE.getImage(ResourceParam.getPromotionFileName().get()), CENTER);
        }
    }

    protected void addDuplicate() {
        page.addLine().addUnit("***** DUPLICATE *****", PrintFontSize.FONT_NORMAL, CENTER, TEXT_STYLE_BOLD);
    }

    protected String convertTransName(String transType, String status) {
        String transName;
        if (transType.equals(ETransType.SALE.toString())) {
            transName = "SALE";
        } else if (transType.equals(ETransType.VOID.toString())) {
            transName = "VOID";
        } else {
            transName = transType;
        }
        if (TransStatus.VOID.equals(status)) {
            transName += " (VOID)";
        }
        return transName;
    }

    protected void parsePrintingMarkups(String printingMarkups) {
//        try {
//            String remainMarkups = printingMarkups;
//            while (!Strings.isNullOrEmpty(remainMarkups) && remainMarkups.length() >= 4) {
//                remainMarkups = parse(remainMarkups);
//            }
//            convertSlipType();
//        } catch (Exception ignore) {
//        }
    }

    private void convertSlipType() {
        if (hasTag01) {
            // Only print 01
            removeSlipType(PrintingMarkups.SlipType.CUS, PrintingMarkups.SlipType.ALL);
        } else if (hasTag03) {
            // Only print 03
            removeSlipType(PrintingMarkups.SlipType.MER, PrintingMarkups.SlipType.CUS);
        } else {
            // Only print 02
            removeSlipType(PrintingMarkups.SlipType.MER, PrintingMarkups.SlipType.ALL);
        }
    }

    private void removeSlipType(int removeType1, int removeType2) {
        Iterator<PrintingMarkups> iterator = printingMarkupsList.iterator();
        while (iterator.hasNext()) {
            PrintingMarkups markups = iterator.next();
            if (markups.getBitmap() == null && // 二维码/条形码不删除 QR code/Bar code not deleted
                    (markups.getSlipType() == removeType1 || markups.getSlipType() == removeType2)) {
                iterator.remove();
            }
        }
    }

    private String parse(String printingMarkups) throws Exception {
        String tag = printingMarkups.substring(0, 2);
        switch (tag) {
            case "01":
                hasTag01 = true;
                return parseText(PrintingMarkups.SlipType.MER, printingMarkups);
            case "02":
                hasTag02 = true;
                return parseText(PrintingMarkups.SlipType.CUS, printingMarkups);
            case "03":
                hasTag03 = true;
                return parseText(PrintingMarkups.SlipType.ALL, printingMarkups);
            case "07":
                return parseBarCode(PrintingMarkups.SlipType.MER, printingMarkups);
            case "08":
                return parseBarCode(PrintingMarkups.SlipType.CUS, printingMarkups);
            case "09":
                return parseBarCode(PrintingMarkups.SlipType.ALL, printingMarkups);
            case "11":
                return parseQRCode(PrintingMarkups.SlipType.MER, printingMarkups);
            case "12":
                return parseQRCode(PrintingMarkups.SlipType.CUS, printingMarkups);
            case "13":
                return parseQRCode(PrintingMarkups.SlipType.ALL, printingMarkups);
            default:
                throw new Exception();
        }
    }

    private String parseText(int slipType, String str) {
        int length = Integer.parseInt(str.substring(2, 4));
        String value = str.substring(4, 4 + length * 2);
        String[] values = value.split("0A");
        for (String s : values) {
            printingMarkupsList.add(new PrintingMarkups(slipType, extendText(s)));
        }
        return str.substring(4 + length * 2);
    }

    private String extendText(String str) {
        StringBuilder builder = new StringBuilder();
        String remainStr = str;
        while (remainStr.length() > 0) {
            switch (remainStr.substring(0, 2)) {
                case "14":
                    builder.append(parseRepeat14(remainStr.substring(0, 6)));
                    remainStr = remainStr.substring(6);
                    break;
                case "15":
                    builder.append(parseRepeat(remainStr.substring(0, 4), " "));
                    remainStr = remainStr.substring(4);
                    break;
                case "16":
                    builder.append(parseRepeat(remainStr.substring(0, 4), "="));
                    remainStr = remainStr.substring(4);
                    break;
                case "17":
                    builder.append(parseRepeat(remainStr.substring(0, 4), "-"));
                    remainStr = remainStr.substring(4);
                    break;
                default:
                    builder.append(BytesUtil.hexString2String(remainStr.substring(0, 2), BytesUtil.CHARSET_TIS_620));
                    remainStr = remainStr.substring(2);
            }
        }
        return builder.toString();
    }

    private String parseBarCode(int slipType, String str) {
        int length = Integer.parseInt(str.substring(2, 4));
        String value = str.substring(4, 4 + length * 2);
        Bitmap barCode = QRCodeEncoder.syncEncodeBarcode(BytesUtil.hexString2String(value, BytesUtil.CHARSET_TIS_620), 384, 100, 0);
        printingMarkupsList.add(new PrintingMarkups(slipType, barCode));

        return str.substring(4 + length * 2);
    }

    private String parseQRCode(int slipType, String str) {
        int length = Integer.parseInt(str.substring(2, 6));
        String value = str.substring(6, 6 + length * 2);
        Bitmap qrCode = QRCodeEncoder.syncEncodeQRCode(BytesUtil.hexString2String(value, BytesUtil.CHARSET_TIS_620), 200);
        printingMarkupsList.add(new PrintingMarkups(slipType, qrCode));

        return str.substring(6 + length * 2);
    }

    private String parseRepeat14(String str) {
        String repeatStr = str.substring(2, 4);
        int count = Integer.parseInt(str.substring(4, 6));
        return repeatStr(BytesUtil.hexString2String(repeatStr, BytesUtil.CHARSET_TIS_620), count);
    }

    private String parseRepeat(String str, String repeatStr) {
        int count = Integer.parseInt(str.substring(2, 4));
        return repeatStr(repeatStr, count);
    }

    private String repeatStr(String str, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(str);
        }
        return builder.toString();
    }

}
