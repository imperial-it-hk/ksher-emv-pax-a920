package com.evp.payment.ksher.utils.table;

import com.evp.payment.ksher.utils.BytesUtil;

public class SlipPrintingTable extends BaseCustomTable {

    public static final String tableId = "SL";

    private String printingData;

    public static String generate() {
        StringBuilder tableData = new StringBuilder();
        // Table name
        tableData.append(BytesUtil.string2HexString(tableId));
        // Version
        tableData.append(version);
        // The number of characters per line
        tableData.append("32");
        // Image format: Binary image
        tableData.append("00");
        // Maximum image width
        tableData.append("0384");
        // Maximum image length
        tableData.append("0000");
        // Support Bar code/QR code
        tableData.append("03");
        // Maximum print data length
        tableData.append("9999");

        return addLength(tableData.toString());
    }

    public static SlipPrintingTable parse(String data) {
        SlipPrintingTable table = new SlipPrintingTable();
        table.setPrintingData(data);
        return table;
    }

    public String getPrintingData() {
        return printingData;
    }

    public void setPrintingData(String printingData) {
        this.printingData = printingData;
    }
}
