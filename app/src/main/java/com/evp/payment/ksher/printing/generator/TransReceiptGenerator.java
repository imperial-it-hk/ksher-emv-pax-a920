package com.evp.payment.ksher.printing.generator;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;

import com.evp.payment.ksher.BuildConfig;
import com.evp.payment.ksher.database.table.TransDataModel;
import com.evp.payment.ksher.parameter.CurrencyParam;
import com.evp.payment.ksher.parameter.MerchantParam;
import com.evp.payment.ksher.parameter.PrintParam;
import com.evp.payment.ksher.parameter.ResourceParam;
import com.evp.payment.ksher.utils.DateUtils;
import com.evp.payment.ksher.utils.StringUtils;
import com.evp.payment.ksher.utils.constant.TransStatus;
import com.evp.payment.ksher.utils.transactions.ETransType;
import com.google.common.base.Strings;

import java.math.BigDecimal;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import timber.log.Timber;

import static com.evp.payment.ksher.extension.DisplayExtKt.toPaymentChannelDisplay;
import static com.pax.gl.page.IPage.EAlign.CENTER;
import static com.pax.gl.page.IPage.EAlign.RIGHT;
import static com.pax.gl.page.IPage.ILine.IUnit.TEXT_STYLE_BOLD;

/**
 * Transaction receipt generator
 */
public class TransReceiptGenerator extends BaseReceiptGenerator {

    protected TransDataModel transData;

    /**
     * Slip number
     */
    private int slipNum;

    /**
     * Whether the receipt is reprint
     */
    private boolean isReprint;

    protected TransReceiptGenerator(TransDataModel transData) {
        super();
        this.transData = transData;
    }

    public TransReceiptGenerator(TransDataModel transData, int slipNum, boolean isReprint) {
        super();

        this.transData = transData;
        this.slipNum = slipNum;
        this.isReprint = isReprint;
    }

    @Override
    public Bitmap generate() {
        // LOGO
        addPrintLogo();

        feedLine();

        addDividerLine();

        // Merchant address
        page.addLine().addUnit(MerchantParam.INSTANCE.getAddress().get(), PrintFontSize.FONT_SMALL, CENTER, TEXT_STYLE_BOLD);

        addDividerLine();

        if (isReprint)
            addDuplicateLogo();

        addDemo();

        // Terminal number &  Merchant number
        page.addLine().addUnit("TID: " + transData.getTerminalId(), PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD);
//                .addUnit("MID: " + transData.getMerchantId().trim(), PrintFontSize.FONT_SMALL, RIGHT, TEXT_STYLE_BOLD);

        // Terminal number &  Merchant number
        page.addLine().addUnit("MID: " + transData.getMerchantId().trim(), PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD);

        // Invoice number
        String tranceNo = "";
        if (transData.getTransType().equals(ETransType.VOID.toString()) || transData.getTransType().equals(ETransType.REFUND.toString())) {
            tranceNo = StringUtils.INSTANCE.formatTraceNo(transData.getOrigTraceNo().toString());
        } else {
            tranceNo = StringUtils.INSTANCE.formatTraceNo(transData.getTraceNo().toString());
        }
        page.addLine().addUnit("TRACE: " + tranceNo, PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD)
                .addUnit("BATCH: " + StringUtils.INSTANCE.formatTraceNo(transData.getBatchNo()), PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);

        // System trace audit number
//        page.addLine().addUnit("STAN#", PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD)
//                .addUnit(StringUtils.INSTANCE.formatTraceNo(transData.getTraceNo()), PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);

        // Datetime
        String date = DateUtils.INSTANCE.getFormattedDate(transData.getYear() + transData.getDate(), "yyyyMMdd", "MMM dd, yy");
        String time = DateUtils.INSTANCE.getFormattedDate(transData.getTime(), "HHmmss", "HH:mm:ss");
        page.addLine().addUnit(date, PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD)
                .addUnit(time, PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);
        // Auth code
//        String authCode = !Strings.isNullOrEmpty(transData.getAuthCode()) ? transData.getAuthCode() : transData.getOrigAuthCode();
//        if (!Strings.isNullOrEmpty(authCode)) {
//            page.addLine().addUnit("APPROVE CODE", PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD)
//                    .addUnit(authCode, PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);
//        }
        // Reference number
        String refNo = !Strings.isNullOrEmpty(transData.getReferNo()) ? transData.getReferNo() : transData.getOrigReferNo();
        if (!Strings.isNullOrEmpty(refNo)) {
            page.addLine().addUnit("Ref No: " + refNo, PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD);
//                    .addUnit(refNo, PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);
        }


        addDividerLine();

        // Transaction type
        page.addLine().addUnit(convertTransName(transData.getTransType().toString(), TransStatus.NORMAL).toUpperCase(), PrintFontSize.FONT_SUPER_BIG, CENTER, TEXT_STYLE_BOLD);

        // Payment type
        if (!Strings.isNullOrEmpty(transData.getPaymentChannel())) {
            page.addLine().addUnit("Payment Type:", PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD)
                    .addUnit(toPaymentChannelDisplay(transData.getPaymentChannel()), PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);
        }
        // Add transaction ID
        if (!Strings.isNullOrEmpty(transData.getTransactionId())) {
            String transId = !Strings.isNullOrEmpty(transData.getTransactionId()) ? transData.getTransactionId() : !Strings.isNullOrEmpty(transData.getMchOrderNo()) ? transData.getMchOrderNo() : "";
            page.addLine().addUnit("Transaction ID:", PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD)
                    .addUnit(transData.getTransactionId(), PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);
        }
        // Add Invoice No.
        page.addLine().addUnit("Invoice No:", PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD)
                .addUnit("" + transData.getInvoiceNo(), PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);

        feedLine();

        addMarkups();

        feedLine();

        // 票据尾部
        // Receipt tail
        addTail();

        return page.toBitmap(PrintParam.PRINT_WIDTH);
    }

