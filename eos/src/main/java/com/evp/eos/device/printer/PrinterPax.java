package com.evp.eos.device.printer;

import android.graphics.Bitmap;

import com.evp.eos.EosService;
import com.evp.eos.R;
import com.pax.dal.IDAL;
import com.pax.dal.exceptions.PrinterDevException;

import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;

/**
 * Printer(Pax implement)
 */
public class PrinterPax implements IPrinter {

    private static PrinterPax instance;

    private com.pax.dal.IPrinter printer;

    /**
     * Print gray, default 500%
     */
    private int grayLevel = 500;

    public PrinterPax(com.pax.dal.IPrinter printer) {
        this.printer = printer;
    }

    public synchronized static PrinterPax getInstance(IDAL idal) {
        if (instance == null) {
            instance = new PrinterPax(idal.getPrinter());
        }
        return instance;
    }

    @Override
    public void setGray(int level) {
        this.grayLevel = level;
    }

    @Override
    public Completable printBitmap(Bitmap bitmap) {
        return Completable.fromAction(() -> {
            printer.init();
            printer.setGray(0);
            Thread.sleep(3000);
            printer.printBitmap(bitmap);
            int ret;
            try {
                ret = printer.start();
            } catch (PrinterDevException e) {
                throw new PrinterException(PrinterException.OTHER, e.getMessage());
            }
            handleErrorCode(ret);
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 错误码转换
     * Error code conversion
     *
     * @param ret 0: 正常 Normal
     *            1: 打印机忙 Busy
     *            2: 打印机缺纸 Out of paper
     *            3: 打印数据包格式错 Packet format error
     *            4: 打印机故障 Fault
     *            8: 打印机过热 Overheating
     *            9: 打印机电压过低 Low battery
     *            -16:打印未完成 Print incomplete
     *            -6:切刀异常(支持E500,E800) Abnormal cutter(Support E500, E800)
     *            -5:开盖错误(支持E500,E800) Open error(Support E500, E800)
     *            -4：打印机未安装字库 Not installed library
     *            -2：数据包过长 Packet Too Big
     */
    private void handleErrorCode(int ret) throws PrinterException {
        switch (ret) {
            case 0:
                return;
            case 1:
                throw new PrinterException(PrinterException.BUSY, EosService.getContext().getString(R.string.printer_busy));
            case 2:
                throw new PrinterException(PrinterException.OUT_OF_PAPER, EosService.getContext().getString(R.string.out_of_paper));
            case 8:
                throw new PrinterException(PrinterException.OVERHEAT, EosService.getContext().getString(R.string.printer_over_heating));
            case 9:
                throw new PrinterException(PrinterException.LOW_VOLTAGE, EosService.getContext().getString(R.string.voltage_is_too_low));
            default:
                throw new PrinterException(PrinterException.OTHER, EosService.getContext().getString(R.string.print_failed));
        }
    }
}
