package com.evp.payment.ksher.printing.generator;

import android.graphics.Bitmap;

import com.evp.payment.ksher.database.SettlementItemModel;
import com.evp.payment.ksher.database.SettlementModel;
import com.evp.payment.ksher.database.table.SettlementDataModel;
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
public class SummaryReceiptGenerator extends BaseReceiptGenerator {

    protected SettlementDataModel information;

    /**
     * Slip number
     */
    private int slipNum;

    /**
     * Whether the receipt is reprint
     */
    private boolean isReprint;

    protected SummaryReceiptGenerator(SettlementDataModel information) {
        super();
        this.information = information;
    }

    public SummaryReceiptGenerator(SettlementDataModel information, int slipNum, boolean isReprint) {
        super();

        this.information = information;
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

        addDemo();

        // Terminal number &  Merchant number
        page.addLine().addUnit("TID: " + information.getTerminalId(), PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD);

        // Terminal number &  Merchant number
        page.addLine().addUnit("MID: " + information.getMerchantId().trim(), PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD);

        // Datetime
        String date = DateUtils.INSTANCE.getFormattedDate(information.getYear() + information.getDate(), "yyyyMMdd", "MMM dd, yy");
        String time = DateUtils.INSTANCE.getFormattedDate(information.getTime(), "HHmmss", "HH:mm:ss");
        page.addLine().addUnit(date, PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD)
                .addUnit(time, PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);

        // Batch number
        page.addLine().addUnit("BATCH: " + StringUtils.INSTANCE.formatTraceNo(information.getBatchNo()), PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD);

        // Host name
        page.addLine().addUnit("HOST NAME: " + information.getHostName(), PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD);

        addDividerLine();
        // Transaction type
        page.addLine().addUnit("SUMMARY REPORT", PrintFontSize.FONT_SUPER_BIG, CENTER, TEXT_STYLE_BOLD);

        addDividerLine();

        SettlementModel channelData = new Gson().fromJson(information.getChannelDatas(), SettlementModel.class);
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

        SettlementItemModel grandTotalData = new Gson().fromJson(information.getGrandTotalData(), SettlementItemModel.class);
        page.addLine().addUnit(DisplayExtKt.toPaymentChannelDisplay(grandTotalData.getPaymentChannel()), PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD);
        page.addLine().addUnit("  SALES", PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD)
                .addUnit("" + grandTotalData.getSaleCount(), PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);
        page.addLine().addUnit("THB  " + StringUtils.INSTANCE.toDisplayAmount(grandTotalData.getSaleTotalAmount()), PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);
        page.addLine().addUnit("  REFUNDS", PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD)
                .addUnit("" + grandTotalData.getRefundCount(), PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);
        page.addLine().addUnit("THB  " + StringUtils.INSTANCE.toDisplayAmount(grandTotalData.getRefundTotalAmount()), PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);


        feedEndLine();

        return page.toBitmap(PrintParam.PRINT_WIDTH);
    }
}
