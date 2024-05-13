package com.evp.payment.ksher.function.history.summary

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import com.evp.payment.ksher.R
import com.evp.payment.ksher.function.main.BaseTimeoutActivity

import android.view.View
import android.widget.TextView
import butterknife.BindView
import com.evp.payment.ksher.database.*
import com.evp.payment.ksher.database.repository.SettlementRepository
import com.evp.payment.ksher.database.repository.TransactionRepository
import com.evp.payment.ksher.extension.*
import com.evp.payment.ksher.utils.DateUtils
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.view.progressbar.ProgressNotifier
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_history_summary_by_channel.*
import kotlinx.android.synthetic.main.header_layout.*
import kotlinx.android.synthetic.main.item_history_summary_by_channel.view.*

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

@AndroidEntryPoint
class HistorySummaryByChannelActivity : BaseTimeoutActivity() {

    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Inject
    lateinit var settlementRepository: SettlementRepository

    val paymentChannelList =
        listOf("linepay", "promptpay", "truemoney", "alipay", "wechat", "airpay")
    var historySummaryByChannelList: ArrayList<HistorySummaryByChannelModel> = arrayListOf()

    @BindView(R.id.layout_sub_header)
    lateinit var layoutSubHeader: View

    @BindView(R.id.tv_sub_header_detail)
    lateinit var tvSubHeaderTitle: TextView

    override fun loadParam() {

    }

    override val layoutId: Int
        get() = R.layout.activity_history_summary_by_channel

    override fun initViews() {
        layoutSubHeader.visible()
        tvSubHeaderTitle.visible()
        tvSubHeaderTitle.text = StringUtils.getText(configModel.data?.stringFile?.transactionHistoryLabel?.label)
        layout_back.setOnClickListener { onBackPressed() }


        tvDateTime.text = DateUtils.getCurrentTime("MMM dd, YYYY  HH:mm")
        queryHistory()
    }

    private fun queryHistory() {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                ProgressNotifier.getInstance().show()
                ProgressNotifier.getInstance().primaryContent(StringUtils.getText(configModel.data?.stringFile?.queryHistoryLabel?.label))
                paymentChannelList.forEach {
                    Timber.d(it)
                    transactionRepository.queryHistorySummaryByChannel(it).collect {
                        val historys = it?.getOrNull()
                        historys?.let {
                            historySummaryByChannelList.add(it)
                        }
                    }
                }
                CoroutineScope(Dispatchers.Main).launch {
                    if (layoutViews.childCount > 0)
                        layoutViews.removeAllViews()
                }
                historySummaryByChannelList.forEachIndexed { i, it ->
                    val view = LayoutInflater.from(this@HistorySummaryByChannelActivity)
                        .inflate(R.layout.item_history_summary_by_channel, null)
                    setupSubHeaderLogo(
                        view.ivPaymentChannel,
                        it.paymentChannel ?: paymentChannelList[i]
                    )
                    view.tvPaymentChannel.text =
                        (it.paymentChannel ?: paymentChannelList[i]).toPaymentChannelDisplay()
                    view.tvSale.text =
                        "THB  ${(it.saleTotalAmount + it.voidTotalAmount).toAmount2DigitDisplay()}"
                    view.tvVoid.text = "THB  ${(it.voidTotalAmount).toAmount2DigitDisplay()}"
                    view.tvNetSalses.text = "THB  ${(it.saleTotalAmount).toAmount2DigitDisplay()}"
                    CoroutineScope(Dispatchers.Main).launch {
                        layoutViews.addView(view)
                    }
                }

                ProgressNotifier.getInstance().dismiss()
            }
        } catch (e: Exception) {
            ProgressNotifier.getInstance().dismiss()
            e.printStackTrace()
        }
    }

    companion object {
        @JvmStatic
        fun start(context: Activity?) {
            val intent =  Intent(context, HistorySummaryByChannelActivity::class.java)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
        }

        @JvmStatic
        fun startWithInvoke(context: Activity?) {
            val intent =  Intent(context, HistorySummaryByChannelActivity::class.java)
            intent.putExtra("is_invoke", true)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
            context?.finish()
        }

    }
}
