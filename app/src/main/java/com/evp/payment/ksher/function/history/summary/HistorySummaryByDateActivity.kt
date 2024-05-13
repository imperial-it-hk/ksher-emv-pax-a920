package com.evp.payment.ksher.function.history.summary

import android.app.Activity
import android.content.Intent
import com.evp.payment.ksher.R
import com.evp.payment.ksher.function.main.BaseTimeoutActivity

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import butterknife.BindView
import com.evp.payment.ksher.database.*
import com.evp.payment.ksher.database.repository.SettlementRepository
import com.evp.payment.ksher.database.repository.TransactionRepository
import com.evp.payment.ksher.database.table.SettlementDataModel
import com.evp.payment.ksher.database.table.TransDataModel
import com.evp.payment.ksher.extension.*
import com.evp.payment.ksher.function.history.detail.HistoryDetailByDateActivity
import com.evp.payment.ksher.parameter.ControllerParam
import com.evp.payment.ksher.parameter.MerchantParam
import com.evp.payment.ksher.parameter.SystemParam
import com.evp.payment.ksher.parameter.TerminalParam
import com.evp.payment.ksher.printing.TransactionPrinting
import com.evp.payment.ksher.utils.DateUtils
import com.evp.payment.ksher.utils.DeviceUtil
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.view.dialog.DialogUtils
import com.evp.payment.ksher.view.progressbar.ProgressNotifier
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Completable
import kotlinx.android.synthetic.main.activity_history_summary_by_date.*
import kotlinx.android.synthetic.main.header_layout.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HistorySummaryByDateActivity : BaseTimeoutActivity() {

    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Inject
    lateinit var settlementRepository: SettlementRepository

    val paymentChannelList =
        listOf("linepay", "promptpay", "truemoney", "alipay", "wechat", "airpay")
    var settlementModel = SettlementModel()
    var grandTotalAmount = SettlementItemModel()

    @BindView(R.id.layout_sub_header)
    lateinit var layoutSubHeader: View

    @BindView(R.id.tv_sub_header_detail)
    lateinit var tvSubHeaderTitle: TextView

    override fun loadParam() {

    }

    override val layoutId: Int
        get() = R.layout.activity_history_summary_by_date

    override fun initViews() {
        layoutSubHeader.visible()
        tvSubHeaderTitle.visible()
        tvSubHeaderTitle.text = StringUtils.getText(configModel.data?.stringFile?.transactionHistoryLabel?.label)
        layout_back.setOnClickListener { onBackPressed() }


        tvDate.text = DateUtils.getCurrentTime("MMM dd, yyyy")
        tvTime.text = DateUtils.getCurrentTime("HH:mm")
        queryHistory()
    }

    private fun queryHistory() {
        CoroutineScope(Dispatchers.IO).launch {
            ProgressNotifier.getInstance().show()
            ProgressNotifier.getInstance().primaryContent(StringUtils.getText(configModel.data?.stringFile?.queryHistoryLabel?.label))
            transactionRepository.queryHistorySummaryByDate().collect {
                val historys = it?.getOrNull()
                historys?.let {
                    tvSale.text = "THB  ${(it.saleTotalAmount + it.voidTotalAmount).toAmount2DigitDisplay()}"
                    tvVoid.text = "THB  ${(it.voidTotalAmount).toAmount2DigitDisplay()}"
                    tvNetSalses.text = "THB  ${(it.saleTotalAmount).toAmount2DigitDisplay()}"
                }

                ProgressNotifier.getInstance().dismiss()
            }
        }
    }

    companion object {
        @JvmStatic
        fun start(context: Activity?) {
            val intent =  Intent(context, HistorySummaryByDateActivity::class.java)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
        }

        @JvmStatic
        fun startWithInvoke(context: Activity?) {
            val intent =  Intent(context, HistorySummaryByDateActivity::class.java)
            intent.putExtra("is_invoke", true)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
            context?.finish()
        }

    }
}
