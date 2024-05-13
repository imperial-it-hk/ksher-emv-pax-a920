package com.evp.payment.ksher.view.progressbar

import android.app.Dialog
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.content.res.AppCompatResources
import com.evp.payment.ksher.R
import com.evp.payment.ksher.databinding.DialogProgressBarBinding
import com.evp.payment.ksher.extension.gone
import com.evp.payment.ksher.extension.invisible
import com.evp.payment.ksher.extension.visible
import com.evp.payment.ksher.function.BaseApplication.Companion.appContext
import com.evp.payment.ksher.parameter.SystemParam

internal class ProgressBarDialog : Dialog(appContext!!, R.style.DialogStyle) {

    private lateinit var binding: DialogProgressBarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogProgressBarBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    fun show(progressBarParameter: ProgressBarParameter) {
        if (!isShowing) {
            show()
        }
        setProgressWheelVisible(progressBarParameter.isHideProgress)
        setCountDown(progressBarParameter.timeout)
        setPrimaryContent(progressBarParameter.primaryContent)
        setSubContent(progressBarParameter.subContent)
        when {
            progressBarParameter.isApproved -> showStatusContent(
                AppCompatResources.getDrawable(context, R.drawable.ic_approve)!!,
                progressBarParameter.primaryContent
            )
            progressBarParameter.isDeclined -> showStatusContent(
                AppCompatResources.getDrawable(context, R.drawable.ic_declined)!!,
                progressBarParameter.primaryContent
            )
            else -> {
                binding.progressWheel.visible()
                binding.ivStatus.invisible()
            }
        }
    }

    private fun setProgressWheelVisible(hide: Boolean) {
        if (hide) {
            binding.progressWheel.gone()
        } else {
            binding.progressWheel.visible()
            val progressHelper = ProgressHelper(context)
            progressHelper.progressWheel = binding.progressWheel
        }
    }


    private fun setCountDown(countDown: Long) {
        binding.tvCountDown.text = if (countDown < 0) "" else countDown.toString() + "s"
    }

    private fun setPrimaryContent(primaryContent: String) {
        binding.tvPrimaryContent.text = primaryContent
    }

    private fun setSubContent(subContent: String?) {
        binding.tvSubContent.text = subContent
    }

    private fun showStatusContent(drawable: Drawable, primaryContent: String) {
        binding.progressWheel.invisible()
        binding.ivStatus.visible()
        binding.ivStatus.setImageDrawable(drawable)
        binding.tvPrimaryContent.text = primaryContent
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        } else {
            window?.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
        }
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }
}