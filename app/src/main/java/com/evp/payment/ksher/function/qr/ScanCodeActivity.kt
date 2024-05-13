package com.evp.payment.ksher.function.qr

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.Camera
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.OnClick
import com.evp.payment.ksher.R
import com.evp.payment.ksher.customview.ScanCodeView
import com.evp.payment.ksher.database.repository.SettlementRepository
import com.evp.payment.ksher.database.repository.SuspendedRepository
import com.evp.payment.ksher.database.repository.TransactionRepository
import com.evp.payment.ksher.database.table.TransDataModel
import com.evp.payment.ksher.extension.extra
import com.evp.payment.ksher.extension.extraNotNull
import com.evp.payment.ksher.extension.gone
import com.evp.payment.ksher.function.BaseApplication
import com.evp.payment.ksher.function.main.BaseTimeoutActivity
import com.evp.payment.ksher.function.main.InputPasswordDialog
import com.evp.payment.ksher.function.payment.PaymentAmountConfirmActivity
import com.evp.payment.ksher.function.payment.action.print.PrintActivity
import com.evp.payment.ksher.function.qr.view.ScanCodeContact
import com.evp.payment.ksher.function.settlement.SettlementActivity
import com.evp.payment.ksher.parameter.SystemParam.Companion.frontCameraScanEnabled
import com.evp.payment.ksher.utils.DeviceUtil
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.utils.constant.PasswordType
import com.evp.payment.ksher.utils.constant.PaymentAction
import com.evp.payment.ksher.utils.sharedpreferences.OnClickUtils.isShakeClick
import com.google.gson.Gson
import com.icg.scancode.QRCodeView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ScanCodeActivity : BaseTimeoutActivity(), QRCodeView.Delegate, ScanCodeContact.View {
    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Inject
    lateinit var settlementRepository: SettlementRepository

    @BindView(R.id.header_title)
    lateinit var headerTitle: TextView

    @BindView(R.id.scancodeview)
    lateinit var scanCodeView: ScanCodeView

    @BindView(R.id.iv_flash)
    lateinit var ivFlash: ImageView

    @BindView(R.id.tv_flash)
    lateinit var tvFlash: TextView

    @BindView(R.id.tv_cancel)
    lateinit var tvCancel: TextView

    @BindView(R.id.layout_sub_header)
    lateinit var layoutSubHeader: LinearLayout

    @BindView(R.id.tv_sub_header_detail)
    lateinit var ivSubHeaderDetail: TextView

    @BindView(R.id.iv_sub_header_channel)
    lateinit var ivSubHeader: AppCompatImageView

    @BindView(R.id.tv_sub_header_amount)
    lateinit var ivSubAmount: AppCompatTextView

    private var isFlashOpen = false
    private var isFrontCamera = false
    private var requestEvent: ScanCodeRequestEvent? = null

    private val ACTION_METHOD_TYPE by extraNotNull<String>(PaymentAction.KEY_ACTION)
    private val payChannel by extraNotNull<String>("pay_channel")
    private val payAmount by extra<String>("pay_amount", "0")
    private val mediaType by extra<String>("media_type", "")

    override val isNavBarTransparent: Boolean
        get() = true

    override fun loadParam() {
        requestEvent =
            intent.getSerializableExtra(ScanCodeRequestEvent::class.java.simpleName) as ScanCodeRequestEvent?
        isFrontCamera = frontCameraScanEnabled.get()!!
    }

    override val layoutId: Int
        get() = R.layout.activity_scan_code

    override fun initViews() {
//        layoutHeader.gone()
        disableCountDown()
        layoutHeader.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))
        headerTitle.text = requestEvent?.title
        ivFlash.visibility = View.GONE
        tvFlash.visibility = View.GONE
        tvCancel.text = StringUtils.getText(configModel.data?.button?.buttonCancel?.label)
        when (ACTION_METHOD_TYPE) {
            PaymentAction.SCAN -> checkForceSettlement {
                scanCodeView.setDelegate(this)
                startCountDown()
            }
            else -> {
                scanCodeView.setDelegate(this)
                startCountDown()
            }
        }
        showPaymentBanner()
