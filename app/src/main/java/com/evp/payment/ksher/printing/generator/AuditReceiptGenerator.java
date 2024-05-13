package com.evp.payment.ksher.printing.generator;

import android.graphics.Bitmap;

import com.evp.payment.ksher.database.HistoryData;
import com.evp.payment.ksher.database.SaleTotalByChannelModel;
import com.evp.payment.ksher.database.table.TransDataModel;
import com.evp.payment.ksher.parameter.CurrencyParam;
import com.evp.payment.ksher.parameter.MerchantParam;
import com.evp.payment.ksher.parameter.PrintParam;
import com.evp.payment.ksher.parameter.SystemParam;
import com.evp.payment.ksher.parameter.TerminalParam;
import com.evp.payment.ksher.utils.DateUtils;
import com.evp.payment.ksher.utils.StringUtils;
import com.evp.payment.ksher.utils.constant.TransStatus;
import com.evp.payment.ksher.utils.transactions.ETransType;
import com.google.common.base.Strings;

import java.util.List;

import static com.evp.payment.ksher.extension.DisplayExtKt.toAmount2DigitDisplay;
import static com.evp.payment.ksher.extension.DisplayExtKt.toPaymentChannelDisplay;
import static com.pax.gl.page.IPage.EAlign.CENTER;
import static com.pax.gl.page.IPage.EAlign.RIGHT;
import static com.pax.gl.page.IPage.ILine.IUnit.TEXT_STYLE_BOLD;

/**
 * Transaction receipt generator
 */
public class AuditReceiptGenerator extends BaseReceiptGenerator {

    protected List<HistoryData> information;

    /**
     * Slip number
     */
    private int slipNum;

    /**
     * Whether the receipt is reprint
     */
    private boolean isReprint;

    protected AuditReceiptGenerator(List<HistoryData> information) {
        super();
        this.information = information;
    }

    public AuditReceiptGenerator(List<HistoryData> information, int slipNum, boolean isReprint) {
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
        page.addLine().addUnit(MerchantParam.getAddress().get(), PrintFontSize.FONT_SMALL, CENTER, TEXT_STYLE_BOLD);

        addDividerLine();

        addDemo();

        // Terminal number &  Merchant number
        page.addLine().addUnit("TID: " + TerminalParam.getNumber().get(), PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD);

        // Terminal number &  Merchant number
        page.addLine().addUnit("MID: " + MerchantParam.getMerchantId().get(), PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD);

        String year = DateUtils.INSTANCE.getCurrentTime("yyyy");
        String date = DateUtils.INSTANCE.getCurrentTime("MMdd");
        String resultDate = DateUtils.INSTANCE.getFormattedDate(
                year + date, "yyyyMMdd", "MMM dd, yy"
        );

        // Datetime
        String time = DateUtils.INSTANCE.getFormattedDate(DateUtils.INSTANCE.getCurrentTime("HH:mm:ss"), "HHmmss", "HH:mm:ss");
        page.addLine().addUnit(resultDate, PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD)
                .addUnit(time, PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);

        // Batch number
        page.addLine().addUnit("BATCH: " + SystemParam.Companion.getBatchNo().get(), PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD);

        // Host name
        page.addLine().addUnit("HOST NAME: "+ MerchantParam.getName().get(), PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD);

        addDividerLine();
        // Transaction type
        page.addLine().addUnit("Audit Report", PrintFontSize.FONT_SUPER_BIG, CENTER, TEXT_STYLE_BOLD);

        addDividerLine();

        int total = 0;
        int totalAmount = 0;
        boolean isShowLine1 = true;
        for (HistoryData item : information) {
            if (item instanceof TransDataModel) {
                TransDataModel dataModel = (TransDataModel) item;
                String currencyName = CurrencyParam.INSTANCE.getCurrency().getName();

                String amount = "";
                if (dataModel.getTransType().equals(ETransType.VOID.toString()) || dataModel.getTransType().equals(ETransType.REFUND.toString())) {
                    amount = "-" + StringUtils.INSTANCE.toDisplayAmount(dataModel.getAmount());
                } else {
                    amount = StringUtils.INSTANCE.toDisplayAmount(dataModel.getAmount());
                }

                // Transaction type
                page.addLine().addUnit(convertTransName(toPaymentChannelDisplay(dataModel.getPaymentChannel()) + " " + dataModel.getTransType(), TransStatus.NORMAL), PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD)
                        .addUnit(currencyName + " " + amount, PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);


                // Datetime
                String dateDetail = DateUtils.INSTANCE.getFormattedDate(dataModel.getYear() + dataModel.getDate(), "yyyyMMdd", "MMM dd, yy");
                String timeDetail = DateUtils.INSTANCE.getFormattedDate(dataModel.getTime(), "HHmmss", "HH:mm:ss");
                page.addLine().addUnit(dateDetail, PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD)
                        .addUnit(timeDetail, PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);

                // Add transaction ID
                if (!Strings.isNullOrEmpty(dataModel.getTransactionId())) {
                    String transId = !Strings.isNullOrEmpty(dataModel.getTransactionId()) ? dataModel.getTransactionId() : !Strings.isNullOrEmpty(dataModel.getMchOrderNo()) ? dataModel.getMchOrderNo() : "";
                    page.addLine().addUnit("Transaction ID:", PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD)
                            .addUnit(dataModel.getTransactionId(), PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);
                }
                // Add Invoice No.
                page.addLine().addUnit("Invoice No:", PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD)
                        .addUnit("" + dataModel.getInvoiceNo(), PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);

                feedLine();
            } else if (item instanceof SaleTotalByChannelModel) {
                if(isShowLine1) {
                    addDividerLine();
                    isShowLine1 = false;
                }
                SaleTotalByChannelModel dataModel = (SaleTotalByChannelModel) item;
                total += dataModel.getSaleCount();
                totalAmount += dataModel.getSaleTotalAmount();
                page.addLine().addUnit(toPaymentChannelDisplay(dataModel.getPaymentChannel()), PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD);
                page.addLine().addUnit("SALE", PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD)
                        .addUnit(dataModel.getSaleCount() + "", PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);
                page.addLine() .addUnit("THB  " + toAmount2DigitDisplay(dataModel.getSaleTotalAmount()), PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);
            }
        }
        addDividerLine();
        page.addLine().addUnit("TOTAL", PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD)
                .addUnit(total + "", PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);
        page.addLine().addUnit("TOTAL AMOUNT", PrintFontSize.FONT_NORMAL, TEXT_STYLE_BOLD)
                .addUnit("THB  " + toAmount2DigitDisplay(totalAmount) + "", PrintFontSize.FONT_NORMAL, RIGHT, TEXT_STYLE_BOLD);


        feedEndLine();

        return page.toBitmap(PrintParam.PRINT_WIDTH);
    }
}
