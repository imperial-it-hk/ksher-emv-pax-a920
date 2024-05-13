package com.evp.payment.ksher.printing.generator;

import android.graphics.Bitmap;

import com.evp.payment.ksher.database.table.SettlementDataModel;
import com.evp.payment.ksher.database.SettlementItemModel;
import com.evp.payment.ksher.database.SettlementModel;
import com.evp.payment.ksher.extension.DisplayExtKt;
import com.evp.payment.ksher.parameter.MerchantParam;
import com.evp.payment.ksher.parameter.PrintParam;
import com.evp.payment.ksher.utils.DateUtils;
import com.evp.payment.ksher.utils.StringUtils;
import com.google.gson.Gson;

import static com.pax.gl.page.IPage.EAlign.CENTER;
import static com.pax.gl.page.IPage.EAlign.RIGHT;
import static com.pax.gl.page.IPage.ILine.IUnit.TEXT_STYLE_BOLD;

/**
 * Transaction receipt generator
 */
public class SettlementReceiptGenerator extends BaseReceiptGenerator {

    protected SettlementDataModel settlementDataModel;

    /**
     * Slip number
     */
    private int slipNum;

    /**
     * Whether the receipt is reprint
     */
    private boolean isReprint;

    protected SettlementReceiptGenerator(SettlementDataModel settlementDataModel) {
        super();
        this.settlementDataModel = settlementDataModel;
    }

    public SettlementReceiptGenerator(SettlementDataModel settlementDataModel, int slipNum, boolean isReprint) {
        super();

        this.settlementDataModel = settlementDataModel;
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
        page.addLine().addUnit("TID: " + settlementDataModel.getTerminalId(), PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD);
//                .addUnit("MID: " + transData.getMerchantId().trim(), PrintFontSize.FONT_SMALL, RIGHT, TEXT_STYLE_BOLD);

        // Terminal number &  Merchant number
        page.addLine().addUnit("MID: " + settlementDataModel.getMerchantId().trim(), PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD);

        // Datetime
        String date = DateUtils.INSTANCE.getFormattedDate(settlementDataModel.getYear() + settlementDataModel.getDate(), "yyyyMMdd", "MMM dd, yy");
        String time = DateUtils.INSTANCE.getFormattedDate(settlementDataModel.getTime(), "HHmmss", "HH:mm:ss");
        page.addLine().addUnit(date, PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD)
                .addUnit(time, PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);

        // Batch number
        page.addLine().addUnit("BATCH: " + StringUtils.INSTANCE.formatTraceNo(settlementDataModel.getBatchNo()), PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD);

        // Host name
        page.addLine().addUnit("HOST NAME: " + settlementDataModel.getHostName(), PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD);


        addDividerLine();

        // Transaction type
        page.addLine().addUnit("***SETTLEMENT***\nSUCCESSFUL", PrintFontSize.FONT_SUPER_BIG, CENTER, TEXT_STYLE_BOLD);

        addDividerLine();

        SettlementModel channelData = new Gson().fromJson(settlementDataModel.getChannelDatas(), SettlementModel.class);
        for (int i = 0; i < channelData.getSettlements().size(); i++) {
            SettlementItemModel item = channelData.getSettlements().get(i);
            page.addLine().addUnit(DisplayExtKt.toPaymentChannelDisplay(item.getPaymentChannel()), PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD);
            page.addLine().addUnit("  SALES", PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD)
                    .addUnit("" + item.getSaleCount(), PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);
            page.addLine().addUnit("THB  " + StringUtils.INSTANCE.toDisplayAmount(item.getSaleTotalAmount()), PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);
            page.addLine().addUnit("  REFUNDS", PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD)
                    .addUnit("" + item.getRefundCount(), PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);
            page.addLine().addUnit("THB  " + StringUtils.INSTANCE.toDisplayAmount(item.getRefundTotalAmount()), PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);
        }

        addDividerLine();

        SettlementItemModel grandTotalData = new Gson().fromJson(settlementDataModel.getGrandTotalData(), SettlementItemModel.class);
        page.addLine().addUnit(DisplayExtKt.toPaymentChannelDisplay(grandTotalData.getPaymentChannel()), PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD);
        page.addLine().addUnit("  SALES", PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD)
                .addUnit("" + grandTotalData.getSaleCount(), PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);
        page.addLine().addUnit("THB  " + StringUtils.INSTANCE.toDisplayAmount(grandTotalData.getSaleTotalAmount()), PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);
        page.addLine().addUnit("  REFUNDS", PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD)
                .addUnit("" + grandTotalData.getRefundCount(), PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);
        page.addLine().addUnit("THB  " + StringUtils.INSTANCE.toDisplayAmount(grandTotalData.getRefundTotalAmount()), PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);

//        // Payment type
//        if (!Strings.isNullOrEmpty(transData.getPaymentChannel())) {
//            page.addLine().addUnit("Payment Type:", PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD)
//                    .addUnit(transData.getPaymentChannel(), PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);
//        }
//        // Add transaction ID
//        if (!Strings.isNullOrEmpty(transData.getTransactionId())) {
//            String transId = !Strings.isNullOrEmpty(transData.getTransactionId()) ? transData.getTransactionId() : !Strings.isNullOrEmpty(transData.getMchOrderNo()) ? transData.getMchOrderNo() : "";
//            page.addLine().addUnit("Transaction ID:", PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD)
//                    .addUnit(transData.getTransactionId(), PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);
//        }
//        // Add Invoice No.
//        page.addLine().addUnit("Invoice No:", PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD)
//                .addUnit("" + transData.getInvoiceNo(), PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);


        // 票据尾部
        // Receipt tail

        feedEndLine();

        return page.toBitmap(PrintParam.PRINT_WIDTH);
    }
}
