package com.evp.payment.ksher.view.dialog;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;


import com.evp.payment.ksher.R;
import com.evp.payment.ksher.function.BaseApplication;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

class RxDialog extends Dialog {

    @BindView(R.id.tv_content)
    TextView tvContent;
    @BindView(R.id.btn_cancel)
    Button btnCancel;
    @BindView(R.id.v_btn_divider)
    View vBtnDivider;
    @BindView(R.id.btn_confirm)
    Button btnConfirm;
    @BindView(R.id.layout_button)
    ConstraintLayout layoutButton;

    private Disposable timerDisable;

    private PublishSubject<Integer> notifier;

    public RxDialog() {
        super(BaseApplication.Companion.getAppContext(), R.style.DialogStyle);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        } else {
            getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_rx);
        ButterKnife.bind(this);
    }

    Single<Integer> show(DialogParameter param) {
        show();

        notifier = PublishSubject.create();

        tvContent.setText(param.getContent());

        updateButton(param);

        if (param.getCountDown() > 0) {
            initCountDown(param.getCountDown());
        } else if (param.getTimeout() > 0) {
            initTimeout(param.getTimeout());
        }

        return notifier.singleOrError();
    }

    private void updateButton(DialogParameter param) {
        layoutButton.setVisibility((param.isCancelEnabled() || param.isConfirmEnabled()) ? View.VISIBLE : View.GONE);
        btnCancel.setVisibility(param.isCancelEnabled() ? View.VISIBLE : View.GONE);
        btnCancel.setText(param.getCancelText());
        btnConfirm.setVisibility(param.isConfirmEnabled() ? View.VISIBLE : View.GONE);
        btnConfirm.setText(param.getConfirmText());
        vBtnDivider.setVisibility((param.isCancelEnabled() && param.isConfirmEnabled()) ? View.VISIBLE : View.GONE);
    }

    private void initCountDown(int countDown) {
        if (timerDisable != null && !timerDisable.isDisposed()) {
            timerDisable.dispose();
        }
        String confirmText = btnConfirm.getText().toString();
        Observable.intervalRange(0, countDown + 1L, 0, 1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(t -> btnConfirm.setText(confirmText + "(" + (countDown - t) + "s)"))
                .doOnTerminate(this::onConfirmClick)
                .doOnSubscribe(d -> timerDisable = d)
                .subscribe();
    }

    private void initTimeout(int timeout) {
        if (timerDisable != null && !timerDisable.isDisposed()) {
            timerDisable.dispose();
        }

        Observable.timer(timeout, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate(this::onTimeout)
                .doOnSubscribe(d -> timerDisable = d)
                .subscribe();
    }

    @OnClick(R.id.btn_cancel)
    public void onCancelClick() {
        dismiss();
        notifier.onNext(DialogEvent.CANCEL);
        notifier.onComplete();
    }

    @OnClick(R.id.btn_confirm)
    public void onConfirmClick() {
        dismiss();
        notifier.onNext(DialogEvent.CONFIRM);
        notifier.onComplete();
    }

    private void onTimeout() {
        dismiss();
        notifier.onNext(DialogEvent.TIMEOUT);
        notifier.onComplete();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (timerDisable != null && !timerDisable.isDisposed()) {
            timerDisable.dispose();
        }
    }

}
