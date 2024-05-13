package com.evp.payment.ksher.function.inquiry.suspendedqr

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.evp.payment.ksher.R
import com.evp.payment.ksher.function.main.BaseTimeoutActivity

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.evp.payment.ksher.database.*
import com.evp.payment.ksher.database.repository.SettlementRepository
import com.evp.payment.ksher.database.repository.SuspendedRepository
import com.evp.payment.ksher.database.repository.TransactionRepository
import com.evp.payment.ksher.database.table.SuspendedQrDataModel
import com.evp.payment.ksher.database.table.TransDataModel
import com.evp.payment.ksher.extension.*
import com.evp.payment.ksher.function.main.ConfirmSuspendedDialog
import com.evp.payment.ksher.function.payment.action.print.PrintActivity
import com.evp.payment.ksher.parameter.ControllerParam
import com.evp.payment.ksher.parameter.SystemParam
import com.evp.payment.ksher.printing.TransactionPrinting
import com.evp.payment.ksher.utils.*
import com.evp.payment.ksher.utils.DateUtils.getAbbreviatedFromDateTime
import com.evp.payment.ksher.utils.constant.KsherConstant
import com.evp.payment.ksher.view.dialog.DialogUtils
import com.evp.payment.ksher.view.progressbar.ProgressNotifier
import com.ksher.ksher_sdk.Ksher_pay_sdk
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Completable
import kotlinx.android.synthetic.main.activity_history_detail_by_date.*
import kotlinx.android.synthetic.main.header_layout.*

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.json.JSONObject
import timber.log.Timber
import java.lang.Exception
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class SuspendedQrActivity : BaseTimeoutActivity() {
    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Inject
    lateinit var settlementRepository: SettlementRepository

    @Inject
    lateinit var suspendedRepository: SuspendedRepository

    private val ksherPay: Ksher_pay_sdk by lazy {
        Ksher_pay_sdk(
            SystemParam.appIdOnline.get(),
            SystemParam.tokenOnline.decrypt(),
            SystemParam.paymentDomain.get(),
            SystemParam.gateWayDomain.get(),
            SystemParam.publicKey.get(),
            SystemParam.communicationMode.get()
        )
    }

    var offset = 0
    var limit = 25
    var isNoMore = false
    var isLoading = true

    private lateinit var recyclerViewAdapter: SuspenededQrItemAdapter

    private fun showConfirmInquiry(suspendedQrDataModel: SuspendedQrDataModel) {
        disableCountDown()
        val confirmDialog = ConfirmSuspendedDialog(
            this, ConfirmSuspendedDialog.Builder(this,
                StringUtils.getText(configModel.data?.stringFile?.qrInquiryLabel?.label),
                StringUtils.getText(configModel.data?.button?.buttonSuspendedQR?.label),
                suspendedQrDataModel,
                object : ConfirmSuspendedDialog.OnActionListener {
                    override fun onSuccess(
                        transData: SuspendedQrDataModel,
                        dialogFragment: ConfirmSuspendedDialog
                    ) {
                        dialogFragment.dismiss()
                        inquiry(transData)
                    }

                    override fun onFail(
                        transData: SuspendedQrDataModel,
                        dialogFragment: ConfirmSuspendedDialog
                    ) {
                        dialogFragment.dismiss()
                        showDialog(
                            msg = StringUtils.getText(configModel.data?.stringFile?.deleteSuspendQRTracNoLabel?.label) + " ${transData.traceNo}",
                            actionCancel = { startCountDown() },
                            actionConfirm = { inquiry(transData, true) })
                    }
                })
        )
        confirmDialog.show()
//        startActivityForResult(s
//            Intent(
//                this@SuspendedQrActivity,
//                HistoryDetailActivity::class.java
//            ).apply {
//                putExtra("mchOrderNo", suspendedQrDataModel.mchOrderNo)
//            }, 500
//        )
    }

    private fun inquiry(transData: SuspendedQrDataModel, isDelete: Boolean? = false) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ProgressNotifier.getInstance().show()
                ProgressNotifier.getInstance()
                    .primaryContent(StringUtils.getText(configModel.data?.stringFile?.inquiryLabel?.label))
                inquiryOnlineProcess(transData, isDelete)
            }catch (e: Exception){
                startCountDown()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 500) {
            if (resultCode == Activity.RESULT_OK) {
                reloadData()
            }
        }
    }


    var historyDataList: ArrayList<SuspendedQrData> = arrayListOf()

    @BindView(R.id.layout_sub_header)
    lateinit var layoutSubHeader: View

    override fun loadParam() {

    }

    override val layoutId: Int
        get() = R.layout.activity_history_detail_by_date

    override fun initViews() {

        recyclerViewAdapter = SuspenededQrItemAdapter(arrayListOf(), true, configModel) {
                showConfirmInquiry(it)
        }

        layoutSubHeader.visible()
        tv_sub_header_title.visible()
        tv_sub_header_detail.visible()
        tv_sub_header_title.text =
            StringUtils.getText(configModel.data?.stringFile?.qrInquiryLabel?.label)
        tv_sub_header_detail.text =
            StringUtils.getText(configModel.data?.button?.buttonSuspendedQR?.label)
        layout_back.setOnClickListener { onBackPressed() }

        setupRecyclerView()
        querySuspeneded()
    }

    private fun querySuspeneded() {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                isLoading = true
                ProgressNotifier.getInstance().show()
                ProgressNotifier.getInstance()
                    .primaryContent(StringUtils.getText(configModel.data?.stringFile?.querySuspendedQRLabel?.label))
                suspendedRepository.getAllSuspendedQr("PENDING", limit = limit, offset = offset)
                    .collect {
                        val historys = it?.getOrNull()
                        if (historys.isNullOrEmpty()) {
                            ProgressNotifier.getInstance().dismiss()
                            CoroutineScope(Dispatchers.Main).launch {
                                showDialog(
                                    msg = StringUtils.getText(configModel.data?.stringFile?.noSuspendedQRFoundLabel?.label),
                                    actionCancel = { finish() },
                                    actionConfirm = { finish() })
                            }
                            return@collect
                        }
                        val size = historys.size ?: 0
                        if (size < limit)
                            isNoMore = true
                        offset += size

                        historys.let {
                            historyDataList.addAll(getHistoryListWithMonthAndYearHeader(it))
                            CoroutineScope(Dispatchers.Main).launch {
                                recyclerViewAdapter.onUpdateItem(historyDataList)
                                ProgressNotifier.getInstance().dismiss()
                                isLoading = false
                                startCountDown()
                            }
                        }
                    }
            }
        } catch (e: Exception) {
            ProgressNotifier.getInstance().dismiss()
            e.printStackTrace()
        }
    }

    fun getHistoryListWithMonthAndYearHeader(list: List<SuspendedQrDataModel>): List<SuspendedQrData> {
        val a: ArrayList<SuspendedQrData> = arrayListOf()
        list.forEachIndexed { i, e ->
            val monthAndYearPreviousItem =
                list.getOrNull(i - 1)?.year + list.getOrNull(i - 1)?.date
            val monthAndYearCurrentItem = list.getOrNull(i)?.year + list.getOrNull(i)?.date
            val last = historyDataList.lastOrNull()
            var lastMonthAndYearCurrentItem = ""
            if (last != null) {
                last as TransDataModel
                lastMonthAndYearCurrentItem = last.year + last.date
            }
            if (monthAndYearCurrentItem != monthAndYearPreviousItem && lastMonthAndYearCurrentItem != monthAndYearCurrentItem) {
                a.add(
                    SuspendedQrHeader(
                        DateUtils.getFormattedDate(
                            monthAndYearCurrentItem,
                            "yyyyMMdd",
                            "MMM dd, yy"
                        )
                    )
                )
            }
            a.add(e)
        }
        return a
    }

    private fun setupRecyclerView() {
        recyclerViewHistory.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = recyclerViewAdapter
            addItemDecoration(HeaderItemDecoration(this) { itemPosition ->
                this@SuspendedQrActivity.recyclerViewAdapter.getItemViewType(itemPosition) == 1
            })
            addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if ((layoutManager as LinearLayoutManager).findLastVisibleItemPosition() == (layoutManager as LinearLayoutManager).itemCount - 1) {
                        if (!isNoMore && !isLoading)
                            querySuspeneded()
                    }
                    super.onScrolled(recyclerView, dx, dy)
                }
            })
        }
    }


    suspend fun inquiryOnlineProcess(
        suspendedQrData: SuspendedQrDataModel,
        isDelete: Boolean? = false
    ) {
        if (SystemParam.appIdOnline.get() == suspendedQrData.appid) {
            ksherPay.UpdateAppId(
                SystemParam.appIdOnline.get(),
                SystemParam.tokenOnline.decrypt()
            )
        } else {
            ksherPay.UpdateAppId(
                SystemParam.appIdOffline.get(),
                SystemParam.tokenOffline.decrypt()
            )
        }
        val response =
            gatewayOrderQuery(suspendedQrData?.mchOrderNo.orEmpty())
        val jsonObject = JSONObject(response)
        val dataObject = jsonObject.getJSONObject("data")
        val result = dataObject.getString("result")
        Timber.d("response $response")
        CoroutineScope(Dispatchers.Main).launch {
            when (result) {
                "SUCCESS" -> {
                    suspendedRepository.updateSuspendedQrWithTraceNo(
                        suspendedQrData.traceNo.toString(),
                        "SUCCESS"
                    )
                    val transData =
                        TransDataModel.initFromSuspendedQr(suspendedQrData)
                    setAmount(transData, dataObject)
                    setTimeToTransData(transData, dataObject)
                    setRefNoToTransData(transData, dataObject)
                    setTransactionId(transData, dataObject)
                    setInvoiceNo(transData, dataObject)
                    setAppId(transData, dataObject)

                    transactionRepository.insertTransaction(transData)
                        .collect {
                            // REMOVE ROW FROM SUSPENDED QR WHEN TRANSACTION SUCCESSFULLY.
                            suspendedRepository.updateSuspendedQrWithTraceNo(
                                traceNo = transData.traceNo.toString(),
                                "SUCCESS"
                            )
                                .collect()
                            SystemParam.incInvoiceNo()

                            ControllerParam.needPrintLastTrans.set(true)
                            Completable.fromAction(ProgressNotifier.getInstance()::show)
                                .andThen(
                                    TransactionPrinting().printAnyTrans(
                                        transData
                                    )
                                )
                                .doOnComplete {
                                    ControllerParam.needPrintLastTrans.set(false)
                                    val bundle = Bundle()
                                    val intent =
                                        Intent(this@SuspendedQrActivity, PrintActivity::class.java)
                                    bundle.putParcelable("transData", transData)
                                    intent.putExtras(bundle)
                                    startActivity(intent)
                                    finish()
                                }
                                .doFinally(ProgressNotifier.getInstance()::dismiss)
                                .doOnError { e ->
                                    ProgressNotifier.getInstance()::dismiss
                                    DeviceUtil.beepErr()
                                    DialogUtils.showAlertTimeout(
                                        e.message,
                                        DialogUtils.TIMEOUT_FAIL
                                    )
                                }.onErrorComplete().subscribe()
                        }
                }
                "FAILURE" -> {
                    ProgressNotifier.getInstance().dismiss()
                    if (isDelete == true) {
                        CoroutineScope(Dispatchers.IO).launch {
                            suspendedRepository.deleteSuspendedQrWithTraceNo(suspendedQrData.traceNo.toString())
                                .collect {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        reloadData()
                                    }
                                }
                        }
                    } else {
                        showDialog(msg = dataObject.getString(
                            "err_msg"
                        ),
                            actionCancel = { startCountDown() },
                            actionConfirm = { startCountDown() })
                    }
                }
                "NOTSURE" -> {
                    ProgressNotifier.getInstance().dismiss()
                    showDialog(msg =
                    "Please contact administrator",
                        actionCancel = { startCountDown() },
                        actionConfirm = { startCountDown() })
                }
                "PENDING" -> {
                    ProgressNotifier.getInstance().dismiss()
                    if (isDelete == true) {
                        CoroutineScope(Dispatchers.IO).launch {
                            suspendedRepository.deleteSuspendedQrWithTraceNo(suspendedQrData.traceNo.toString())
                                .collect {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        reloadData()
                                    }
                                }
                        }
                    } else {
                        showDialog(msg = dataObject.getString(
                            "err_msg"
                        ),
                            actionCancel = { startCountDown() },
                            actionConfirm = { startCountDown() })
                    }
                }
                else -> {
                    ProgressNotifier.getInstance().dismiss()
                    if (isDelete == true) {
                        CoroutineScope(Dispatchers.IO).launch {
                            suspendedRepository.deleteSuspendedQrWithTraceNo(suspendedQrData.traceNo.toString())
                                .collect {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        reloadData()
                                    }
                                }
                        }
                    } else {
                        showDialog(
                            msg = StringUtils.getText(configModel.data?.stringFile?.unknownErrorLabel?.label),
                            actionCancel = { startCountDown() },
                            actionConfirm = { startCountDown() })
                    }
                }
            }
        }
    }

    private fun setAmount(transData: TransDataModel, dataObject: JSONObject) {
        if (dataObject.has("total_fee")) {
            transData.amount = dataObject.getLong("total_fee")
        }
        if (dataObject.has("cash_fee")) {
            transData.amountConvert = dataObject.getLong("cash_fee")
        }
        if (dataObject.has("cash_fee_type")) {
            transData.currencyConvert = dataObject.getString("cash_fee_type")
        }
        if (dataObject.has("rate")) {
            transData.exchangeRate = dataObject.getString("rate")
        }
    }

    private fun reloadData() {
        offset = 0
        limit = 25
        isNoMore = false
        isLoading = true
        historyDataList = arrayListOf()
        recyclerViewAdapter.onUpdateItem(historyDataList)
        querySuspeneded()
    }

    private fun setInvoiceNo(transData: TransDataModel, dataObject: JSONObject) {
        val timeEnd = dataObject.getString("time_end")
        val invoiceNo =
            transData.terminalId + DateUtils.getFormattedDate(
                timeEnd,
                "yyyy-MM-dd HH:mm:ss",
                "YYMMddHHmmss"
            )
        transData.invoiceNo = invoiceNo
    }

    private fun setTransactionId(transData: TransDataModel, dataObject: JSONObject) {
        val transId = if (dataObject.has("channel_order_no")) {
            dataObject.getString("channel_order_no")
        } else {
            transData.mchOrderNo
        }

        transData.transactionId = transId
    }

    private fun setRefNoToTransData(transData: TransDataModel, dataObject: JSONObject) {
        val refNo = dataObject.getString("ksher_order_no")
        transData.referNo = refNo
    }

    private fun setAppId(transData: TransDataModel, dataObject: JSONObject) {
        val appId = dataObject.getString("appid")
        transData.appid = appId
    }


    private fun setTimeToTransData(transData: TransDataModel, dataObject: JSONObject) {
        val timeEnd = dataObject.getString("time_end")
        val date = DateUtils.getFormattedDateTime(timeEnd, "yyyy-MM-dd HH:mm:ss")
        val calendar = Calendar.getInstance()
        calendar.time = date
        transData.date = calendar.getAbbreviatedFromDateTime("MMdd")
        transData.year = calendar.getAbbreviatedFromDateTime("YYYY")
        transData.time = calendar.getAbbreviatedFromDateTime("HHmmss")
    }

    suspend fun gatewayOrderQuery(mchOrderNo: String): String {
        Timber.d("gatewayOrderQuery transdata : $mchOrderNo")
        return ksherPay.OrderQuery(
            mchOrderNo
        )
    }
}