//        Toast.makeText(this, "$payChannel $payAmount", Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        if (isFrontCamera) {
            scanCodeView.startCamera(Camera.CameraInfo.CAMERA_FACING_FRONT)
        } else {
            scanCodeView.startCamera(Camera.CameraInfo.CAMERA_FACING_BACK)
        }
    }

    override fun onScanQRCodeSuccess(result: String) {
        DeviceUtil.beepScan()
        when (ACTION_METHOD_TYPE) {
            PaymentAction.SCAN -> {
                if (!mediaType.isNullOrEmpty()) {
                    PaymentAmountConfirmActivity.startWithInvoke(
                        this,
                        result,
                        payAmount,
                        payChannel,
                        mediaType
                    )
                } else {
                    PaymentAmountConfirmActivity.start(this, result, payAmount, payChannel)
                }
            }
            PaymentAction.VOID -> {
                setResult(Activity.RESULT_OK, Intent().apply {
                    data = Uri.parse(result)
                })
                finish()
            }
            PaymentAction.PRINT -> {

            }
            PaymentAction.QUERY -> {

            }
            PaymentAction.REPORT -> {

            }
            PaymentAction.AUDIT_REPORT -> {

            }
            PaymentAction.SETTLEMENT -> {

            }
        }
    }

    override fun onCameraAmbientBrightnessChanged(isDark: Boolean) {
        if (isFrontCamera) return
        if (isDark || isFlashOpen) {
            ivFlash.visibility = View.VISIBLE
            tvFlash.visibility = View.VISIBLE
        } else {
            ivFlash.visibility = View.GONE
            tvFlash.visibility = View.GONE
        }
    }

    override fun onScanQRCodeOpenCameraError() {
        Log.e(TAG, "onScanQRCodeOpenCameraError")
    }

    @OnClick(R.id.iv_flash, R.id.tv_flash)
    override fun onFlashClick() {
        if (isFlashOpen) {
            scanCodeView.closeFlashlight()
            ivFlash.setImageResource(R.drawable.icon_flashlight_close)
        } else {
            scanCodeView.openFlashlight()
            ivFlash.setImageResource(R.drawable.icon_flashlight_open)
        }
        isFlashOpen = !isFlashOpen
    }

    @OnClick(R.id.btn_switch_camera)
    override fun onClickSwitchCamera() {
        scanCodeView.closeFlashlight()
        ivFlash.setImageResource(R.drawable.icon_flashlight_close)
        isFlashOpen = false
        scanCodeView.stopCamera()

        isFrontCamera = !isFrontCamera

        if (isFrontCamera) {
            scanCodeView.startCamera(Camera.CameraInfo.CAMERA_FACING_FRONT)
        } else {
            scanCodeView.startCamera(Camera.CameraInfo.CAMERA_FACING_BACK)
        }
        scanCodeView.showScanRect()

    }

    @OnClick(R.id.tv_cancel)
    override fun onCancelClick() {
        finish()
    }

    override fun onBackPressed() {
        if (isShakeClick(this)) return
        val intent = Intent()
        intent.putExtra(BACK, true)
        setResult(0, intent)
        if (requestEvent != null && requestEvent!!.isNotifyByEvent) {
            sendCancelEvent()
        }
        finish()
    }

    override fun onTimeout() {
        val intent = Intent()
        intent.putExtra(TIMEOUT, true)
        setResult(0, intent)
        if (requestEvent!!.isNotifyByEvent) {
            sendTimeoutEvent()
        }
        finish()
    }

    override fun showPaymentBanner() {
        layoutSubHeader.visibility = View.VISIBLE
        ivSubHeader.visibility = View.VISIBLE
        ivSubAmount.visibility = View.GONE
        ivSubHeaderDetail.visibility = View.GONE
        setupSubHeaderLogo(ivSubHeader, payChannel)
    }

    @OnClick(R.id.layout_back)
    override fun onBackClick() {
        onBackPressed()
    }

    override fun onStop() {
        // Close flash light
        scanCodeView.closeFlashlight()
        ivFlash.setImageResource(R.drawable.icon_flashlight_close)
        isFlashOpen = false
        scanCodeView.stopCamera()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        scanCodeView.onDestroy()
    }

    companion object {
        private const val TAG = "ScanCodeActivity"
        const val BACK = "BACK"
        const val TIMEOUT = "TIMEOUT"
        const val MANUAL_INPUT = "MANUAL_INPUT"
        const val CODE = "CODE"

        @JvmStatic
        fun start(context: Activity?, keyAction: String, amount: String, channel: String?) {
            val intent = Intent(context, ScanCodeActivity::class.java)
            val bundle = Bundle()
            val requestEvent = ScanCodeRequestEvent()
            requestEvent.title = channel
            requestEvent.isNotifyByEvent = true
            requestEvent.isManualInputEnabled = true
            bundle.putSerializable(ScanCodeRequestEvent::class.java.simpleName, requestEvent)
            intent.putExtra(PaymentAction.KEY_ACTION, keyAction)
            intent.putExtra("pay_amount", StringUtils.toDisplayAmount(amount, 2))
            intent.putExtra("pay_channel", channel)
            intent.putExtras(bundle)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
            context?.finish()
        }

        @JvmStatic
        fun startWithInvoke(
            context: Activity?,
            amount: String,
            channel: String?,
            mediaType: String
        ) {
            val intent = Intent(context, ScanCodeActivity::class.java)
            val bundle = Bundle()
            val requestEvent = ScanCodeRequestEvent()
            requestEvent.title = channel
            requestEvent.isNotifyByEvent = true
            requestEvent.isManualInputEnabled = true
            bundle.putSerializable(ScanCodeRequestEvent::class.java.simpleName, requestEvent)
            intent.putExtra(PaymentAction.KEY_ACTION, PaymentAction.SCAN)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("pay_amount", StringUtils.toDisplayAmount(amount, 2))
            intent.putExtra("pay_channel", channel)
            intent.putExtra("media_type", mediaType)
            intent.putExtras(bundle)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
            context?.finish()
        }

    }

    private fun checkForceSettlement(action: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                transactionRepository.getYesterdayTransaction().collect { transaction ->
                    Timber.d("transaction : " + transaction?.getOrNull())
                    if (transaction?.getOrNull() == null || (transaction.getOrNull()!!.saleTotalAmount == 0 && transaction.getOrNull()!!.refundTotalAmount == 0)) {
                        CoroutineScope(Dispatchers.Main).launch {
                            action.invoke()
                        }
                    } else {
                        val yesterdayTransaction = transaction.getOrNull()
                        settlementRepository.getLastSettlement().collect { settlement ->
                            if (settlement?.getOrNull() == null) {
                                if (yesterdayTransaction!!.saleTotalAmount > 0 || yesterdayTransaction.refundTotalAmount > 0) {
                                    showForceSettlementDialog()
                                } else {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        action.invoke()
                                    }
                                }
                            } else {
                                val lastSettlement = settlement.getOrNull()
                                if (yesterdayTransaction!!.batchNo?.toInt()!! > lastSettlement!!.batchNo?.toInt() ?: 0)
                                    if (yesterdayTransaction.saleTotalAmount > 0 || yesterdayTransaction.refundTotalAmount > 0) {
                                        showForceSettlementDialog()
                                    } else {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            action.invoke()
                                        }
                                    }
                                else {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        action.invoke()
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
                e.printStackTrace()
                action.invoke()
            }
        }
    }

    private fun showForceSettlementDialog() {
        showDialog(
            msg = StringUtils.getText(configModel.data?.stringFile?.forceSettlementLabel?.label),
            actionCancel = { finish() },
            actionConfirm = {
                val passwordDialog =
                    InputPasswordDialog(
                        this@ScanCodeActivity,
                        InputPasswordDialog.Builder(
                            this@ScanCodeActivity,
                            PasswordType.SETTLEMENT,
                            object :
                                InputPasswordDialog.OnPasswordActionListener {
                                override fun onSuccess(
                                    dialogFragment: InputPasswordDialog
                                ) {
                                    SettlementActivity.start(
                                        this@ScanCodeActivity,
                                        PaymentAction.SETTLEMENT,
                                        false
                                    )
                                    dialogFragment.dismiss()
                                    finish()
                                }

                                override fun onFail(dialogFragment: InputPasswordDialog) {
                                    dialogFragment.dismiss()
                                    finish()
                                }
                            })
                    )
                passwordDialog.show()
            })
    }
}