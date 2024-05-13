package com.evp.payment.ksher.function.history.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.evp.payment.ksher.config.ConfigModel
import com.evp.payment.ksher.databinding.FragmentDetailHistoryBinding
import com.evp.payment.ksher.function.history.detail.HistoryDetailByDateActivity
import com.evp.payment.ksher.function.history.summary.HistorySummaryByChannelActivity
import com.evp.payment.ksher.function.history.summary.HistorySummaryByDateActivity
import com.evp.payment.ksher.utils.StringUtils


class TransactionHistorySummaryFragment(private val configModel: ConfigModel, private val isInvoke: Boolean) : Fragment() {
    private lateinit var binding: FragmentDetailHistoryBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDetailHistoryBinding.inflate(layoutInflater)
        init()
        return binding.root
    }

    fun init(){
        binding.btDetailByDate.text = StringUtils.getText(configModel.data?.button?.buttonSummaryByDate?.label)
        binding.btDetailByPayment.text = StringUtils.getText(configModel.data?.button?.buttonSummaryPayment?.label)

        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        shape.setColor(Color.parseColor(configModel.data?.setting?.colorPrimary))
        shape.setStroke(6, Color.parseColor(configModel.data?.setting?.colorPrimary))
        shape.cornerRadii = floatArrayOf(6f, 6f, 6f, 6f, 6f, 6f, 6f, 6f)
        binding.btDetailByDate.background = shape
        binding.btDetailByPayment.background = shape

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btDetailByDate.setOnClickListener {
            if(isInvoke){
                HistorySummaryByDateActivity.startWithInvoke(context as Activity)

            }else{
                HistorySummaryByDateActivity.start(context as Activity)
            }
        }

        binding.btDetailByPayment.setOnClickListener {
            if(isInvoke){
                HistorySummaryByChannelActivity.startWithInvoke(context as Activity)

            }else{
                HistorySummaryByChannelActivity.start(context as Activity)
            }
        }
    }
}