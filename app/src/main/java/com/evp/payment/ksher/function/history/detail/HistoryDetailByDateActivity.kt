package com.evp.payment.ksher.function.history.detail

import android.app.Activity
import android.content.Intent
import com.evp.payment.ksher.R
import com.evp.payment.ksher.function.main.BaseTimeoutActivity

import android.view.View
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.evp.payment.ksher.database.*
import com.evp.payment.ksher.database.repository.SettlementRepository
import com.evp.payment.ksher.database.repository.TransactionRepository
import com.evp.payment.ksher.database.table.TransDataModel
import com.evp.payment.ksher.extension.*
import com.evp.payment.ksher.function.history.adapter.HistoryByDateItemAdapter
import com.evp.payment.ksher.function.payment.PaymentSelectorActivity
import com.evp.payment.ksher.utils.DateUtils
import com.evp.payment.ksher.utils.HeaderItemDecoration
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.utils.constant.PaymentAction
import com.evp.payment.ksher.view.progressbar.ProgressNotifier
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_history_detail_by_date.*
import kotlinx.android.synthetic.main.header_layout.*

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

@AndroidEntryPoint
class HistoryDetailByDateActivity : BaseTimeoutActivity() {
    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Inject
    lateinit var settlementRepository: SettlementRepository

    private val isInvoke by extra("is_invoke", false)

    var offset = 0
    var limit = 25
    var isNoMore = false
    var isLoading = true

    lateinit var recyclerViewAdapter: HistoryByDateItemAdapter

    private fun gotoHistoryDetail(transData: TransDataModel) {
        if(isInvoke!!){
            HistoryDetailActivity.startWithInvoke(this, transData.mchOrderNo)
        }else {
            startActivityForResult(
                Intent(
                    this@HistoryDetailByDateActivity, HistoryDetailActivity::class.java
                ).apply {
                    putExtra("mchOrderNo", transData.mchOrderNo)
                }, 500
            )
        }
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
        get() = R.layout.activity_history_detail_by_date

    override fun initViews() {
        recyclerViewAdapter =  HistoryByDateItemAdapter(arrayListOf(), true, configModel) {
            gotoHistoryDetail(it)
        }
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
                transactionRepository.getAllTransaction(limit = limit, offset = offset)
                    .collect { it ->
                        val historys = it?.getOrNull()
                        if (historys?.results.isNullOrEmpty()) {
                            ProgressNotifier.getInstance().dismiss()
                            CoroutineScope(Dispatchers.Main).launch {
                                showDialog(
                                    msg = StringUtils.getText(configModel.data?.stringFile?.transactionNotFoundLabel?.label),
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
                this@HistoryDetailByDateActivity.recyclerViewAdapter.getItemViewType(itemPosition) == 1
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
        fun start(context: Activity?) {
            val intent =  Intent(context, HistoryDetailByDateActivity::class.java)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
        }

        @JvmStatic
        fun startWithInvoke(context: Activity?) {
            val intent =  Intent(context, HistoryDetailByDateActivity::class.java)
            intent.putExtra("is_invoke", true)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
            context?.finish()
        }

    }

}
