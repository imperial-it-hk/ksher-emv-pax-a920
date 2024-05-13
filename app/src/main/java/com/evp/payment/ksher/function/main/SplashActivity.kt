package com.evp.payment.ksher.function.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.evp.payment.ksher.config.ConfigModel
import com.evp.payment.ksher.extension.extra
import com.evp.payment.ksher.function.BaseApplication
import com.evp.payment.ksher.function.history.TransactionHistoryActivity
import com.evp.payment.ksher.function.payment.action.InputAmountActivity
import com.evp.payment.ksher.function.qr.GeneratorQRActivity
import com.evp.payment.ksher.function.qr.ScanCodeActivity
import com.evp.payment.ksher.function.settings.SettingActivity
import com.evp.payment.ksher.function.settlement.SettlementActivity
import com.evp.payment.ksher.function.voided.InputTransactionActivity
import com.evp.payment.ksher.invoke.InvokeModel
import com.evp.payment.ksher.parameter.SystemParam
import com.evp.payment.ksher.utils.*
import com.evp.payment.ksher.utils.StringUtils.toDisplayAmount
import com.evp.payment.ksher.utils.ToastUtils.showMessage
import com.evp.payment.ksher.utils.constant.InvokeConstant
import com.evp.payment.ksher.utils.constant.PasswordType
import com.evp.payment.ksher.utils.constant.PaymentAction
import com.evp.payment.ksher.utils.sharedpreferences.SharedPreferencesUtil
import com.evp.payment.ksher.view.dialog.DialogUtils
import com.evp.payment.ksher.view.progressbar.ProgressNotifier
import com.google.gson.Gson
import com.evp.eos.utils.LogUtil
import com.evp.payment.ksher.R
import com.evp.payment.ksher.function.about.AboutActivity
import com.evp.payment.ksher.parameter.AppStoreParam
import com.evp.payment.ksher.utils.alarm.Util
import com.evp.payment.ksher.view.EVPDialog
import com.evp.payment.ksher.view.EVPSingleChoiceDialog
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class SplashActivity : AppCompatActivity() {
    private val invokeData by extra("request_payment_ksher", "")
    private lateinit var configModel: ConfigModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
//        val json = SharedPreferencesUtil.getString("config_file", "")
//        configModel = Gson().fromJson(json.get().toString(), ConfigModel::class.java)
//        SharedPreferencesUtil.getString("config_setting_language", "").set(configModel.data?.setting?.language)

        if (!isTaskRoot) {
            finish()
            return
        }
        DeviceUtil.setDeviceEnableExit()
        checkConfigFile()

//        // Set language
//        LanguageSettingUtil(this).setLanguage(SystemParam.language.get())
//        Completable.fromAction {
//            ProgressNotifier.getInstance()::show
//            ProgressNotifier.getInstance()
//                .primaryContent("Check update.")
//        }
//            .andThen(Normalizer().normalize(false))
//            .doOnComplete {
////                    val json = SharedPreferencesUtil.getString("config_file", "")
////                    configModel = Gson().fromJson(json.get().toString(), ConfigModel::class.java)
////                    SharedPreferencesUtil.getString("config_setting_language", "")
////                        .set(configModel.data?.setting?.language)
//                // Set language
////                    LanguageSettingUtil(this).setLanguage(SystemParam.language.get())
//                checkEnvironment()
//
//            }.doFinally(ProgressNotifier.getInstance()::dismiss)
//            .onErrorResumeNext { e ->
//                DeviceUtil.beepErr()
//                DialogUtils.showAlertTimeout(e.message, DialogUtils.TIMEOUT_FAIL)
//            }.subscribe()
//        checkEnvironment()

    }

    var count = 0
    private fun checkConfigFile() {
        CoroutineScope(Dispatchers.IO).launch {
            val one = async { AppStoreParam.updateParams(false) }
            when (one.await()) {
                "SUCCESS" -> checkEnvironment()
                else -> {
                    if(count >= 10) {
                        DeviceUtil.beepErr()
                        DeviceUtil.restoreDeviceStatus()
                        EVPSingleChoiceDialog("Config file not found please contact Admin.") { finish() }.show(
                            supportFragmentManager,
                            "custom_dialog"
                        )
                    } else {
                        delay(1000)
                        count++
                        checkConfigFile()
                    }
//                    DeviceUtil.beepErr()
//                    EVPDialog(result, { finish() }, { checkEnvironment() }).show(
//                        supportFragmentManager,
//                        "custom_dialog"
//                    )
//                    DialogUtils.showAlertTimeout(result, DialogUtils.TIMEOUT_FAIL)

                }
            }
        }
    }

    private fun checkEnvironment() {
        SystemParam.isInvokeFlag.set(false)
        // Initialize
        Initiator.initialize().observeOn(AndroidSchedulers.mainThread()).doOnComplete {
            if (!invokeData.isNullOrEmpty()) {
                SystemParam.isInvokeFlag.set(true)
                val json = SharedPreferencesUtil.getString("config_file", "")
                configModel = Gson().fromJson(json.get().toString(), ConfigModel::class.java)
                checkInvokeFunction()
            } else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.onErrorResumeNext { e: Throwable ->
            LogUtil.e(TAG, e)
            showMessage(e.message!!)
            Completable.timer(3, TimeUnit.SECONDS).doOnComplete(BaseApplication.Companion::exitApp)
        }.subscribe()
    }

    private fun checkInvokeFunction() {
        try {
            if (!invokeData.isNullOrEmpty()) {
                // Initialize
                Initiator.initialize().observeOn(AndroidSchedulers.mainThread()).doOnComplete {
                    val invokeModel: InvokeModel =
                        Gson().fromJson(invokeData, InvokeModel::class.java)
                    val outputJson: String = Gson().toJson(invokeModel)
                    Log.d("invoke", outputJson)
                    Log.d("invoke", invokeModel.data?.amount.toString())

                    if(!invokeModel.data?.language.isNullOrEmpty()){
                        SystemParam.language.set(invokeModel.data?.language)
                    }

                    if (InvokeConstant.TRANSACTION_TYPE_SALE.equals(
                            invokeModel.data?.transaction_type,
                            true
                        )
                    ) {
                        if (InvokeConstant.MEDIA_TYPE_C2B.equals(
                                invokeModel.data?.media_type,
                                true
                            )
                        ) {
                            if (invokeModel.data?.amount.toString().equals("0", true)) {
                                InputAmountActivity.startWithInvoke(
                                    this,
                                    toDisplayAmount(invokeModel.data?.amount.toString(), 2),
                                    invokeModel.data?.payment_type,
                                    InvokeConstant.MEDIA_TYPE_C2B
                                )
                            } else {
                                GeneratorQRActivity.startWithInvoke(
                                    this,
                                    toDisplayAmount(invokeModel.data?.amount.toString(), 2),
                                    invokeModel.data?.payment_type,
                                    InvokeConstant.MEDIA_TYPE_C2B
                                )
                            }
                        }
                        if (InvokeConstant.MEDIA_TYPE_B2C.equals(
                                invokeModel.data?.media_type,
                                true
                            )
                        ) {
                            ScanCodeActivity.startWithInvoke(
                                this,
                                toDisplayAmount(invokeModel.data?.amount.toString(), 2),
                                invokeModel.data?.payment_type, InvokeConstant.MEDIA_TYPE_B2C
                            )
                        }
                    } else if (InvokeConstant.TRANSACTION_TYPE_VOID.equals(
                            invokeModel.data?.transaction_type,
                            true
                        )
                    ) {
                        val passwordDialog = InputPasswordDialog(
                            this,
                            InputPasswordDialog.Builder(this, PasswordType.VOID, object :
                                InputPasswordDialog.OnPasswordActionListener {
                                override fun onSuccess(dialogFragment: InputPasswordDialog) {
                                    InputTransactionActivity.startWithInvoke(
                                        this@SplashActivity,
                                        PaymentAction.VOID
                                    )
                                    dialogFragment.dismiss()
                                }

                                override fun onFail(dialogFragment: InputPasswordDialog) {
                                    dialogFragment.dismiss()
                                    finish()
                                }
                            })
                        )
                        passwordDialog.show()
                    } else if (InvokeConstant.TRANSACTION_TYPE_REFUND.equals(
                            invokeModel.data?.transaction_type,
                            true
                        )
                    ) {
                        val passwordDialog = InputPasswordDialog(
                            this,
                            InputPasswordDialog.Builder(this, PasswordType.VOID, object :
                                InputPasswordDialog.OnPasswordActionListener {
                                override fun onSuccess(dialogFragment: InputPasswordDialog) {
                                    InputTransactionActivity.startWithInvoke(
                                        this@SplashActivity,
                                        PaymentAction.VOID
                                    )
                                    dialogFragment.dismiss()
                                }

                                override fun onFail(dialogFragment: InputPasswordDialog) {
                                    dialogFragment.dismiss()
                                    finish()
                                }
                            })
                        )
                        passwordDialog.show()
                    } else if (InvokeConstant.TRANSACTION_TYPE_PREAUTH.equals(
                            invokeModel.data?.transaction_type,
                            true
                        )
                    ) {

                    } else if (InvokeConstant.TRANSACTION_TYPE_AUTH.equals(
                            invokeModel.data?.transaction_type,
                            true
                        )
                    ) {

                    } else if (InvokeConstant.TRANSACTION_TYPE_TRANSLOG.equals(
                            invokeModel.data?.transaction_type,
                            true
                        )
                    ) {
                        TransactionHistoryActivity.startWithInvoke(this)
                        finish()
                    } else if (InvokeConstant.TRANSACTION_TYPE_REPRINT.equals(
                            invokeModel.data?.transaction_type,
                            true
                        )
                    ) {
                        PaymentActionTypeActivity.startWithInvoke(
                            this,
                            PaymentAction.PRINT,
                            StringUtils.getText(configModel.data?.stringFile?.selectReportTypeLabel?.label)
                        )
                        finish()
                    } else if (InvokeConstant.TRANSACTION_TYPE_REPORT.equals(
                            invokeModel.data?.transaction_type,
                            true
                        )
                    ) {
                        PaymentActionTypeActivity.startWithInvoke(
                            this,
                            PaymentAction.REPORT,
                            StringUtils.getText(configModel.data?.stringFile?.selectReportTypeLabel?.label)
                        )
                        finish()
                    } else if (InvokeConstant.TRANSACTION_TYPE_SETTLEMENT.equals(
                            invokeModel.data?.transaction_type,
                            true
                        )
                    ) {
                        val passwordDialog =
                            InputPasswordDialog(
                                this,
                                InputPasswordDialog.Builder(this, PasswordType.SETTLEMENT, object :
                                    InputPasswordDialog.OnPasswordActionListener {
                                    override fun onSuccess(dialogFragment: InputPasswordDialog) {
                                        SettlementActivity.startWithInvoke(
                                            this@SplashActivity,
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
                    } else if (InvokeConstant.TRANSACTION_TYPE_ENQUIRY.equals(
                            invokeModel.data?.transaction_type,
                            true
                        )
                    ) {
                        PaymentActionTypeActivity.startWithInvoke(
                            this,
                            PaymentAction.QUERY,
                            StringUtils.getText(configModel.data?.stringFile?.qrInquiryLabel?.label)
                        )
                        finish()
                    } else if (InvokeConstant.TRANSACTION_TYPE_OFFLINESALE.equals(
                            invokeModel.data?.transaction_type,
                            true
                        )
                    ) {

                    } else if (InvokeConstant.TRANSACTION_TYPE_OFFLINEREFUND.equals(
                            invokeModel.data?.transaction_type,
                            true
                        )
                    ) {

                    } else if (InvokeConstant.TRANSACTION_TYPE_INSTALLMENT.equals(
                            invokeModel.data?.transaction_type,
                            true
                        )
                    ) {

                    } else if (InvokeConstant.TRANSACTION_TYPE_REDEMPTION.equals(
                            invokeModel.data?.transaction_type,
                            true
                        )
                    ) {

                    } else if (InvokeConstant.TRANSACTION_TYPE_SETTING.equals(
                            invokeModel.data?.transaction_type,
                            true
                        )
                    ) {
                        SettingActivity.startWithInvoke(this)
                        finish()
                    }else if (InvokeConstant.TRANSACTION_TYPE_ABOUT.equals(
                            invokeModel.data?.transaction_type,
                            true
                        )
                    ) {
                        AboutActivity.start(this)
                        finish()
                    }
                }.onErrorResumeNext { e: Throwable ->
                    LogUtil.e(TAG, e)
                    showMessage(e.message!!)
                    Completable.timer(3, TimeUnit.SECONDS)
                        .doOnComplete(BaseApplication.Companion::exitApp)
                }.subscribe()
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, "Some thing went wrong, Please try again.", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

//    private fun updateConfig(): Completable{
//        return Completable.complete().doOnComplete {
//            val json = SharedPreferencesUtil.getString("config_file", "")
//            configModel = Gson().fromJson(json.get().toString(), ConfigModel::class.java)
//            SharedPreferencesUtil.getString("config_setting_language", "").set(configModel.data?.setting?.language)
    // Set language
//            LanguageSettingUtil(this).setLanguage(SystemParam.language.get())
//        }
//    }

//    override fun onStart() {
//        super.onStart()
//        runOnUiThread {
//        Completable.fromAction(ProgressNotifier.getInstance()::show)
//            .andThen(Normalizer().normalize())
////            .andThen(updateConfig())
//            .doOnComplete {
//                val json = SharedPreferencesUtil.getString("config_file", "")
//                configModel = Gson().fromJson(json.get().toString(), ConfigModel::class.java)
//                SharedPreferencesUtil.getString("config_setting_language", "").set(configModel.data?.setting?.language)
//                // Set language
//                LanguageSettingUtil(this).setLanguage(SystemParam.language.get())
//            }
//            .doFinally(ProgressNotifier.getInstance()::dismiss)
//            .onErrorResumeNext { e ->
//                DeviceUtil.beepErr()
//                DialogUtils.showAlertTimeout(e.message, DialogUtils.TIMEOUT_FAIL)
//            }.subscribe()
//        }
//    }

    override fun onBackPressed() {
        // Disable back button
    }

    companion object {
        private const val TAG = "SplashActivity"
    }
}