package com.evp.payment.ksher.function.history.detail

import android.app.Activity
import android.content.Intent
import com.evp.payment.ksher.R
import com.evp.payment.ksher.function.main.BaseTimeoutActivity

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.evp.payment.ksher.database.*
import com.evp.payment.ksher.database.repository.SettlementRepository
import com.evp.payment.ksher.database.repository.TransactionRepository
import com.evp.payment.ksher.database.table.TransDataModel
import com.evp.payment.ksher.extension.*
import com.evp.payment.ksher.function.history.adapter.HistoryByDateItemAdapter
import com.evp.payment.ksher.utils.DateUtils
import com.evp.payment.ksher.utils.HeaderItemDecoration
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.view.progressbar.ProgressNotifier
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_history_detail_by_channel.*
import kotlinx.android.synthetic.main.header_layout.*

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.lang.Exception
import javax.inject.Inject

@AndroidEntryPoint
class HistoryDetailByChannelActivity : BaseTimeoutActivity() {
    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Inject
    lateinit var settlementRepository: SettlementRepository

    var offset = 0
    var limit = 25
    var isNoMore = false
    var isLoading = true

    private val payChannel by extraNotNull<String>("pay_channel")
    lateinit var recyclerViewAdapter: HistoryByDateItemAdapter

    private fun gotoHistoryDetail(transData: TransDataModel) {
        startActivityForResult(
            Intent(
                this@HistoryDetailByChannelActivity,
                HistoryDetailActivity::class.java
            ).apply {
                putExtra("mchOrderNo", transData.mchOrderNo)
            }, 500
        )
//        try {
//            CoroutineScope(Dispatchers.IO).launch {
//                isLoading = true
//                ProgressNotifier.getInstance().show()
//                ProgressNotifier.getInstance().primaryContent("Check transaction...")
//                transactionRepository.getTransactionDetailLatest(transData.mchOrderNo!!)
//                    .collect { transData ->
//                        ProgressNotifier.getInstance().dismiss()
//                        startActivity(
//                            Intent(
//                                this@HistoryDetailByDateActivity,
//                                HistoryDetailActivity::class.java
//                            ).apply {
//                                putExtra("transData", transData)
//                            })
//                    }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 500) {
            if (resultCode == Activity.RESULT_OK) {
                offset = 0
                limit = 25
                isNoMore = false
                isLoading = true
                historyDataList = arrayListOf()
                queryHistory()
            }
        }
    }


    var historyDataList: ArrayList<HistoryData> = arrayListOf()

    @BindView(R.id.layout_sub_header)
    lateinit var layoutSubHeader: View

    @BindView(R.id.tv_sub_header_detail)
    lateinit var tvSubHeaderTitle: TextView

    override fun loadParam() {

    }

    override val layoutId: Int
        get() = R.layout.activity_history_detail_by_channel

    override fun initViews() {
        recyclerViewAdapter = HistoryByDateItemAdapter(arrayListOf(), false, configModel) {
            gotoHistoryDetail(it)
        }
        tvProviderName.text = payChannel.toPaymentChannelDisplay()
        setupSubHeaderLogo(ivProviderImage, payChannel)
        layoutSubHeader.visible()
        tvSubHeaderTitle.visible()
        tvSubHeaderTitle.text = StringUtils.getText(configModel.data?.stringFile?.transactionHistoryLabel?.label)
        layout_back.setOnClickListener { onBackPressed() }

        setupRecyclerView()
        queryHistory()
    }

    private fun queryHistory() {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                isLoading = true
                ProgressNotifier.getInstance().show()
                ProgressNotifier.getInstance().primaryContent(StringUtils.getText(configModel.data?.stringFile?.queryHistoryLabel?.label))
                transactionRepository.getAllTransactionWithChannel(
                    channel = payChannel,
                    limit = limit,
                    offset = offset
                )
                    .collect { it ->
                        val historys = it?.getOrNull()
                        if (historys?.results.isNullOrEmpty()) {
                            ProgressNotifier.getInstance().dismiss()
                            CoroutineScope(Dispatchers.Main).launch {
                                showDialog(
                                    msg = StringUtils.getText(configModel.data?.stringFile?.noTransactionFoundLabel?.label) +" "+payChannel.toPaymentChannelDisplay()+".",
                                    actionCancel = { finish() },
                                    actionConfirm = { finish() })
                            }
                            return@collect
                        }
                        val size = historys?.results?.size ?: 0
                        if (size < limit)
                            isNoMore = true
                        offset += size

                        historys?.results?.let {
                            historyDataList.addAll(getHistoryListWithMonthAndYearHeader(it))
                            CoroutineScope(Dispatchers.Main).launch {
                                recyclerViewAdapter.onUpdateItem(historyDataList)
                                ProgressNotifier.getInstance().dismiss()
                                isLoading = false
                            }
                        }
                    }
            }
        } catch (e: Exception) {
            ProgressNotifier.getInstance().dismiss()
            e.printStackTrace()
        }
    }

    fun getHistoryListWithMonthAndYearHeader(list: List<TransDataModel>): List<HistoryData> {
        val a: ArrayList<HistoryData> = arrayListOf()
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
                    HistoryHeader(
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
                this@HistoryDetailByChannelActivity.recyclerViewAdapter.getItemViewType(itemPosition) == 1
            })
            addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if ((layoutManager as LinearLayoutManager).findLastVisibleItemPosition() == (layoutManager as LinearLayoutManager).itemCount - 1) {
                        if (!isNoMore && !isLoading)
                            queryHistory()
                    }
                    super.onScrolled(recyclerView, dx, dy)
                }
            })
        }
    }

    companion object {
        @JvmStatic
        fun start(context: Activity?, channel: String?) {
            val intent =  Intent(context, HistoryDetailByChannelActivity::class.java)
            intent.putExtra("pay_channel", channel)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
            context?.finish()
        }

        @JvmStatic
        fun startWithInvoke(context: Activity?, channel: String?) {
            val intent =  Intent(context, HistoryDetailByChannelActivity::class.java)
            intent.putExtra("is_invoke", true)
            intent.putExtra("pay_channel", channel)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
            context?.finish()
        }

    }
}
