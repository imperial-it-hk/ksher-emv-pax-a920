package com.evp.payment.ksher.function.payment.action

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import butterknife.BindView
import butterknife.OnClick
import com.evp.payment.ksher.R
import com.evp.payment.ksher.config.ConfigModel
import com.evp.payment.ksher.extension.extra
import com.evp.payment.ksher.extension.extraNotNull
import com.evp.payment.ksher.function.BaseApplication
import com.evp.payment.ksher.function.main.BaseTimeoutActivity
import com.evp.payment.ksher.function.payment.PaymentSelectorActivity
import com.evp.payment.ksher.function.qr.GeneratorQRActivity
import com.evp.payment.ksher.function.qr.ScanCodeActivity
import com.evp.payment.ksher.function.qr.ScanCodeRequestEvent
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.utils.constant.InvokeConstant
import com.evp.payment.ksher.utils.constant.PaymentAction
import com.evp.payment.ksher.utils.keyboard.KeyboardAmountUtil
import com.pax.unifiedsdk.message.BaseRequest
import com.pax.unifiedsdk.message.BaseResponse
import com.pax.unifiedsdk.message.MessageUtils
import kotlinx.android.synthetic.main.activity_input_amount.*

//import com.pax.unifiedsdk.message.MessageUtils


class InputAmountActivity : BaseTimeoutActivity(), KeyboardAmountUtil.KeyboardAmountListener {

    @BindView(R.id.tv_amount_prompt)
    lateinit var tvAmountPrompt: TextView

    @BindView(R.id.tv_abbr)
    lateinit var tvAbbr: TextView

    @BindView(R.id.tv_amount)
    lateinit var tvAmount: TextView

    @BindView(R.id.keyboard)
    lateinit var keyboard: ConstraintLayout

    @BindView(R.id.iv_enter)
    lateinit var ivEnter: ImageView

    @BindView(R.id.key_ok)
    lateinit var keyOk: AppCompatButton

    var keyboardAmountUtil = KeyboardAmountUtil()

    private lateinit var ACTION_METHOD_TYPE: String

    private val payChannel by extra("pay_channel", "")
    private val payAmount by extra("pay_amount", "")
    private val mediaType by extra("media_type", "")

    override fun loadParam() {
    }

    override val layoutId: Int
        get() = R.layout.activity_input_amount

    override fun initViews() {
        keyboardAmountUtil.init(keyboard, this)
        setThemePrimaryColor(v_bg)
        setThemePrimaryColor(keyOk)
        tv_abbr.text = configModel.data?.config?.defaultCurrencyUnit ?: "THB"
        if (intent != null) {
            if (intent.hasExtra(PaymentAction.KEY_ACTION)) {
                ACTION_METHOD_TYPE = intent.getStringExtra(PaymentAction.KEY_ACTION)!!
            }

            when (ACTION_METHOD_TYPE) {
                PaymentAction.SCAN -> {
                    keyOk.text = StringUtils.getText(configModel.data?.button?.buttonOk?.label)
                    keyOk.visibility = View.VISIBLE
                }
                PaymentAction.SALE -> {
                    ivEnter.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun updateAmount(displayAmount: String?) {
        tvAmount.text = "$displayAmount"
    }

    override fun confirm() {
        when (ACTION_METHOD_TYPE) {
            PaymentAction.SCAN -> {
                val intent = Intent(this, PaymentSelectorActivity::class.java)
                intent.putExtra("show_input_intent", false)
                intent.putExtra("pay_amount", tvAmount.text.toString().replace("฿", " ").trim())
                intent.putExtra(PaymentAction.KEY_ACTION, PaymentAction.SCAN)
                startActivity(intent)
                finish()
            }
            PaymentAction.SALE -> {
                if (payChannel.isNullOrEmpty()) {
                    val intent = Intent(this, PaymentSelectorActivity::class.java)
                    intent.putExtra("show_input_intent", true)
                    intent.putExtra("pay_amount", tvAmount.text.toString().replace("฿", " ").trim())
                    intent.putExtra(PaymentAction.KEY_ACTION, PaymentAction.GENERATOR_QR)
                    startActivity(intent)
                    finish()
                } else {
                    GeneratorQRActivity.startWithInvoke(
                        this,
                        StringUtils.toDisplayAmount(tvAmount.text.toString().replace("฿", " ").trim(), 2),
                        payChannel,
                        InvokeConstant.MEDIA_TYPE_C2B
                    )
                }
            }
        }
    }

    @OnClick(R.id.layout_back)
    fun onBackClick() {
        onBackPressed()
    }

    companion object {

        @JvmStatic
        fun start(
            context: Activity?, keyAction: String) {
            val intent = Intent(context, InputAmountActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
            intent.putExtra(PaymentAction.KEY_ACTION, keyAction)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
        }

        @JvmStatic
        fun startWithInvoke(
            context: Activity?, amount: String, channel: String?, mediaType: String
        ) {
            val intent = Intent(context, InputAmountActivity::class.java)
            intent.putExtra("pay_amount", StringUtils.toDisplayAmount(amount, 2))
            intent.putExtra("pay_channel", channel)
            intent.putExtra("media_type", mediaType)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra(PaymentAction.KEY_ACTION, PaymentAction.SALE)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
            context?.finish()
        }

    }

}