package com.evp.payment.ksher.function.inquiry.suspendedqr

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.evp.payment.ksher.R
import com.evp.payment.ksher.config.ConfigModel
import com.evp.payment.ksher.database.SuspendedQrData
import com.evp.payment.ksher.database.SuspendedQrHeader
import com.evp.payment.ksher.database.table.SuspendedQrDataModel
import com.evp.payment.ksher.extension.*
import com.evp.payment.ksher.utils.DateUtils
import com.evp.payment.ksher.utils.StringUtils
import kotlinx.android.synthetic.main.item_history_header_date.view.tvMonthAndYear
import kotlinx.android.synthetic.main.item_history_summary_by_date.view.*
import java.util.*

class SuspenededQrItemAdapter(
    val historyItemList: ArrayList<SuspendedQrData>,
    val isShowChannel: Boolean = true,
    val configModel: ConfigModel,
    private val onItemSelect: (SuspendedQrDataModel) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 1) {
            val headerView = LayoutInflater.from(parent.context).inflate(
                R.layout.item_history_header_date,
                parent, false
            )
            return SuspendedQrHeaderViewHolder(headerView)
        } else {
            val transactionView = LayoutInflater.from(parent.context).inflate(
                R.layout.item_history_summary_by_date,
                parent, false
            )
            return GoldHistoryItemViewHolder(transactionView, configModel)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (historyItemList[position]) {
            is SuspendedQrHeader -> {
                1
            }
            else -> {
                2
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SuspendedQrHeaderViewHolder -> {
                val item = historyItemList[position] as SuspendedQrHeader
                holder.setViewData(item)
            }
            else -> {
                holder as GoldHistoryItemViewHolder
                val item = historyItemList[position]
                holder.setViewData(
                    item as SuspendedQrDataModel,
                    isShowMonthAndYearTitle(position),
                    isShowDivider(position),
                    isShowChannel = isShowChannel ?: true
                )
                holder.itemView.clHistoryItem.setOnClickListener {
                    onItemSelect(item)
                }
            }
        }
    }

    override fun getItemCount() = historyItemList.size

    fun onUpdateItem(list: List<SuspendedQrData>) {
        historyItemList.clear()
        historyItemList.addAll(list)
        notifyDataSetChanged()
    }

    private fun isShowMonthAndYearTitle(position: Int): Boolean {
        return false
    }

    private fun isShowDivider(position: Int): Boolean {
        return historyItemList.getOrNull(position + 1) is SuspendedQrDataModel
    }
}

class GoldHistoryItemViewHolder(private val view: View, private val configModel: ConfigModel) : RecyclerView.ViewHolder(view) {

    fun setViewData(
        item: SuspendedQrDataModel,
        isShowMonthAndYearTitle: Boolean,
        isShowDivider: Boolean,
        isShowChannel: Boolean
    ) {
        val context = view.context
        if (isShowChannel) {
            view.tvProviderName.visible()
//            view.ivProviderImage.visible()
            view.tvProviderName.text = item.paymentChannel?.toPaymentChannelDisplay()

            for (payment in configModel.data?.menusMain?.get(0)?.payment.orEmpty()){
                if(item.paymentChannel.equals(payment?.paymentType, true)){
                    view.ivProviderImage.visible()
                    view.ivProviderImage.setImageBitmap(StringUtils.getImage(payment?.icon))
                }
            }

//            when {
//                "linepay".equals(item.paymentChannel, true) -> {
//                    view.ivProviderImage.setImageResource(R.drawable.ic_payment_linepay)
//                }
//                "promptpay".equals(item.paymentChannel, true) -> {
//                    view.ivProviderImage.setImageResource(R.drawable.ic_payment_promptpay)
//                }
//                "alipay".equals(item.paymentChannel, true) -> {
//                    view.ivProviderImage.setImageResource(R.drawable.ic_payment_alipay)
//                }
//                "wechat".equals(item.paymentChannel, true) -> {
//                    view.ivProviderImage.setImageResource(R.drawable.ic_payment_wechat)
//                }
//                "truemoney".equals(item.paymentChannel, true) -> {
//                    view.ivProviderImage.setImageResource(R.drawable.ic_payment_true_money)
//                }
//                "airpay".equals(item.paymentChannel, true) -> {
//                    view.ivProviderImage.setImageResource(R.drawable.ic_payment_shopee)
//                }
//                else -> view.ivProviderImage.gone()
//            }
        } else {
            view.tvProviderName.gone()
            view.ivProviderImage.gone()
        }


        view.tvTransactionDate.text = DateUtils.getFormattedDate(
            item.year + item.date + item.time,
            "yyyyMMddHHmmss",
            "MMM dd, yy HH:mm:ss"
        )

        view.tvTransType.setTextColor(Color.parseColor("#ff1100"))
        view.tvTransType.text = StringUtils.getText(configModel.data?.stringFile?.suspendedLabel?.label)
        view.tvAmount.text = item.amount?.toAmount2DigitDisplay()

        if (isShowDivider) {
            view.divider.visible()
        } else {
            view.divider.invisible()
        }
    }

}

class SuspendedQrHeaderViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    fun setViewData(item: SuspendedQrHeader) {
        view.tvMonthAndYear.text = item.header
    }
}