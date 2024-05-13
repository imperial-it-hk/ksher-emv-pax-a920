package com.evp.payment.ksher.function.history.fragment

import android.app.Activity
import android.content.Context
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
import com.evp.payment.ksher.function.payment.PaymentSelectorActivity
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.utils.constant.PaymentAction


class TransactionHistoryDetailFragment(private val configModel: ConfigModel, private val isInvoke: Boolean) : Fragment() {
    private lateinit var binding: FragmentDetailHistoryBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDetailHistoryBinding.inflate(layoutInflater)
        init()
        return binding.root
    }

    fun init(){
        binding.btDetailByDate.text = StringUtils.getText(configModel.data?.button?.buttonDetailByDate?.label)
        binding.btDetailByPayment.text = StringUtils.getText(configModel.data?.button?.buttonDetailPayment?.label)

        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        shape.setColor(Color.parseColor(configModel.data?.setting?.colorPrimary))
        shape.setStroke(6, Color.parseColor(configModel.data?.setting?.colorPrimary))
        shape.cornerRadii = floatArrayOf(6f, 6f, 6f, 6f, 6f, 6f, 6f, 6f)
        binding.btDetailByDate.background = shape
        binding.btDetailByPayment.background = shape

        binding.btDetailByDate.setOnClickListener {
            if(isInvoke){
                HistoryDetailByDateActivity.startWithInvoke(context as Activity)

            }else{
                HistoryDetailByDateActivity.start(context as Activity)
            }
        }

        binding.btDetailByPayment.setOnClickListener {
            if(isInvoke){
                PaymentSelectorActivity.startWithInvoke(context as Activity, PaymentAction.HISTORY_DETAIL_BY_PAYMENT_TYPE, false, StringUtils.getText(configModel.data?.button?.buttonDetailPayment?.label), "")

            }else{
                PaymentSelectorActivity.start(context as Activity, PaymentAction.HISTORY_DETAIL_BY_PAYMENT_TYPE, false, StringUtils.getText(configModel.data?.button?.buttonDetailPayment?.label))

            }
        }
    }
}