package com.evp.payment.ksher.printing;

import android.graphics.Bitmap;

import com.evp.payment.ksher.R;
import com.evp.payment.ksher.parameter.PrintParam;
import com.evp.payment.ksher.utils.StringUtils;
import com.evp.payment.ksher.utils.constant.LocalErrorCode;
import com.evp.payment.ksher.utils.transactions.TransactionException;
import com.evp.payment.ksher.view.dialog.DialogEvent;
import com.evp.payment.ksher.view.dialog.DialogUtils;
import com.evp.payment.ksher.view.progressbar.ProgressNotifier;
import com.evp.eos.EosService;
import com.evp.eos.device.printer.PrinterException;

import org.reactivestreams.Publisher;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Flowable;



public abstract class APrinting {

    /**
     * Whether to show prompt dialog
     */
    protected boolean showPrompt = true;

    protected Completable printBitmap(Bitmap bitmap) {
        return Completable.fromAction(
                () -> {
                    if (showPrompt) {
                        ProgressNotifier.getInstance().primaryContentOnly(StringUtils.INSTANCE.getString(R.string.printing_please_wait));
                    }
                    int gray = Integer.parseInt(PrintParam.INSTANCE.getPrintGray().get());
                    EosService.getDevice().getPrinter().setGray(gray);
                })
                .andThen(EosService.getDevice().getPrinter().printBitmap(bitmap))
                .onErrorComplete(e -> !showPrompt) // 不显示打印提示则不报错 If not show print prompt, then not throw exception
                .retryWhen(throwableFlowable -> throwableFlowable.flatMap(this::handleError));
    }

    private Publisher<Integer> handleError(Throwable e) {
        if (e instanceof PrinterException) {
            PrinterException exception = (PrinterException) e;
            int code = exception.getCode();
            if (code == PrinterException.BUSY) {
                // 打印机忙
                // Printer busy
                return Flowable.timer(100, TimeUnit.MILLISECONDS)
                        .map(i -> 0);
            } else if (code == PrinterException.OUT_OF_PAPER) {
                // 缺纸
                // Out of paper
                return showConfirm(StringUtils.INSTANCE.getString(R.string.out_of_paper_please_add_paper_then_click_confirm_to_print));
            } else if (code == PrinterException.OVERHEAT) {
                // 过热
                // Over heating
                return showConfirm(StringUtils.INSTANCE.getString(R.string.printer_overheat_please_wait_then_click_confirm_to_print));
            } else if (code == PrinterException.LOW_VOLTAGE) {
                // 电压低
                // Low battery
                return showConfirm(StringUtils.INSTANCE.getString(R.string.print_voltage_is_too_low_please_charge_then_click_confirm_to_print));
            }
        }
        return Flowable.error(new TransactionException(LocalErrorCode.ERR_PRINT, StringUtils.INSTANCE.getString(R.string.print_failure_please_reprint)));
    }

    private Publisher<Integer> showConfirm(String msg) {
        // If show show dialog, then hide the progress bar
        ProgressNotifier.getInstance().dismiss();
        return DialogUtils.INSTANCE.showConfirm(msg)
                .flatMapPublisher(event -> {
                    if (event == DialogEvent.CANCEL) {
//                        if(msg == StringUtils.INSTANCE.getString(R.string.out_of_paper_please_add_paper_then_click_confirm_to_print)) {
//                            return Flowable.just(0);
//                        } else {
                            // Cancel to throw exception
                            return Flowable.error(new TransactionException(LocalErrorCode.ERR_PRINT, StringUtils.INSTANCE.getString(R.string.print_failure_please_reprint)));
//                        }
                    } else {
                        // Confirm to continue print
                        ProgressNotifier.getInstance().show();
                        return Flowable.just(0);
                    }
                });
    }

    /**
     * Get print slip number
     */
    protected int getSlipCount() {
        try {
            int count = Integer.parseInt(PrintParam.INSTANCE.getPrintSlipNum().get());
            if (count > 3 || count < 0) {
                return 2;
            } else {
                return count;
            }
        } catch (Exception e) {
            return 2;
        }
    }
}
