package com.evp.payment.ksher.function.settlement

import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.evp.payment.ksher.R
import com.evp.payment.ksher.config.ConfigModel
import com.evp.payment.ksher.database.SettlementItemModel
import com.evp.payment.ksher.database.SettlementModel
import com.evp.payment.ksher.database.repository.SettlementRepository
import com.evp.payment.ksher.database.repository.SuspendedRepository
import com.evp.payment.ksher.database.repository.TransactionRepository
import com.evp.payment.ksher.database.table.SettlementDataModel
import com.evp.payment.ksher.function.main.InputPasswordDialog
import com.evp.payment.ksher.parameter.ControllerParam
import com.evp.payment.ksher.parameter.MerchantParam
import com.evp.payment.ksher.parameter.SystemParam
import com.evp.payment.ksher.parameter.TerminalParam
import com.evp.payment.ksher.printing.TransactionPrinting
import com.evp.payment.ksher.utils.DeviceUtil
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.utils.constant.PaymentAction
import com.evp.payment.ksher.utils.alarm.Util
import com.evp.payment.ksher.utils.constant.PasswordType
import com.evp.payment.ksher.utils.sharedpreferences.SharedPreferencesUtil
import com.evp.payment.ksher.view.dialog.DialogUtils
import com.evp.payment.ksher.view.progressbar.ProgressNotifier
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Completable
import kotlinx.android.synthetic.main.activity_auto_settlement.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AutoSettlementActivity : AppCompatActivity() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Inject
    lateinit var settlementRepository: SettlementRepository

    @Inject
    lateinit var suspendedRepository: SuspendedRepository

    lateinit var configModel: ConfigModel

    var paymentChannelList =
        arrayListOf("linepay", "promptpay", "truemoney", "alipay", "wechat", "airpay")
    var settlementModel = SettlementModel()
    var grandTotalAmount = SettlementItemModel()
    val countDown = 30
    val timer = (0..countDown)
        .asSequence()
        .asFlow()
        .onEach { delay(1_000) } // specify delay

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)


        val json = SharedPreferencesUtil.getString("config_file", "")
        configModel = Gson().fromJson(json.get().toString(), ConfigModel::class.java)
        if (configModel?.data?.menusSale?.payment?.isNotEmpty() == true) {
            paymentChannelList = arrayListOf()
            configModel?.data?.menusSale?.payment?.forEach {
                it?.paymentType?.let { payment ->
                    paymentChannelList.add(payment)
                }
            }
        }


        supportActionBar?.title = "";
        supportActionBar?.hide()
        actionBar?.hide()
        setContentView(R.layout.activity_auto_settlement)

        CoroutineScope(Dispatchers.Default).launch {
            // Create the timer flow
            timer.collect {
                if (this@AutoSettlementActivity.isFinishing)
                    return@collect

                tv_content.text = String.format(
                    StringUtils.getText(configModel.data?.stringFile?.autoSettlementLabel?.label),
                    (countDown - it)
                )
                if (countDown - it == 0) {
                    querySettlement()
                    finish()
                }

            }
        }

        btn_confirm.setOnClickListener {
            val passwordDialog =
                InputPasswordDialog(
                    this,
                    InputPasswordDialog.Builder(this, PasswordType.SETTLEMENT, object :
                        InputPasswordDialog.OnPasswordActionListener {
                        override fun onSuccess(dialogFragment: InputPasswordDialog) {
                            SettlementActivity.start(
                                this@AutoSettlementActivity,
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
        }

        btnPostPone.setOnClickListener {
            Util.postPoneAlarmJob(this)
            finish()
        }
    }

    override fun onDestroy() {
        timer?.cancellable()
        super.onDestroy()
    }

    private fun querySettlement() {
        settlementModel.settlements = ArrayList()
        CoroutineScope(Dispatchers.IO).launch {
            ProgressNotifier.getInstance().show()
            ProgressNotifier.getInstance()
                .primaryContent(StringUtils.getText(configModel.data?.stringFile?.queryTransactionLabel?.label))
            paymentChannelList.forEach { paymentChannelName ->
                settlementRepository.getSettlementSaleAndRefundCountByChannel(paymentChannelName)
                    .collect {
                        Timber.d("SettlementItemModel : " + it?.getOrNull())
                        settlementModel.settlements.add(
                            (it?.getOrNull() ?: SettlementItemModel()).apply {
                                paymentChannel = paymentChannelName
                            })
                    }
            }

            settlementRepository.getAllSettlementSaleAndRefundCount().collect {
                grandTotalAmount = (it?.getOrNull() ?: SettlementItemModel()).apply {
                    paymentChannel = "GRAND TOTAL"
                }
            }
            startSettlement()
        }
    }

    suspend fun startSettlement() {
        val channelSettlementJsonString = Gson().toJson(settlementModel)
        val grandTotalAmountJsonString = Gson().toJson(grandTotalAmount)
        val settlementData = SettlementDataModel.init().apply {
            terminalId = TerminalParam.number.get()
            merchantId = MerchantParam.merchantId.get()
            hostName = MerchantParam.name.get()
            channelDatas = channelSettlementJsonString
            grandTotalData = grandTotalAmountJsonString
        }
        CoroutineScope(Dispatchers.IO).launch {
            ProgressNotifier.getInstance().show()
            ProgressNotifier.getInstance()
                .primaryContent(StringUtils.getText(configModel.data?.stringFile?.settlementStartLabel?.label))
            delay(2000)
            settlementRepository.insertSettlement(settlementData).collect {
                Timber.d("SettlementResult " + it)
                deleteCircular()
                //check result with initial transaction
//                val isSuccess = reconcileSettlement(settlementData, it?.getOrNull())
//                if (isSuccess) {
                // increase batch no
                SystemParam.incBatchNo()
                ProgressNotifier.getInstance()
                    .showApprove(StringUtils.getText(configModel.data?.stringFile?.settlementSuccessLabel?.label))
                delay(2000)
                ProgressNotifier.getInstance().dismiss()
                // print
                ControllerParam.needPrintLastTrans.set(true)
                Completable.fromAction {
                    ProgressNotifier.getInstance().show()
                }
                    .andThen(TransactionPrinting().printAnySettlement(settlementData))
                    .doOnComplete {
                        ControllerParam.needPrintLastTrans.set(false)
                        ProgressNotifier.getInstance().dismiss()
                        finish()
                    }
                    .doFinally(ProgressNotifier.getInstance()::dismiss)
                    .doOnError { e ->
//                            isProcessDone = true
                        e.printStackTrace()
                        DeviceUtil.beepErr()
                        DialogUtils.showAlertTimeout(e.message, DialogUtils.TIMEOUT_FAIL)
                    }
                    .onErrorComplete()
                    .subscribe()
//                } else {
//                    settlementRepository.deleteLastSettlement()
//                    ProgressNotifier.getInstance()
//                        .showDecline(StringUtils.getText(configModel.data?.stringFile?.settlementFailLabel?.label))
//                    ProgressNotifier.getInstance().show()
//                    delay(2000)
//                    showDialog(
//                        msg = StringUtils.getText(configModel.data?.stringFile?.settlementFailLabel?.label),
//                        actionCancel = { finish() },
//                        actionConfirm = { startSettlement() })
//                }
            }
        }
    }

    private suspend fun deleteCircular() {
        transactionRepository.deleteTransactionCircular(
            configModel.data?.setting?.systemMaxTransNumberDefault?.toInt() ?: 5000
        ).collect {}
    }

    private fun reconcileSettlement(
        settlementData: SettlementDataModel,
        settlementDataFromDB: SettlementDataModel?
    ) =
        settlementData == settlementDataFromDB
}