    protected void addMarkups() {
//        parsePrintingMarkups(transData.getSlipPrintingMarkups());
        if (printingMarkupsList.size() > 0) {
            boolean preBitmap = false;
            for (PrintingMarkups markups : printingMarkupsList) {
                if (!transData.isPrintQrEnabled()
                        && !Strings.isNullOrEmpty(markups.getText())
                        && markups.getText().contains("------------------------")) {
                    // 不需要打印二维码/条形码(处理方式：虚线下方的都忽略)
                    // Not need to print QR code/Bar code(Process mode: Ignore everything below the dotted line).
                    page.addLine().addUnit(markups.getText(), PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD);
                    break;
                }

                if (!Strings.isNullOrEmpty(markups.getText())) {
                    if (preBitmap) {
                        page.addLine().addUnit(markups.getText(), PrintFontSize.FONT_NORMAL, CENTER, TEXT_STYLE_BOLD);
                    } else {
                        page.addLine().addUnit(markups.getText(), PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD);
                    }
                    preBitmap = false;
                } else if (markups.getBitmap() != null) {
                    page.addLine().addUnit(markups.getBitmap(), CENTER);
                    preBitmap = true;
                }
            }
        } else {
            String currencyName = CurrencyParam.INSTANCE.getCurrency().getName();
            String amount = "";
            String amountConvert = "";
            String currencyConvert = transData.getCurrencyConvert();
            if (transData.getTransType().equals(ETransType.VOID.toString()) || transData.getTransType().equals(ETransType.REFUND.toString())) {
                amount = "-" + StringUtils.INSTANCE.toDisplayAmount(transData.getAmount());
                amountConvert = "-" + StringUtils.INSTANCE.toDisplayAmount(transData.getAmountConvert());
            } else {
                amount = StringUtils.INSTANCE.toDisplayAmount(transData.getAmount());
                amountConvert = StringUtils.INSTANCE.toDisplayAmount(transData.getAmountConvert());
            }

            if (new BigDecimal(transData.getExchangeRate()).compareTo(BigDecimal.ONE) != 0) {
                page.addLine().addUnit(currencyName + " AMT", PrintFontSize.FONT_SUPER_BIG, TEXT_STYLE_BOLD)
                        .addUnit(amount, PrintFontSize.FONT_SUPER_BIG, RIGHT, TEXT_STYLE_BOLD);
                page.addLine().addUnit(currencyConvert + " AMT", PrintFontSize.FONT_SUPER_BIG, TEXT_STYLE_BOLD)
                        .addUnit(amountConvert, PrintFontSize.FONT_SUPER_BIG, RIGHT, TEXT_STYLE_BOLD);

                page.addLine().addUnit("Ex. RATE (" + currencyConvert + "/" + currencyName + ")", PrintFontSize.FONT_SMALL, TEXT_STYLE_BOLD)
                        .addUnit(transData.getExchangeRate(), PrintFontSize.FONT_SMALL, RIGHT, TEXT_STYLE_BOLD);
            } else {
                // If no markups data, then print backup data
                // Amount

                page.addLine().addUnit("AMT", PrintFontSize.FONT_SUPER_BIG, TEXT_STYLE_BOLD)
                        .addUnit(currencyName + " " + amount, PrintFontSize.FONT_SUPER_BIG, RIGHT, TEXT_STYLE_BOLD);
            }
            feedLine();
            addDividerLine();
        }
    }

    protected void addTail() {
//        page.addLine().addUnit("I ACKNOWLEDGE SATISFACTORY", PrintFontSize.FONT_SMALL, CENTER, TEXT_STYLE_BOLD);
//        page.addLine().addUnit("RECEIPT OF RELATIVE GOODS/SERVICE", PrintFontSize.FONT_SMALL, CENTER, TEXT_STYLE_BOLD);

        if(ResourceParam.getDisclaimerTxt().get().length()>0){
            feedLine();
            page.addLine().addUnit(ResourceParam.getDisclaimerTxt().get(), PrintFontSize.FONT_SMALL, CENTER, TEXT_STYLE_BOLD);
        }

        if(ResourceParam.getLabelFooter().get().length()>0){
            feedLine();
            page.addLine().addUnit(ResourceParam.getLabelFooter().get(), PrintFontSize.FONT_SMALL, CENTER, TEXT_STYLE_BOLD);
        }

        if (slipNum != 1 && !transData.getTransType().equals(ETransType.VOID.toString()) && !transData.getTransType().equals(ETransType.REFUND.toString())) {
            QRGEncoder qrgEncoder = new QRGEncoder(
                    transData.getTraceNo().toString(), null, QRGContents.Type.TEXT, 240
            );
            qrgEncoder.setColorBlack(Color.BLACK);
            qrgEncoder.setColorWhite(Color.WHITE);
            try {
                Bitmap logoBmp = qrgEncoder.getBitmap();
                if (logoBmp != null) {
                    feedLine();
                    page.addLine().addUnit("Scan QR Code For VOID", PrintFontSize.FONT_SMALL, CENTER, TEXT_STYLE_BOLD);
                    feedLineSmall();
                    page.addLine().addUnit(cropBorderFromBitmap(logoBmp), CENTER);
                    feedLineSmall();
                    page.addLine().addUnit("" + transData.getTraceNo(), PrintFontSize.FONT_NORMAL, CENTER, TEXT_STYLE_BOLD);
                    feedLine();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Timber.e(e);
            }
        }

        if (transData.getTransType().equals(ETransType.VOID.toString())) {
            feedLine();
            if (PrintParam.INSTANCE.getVoidSlipPrintNoRefund().get()) {
                // 撤销交易需要打印 NO REFUND
                // Void transaction need to print "NO REFUND"
                page.addLine().addUnit("*** NO REFUND ***", PrintFontSize.FONT_NORMAL, CENTER, TEXT_STYLE_BOLD);
            }
        }

//        if (isReprint) {
//            feedLine();
//            addDuplicate();
//        }
        
        feedLine();
        addCopyType();
        page.addLine().addUnit("KSHER V" + BuildConfig.VERSION_NAME, PrintFontSize.FONT_SMALL, CENTER, TEXT_STYLE_BOLD);
        if (slipNum == 1) {
            feedLine();
            addPromotionLogo();
        }
        feedEndLine();
    }

    protected void addCopyType() {
        String copy;
        if (slipNum == 1) {
            copy = "Customer Copy";
        } else {
            copy = "Merchant Copy";
        }
        page.addLine().addUnit("--- " + copy + " ---", PrintFontSize.FONT_NORMAL, CENTER, TEXT_STYLE_BOLD);
    }


    public Bitmap cropBorderFromBitmap(Bitmap bmp) {
        //Convenience variables
        int width = bmp.getWidth();
        int height = bmp.getHeight();

        int[] pixels = new int[height * width];

        //Load the pixel data into the pixels array
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);

        int length = pixels.length;

        int borderColor = pixels[0];

        //Locate the start of the border
        int borderStart = 0;
        for (int i = 0; i < length; i++) {

            // 1. Compare the color of two pixels whether they differ
            // 2. Check whether the difference is significant
            if (pixels[i] != borderColor && !sameColor(borderColor, pixels[i])) {
                borderStart = i;
                break;
            }
        }

        //Locate the end of the border
        int borderEnd = 0;
        for (int i = length - 1; i >= 0; i--) {
            if (pixels[i] != borderColor && !sameColor(borderColor, pixels[i])) {
                borderEnd = length - i;
                break;
            }
        }

        //Calculate the margins
        int leftMargin = borderStart % width;
        int rightMargin = borderEnd % width;
        int topMargin = borderStart / width;
        int bottomMargin = borderEnd / width;

        //Create the new, cropped version of the Bitmap
        bmp = Bitmap.createBitmap(bmp, leftMargin, topMargin, width - leftMargin - rightMargin, height - topMargin - bottomMargin);
        return bmp;
    }

    private boolean sameColor(int color1, int color2) {
        // Split colors into RGB values
        long r1 = (color1) & 0xFF;
        long g1 = (color1 >> 8) & 0xFF;
        long b1 = (color1 >> 16) & 0xFF;

        long r2 = (color2) & 0xFF;
        long g2 = (color2 >> 8) & 0xFF;
        long b2 = (color2 >> 16) & 0xFF;

        long dist = (r2 - r1) * (r2 - r1) + (g2 - g1) * (g2 - g1) + (b2 - b1) * (b2 - b1);

        // Check vs. threshold
        return dist < 200;
    }
}
