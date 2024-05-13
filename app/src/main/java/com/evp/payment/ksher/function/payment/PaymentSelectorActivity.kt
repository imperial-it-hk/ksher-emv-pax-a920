package com.evp.payment.ksher.function.payment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import butterknife.BindView
import butterknife.OnClick
import com.evp.payment.ksher.R
import com.evp.payment.ksher.customview.gridmenu.GridMenu
import com.evp.payment.ksher.extension.extra
import com.evp.payment.ksher.extension.extraNotNull
import com.evp.payment.ksher.function.BaseApplication
import com.evp.payment.ksher.function.history.detail.HistoryDetailByChannelActivity
import com.evp.payment.ksher.function.main.BaseTimeoutActivity
import com.evp.payment.ksher.function.main.InputPasswordDialog
import com.evp.payment.ksher.function.main.PaymentActionTypeActivity
import com.evp.payment.ksher.function.payment.view.PaymentSelectorContact
import com.evp.payment.ksher.function.qr.GeneratorQRActivity
import com.evp.payment.ksher.function.qr.ScanCodeActivity
import com.evp.payment.ksher.function.qr.ScanCodeRequestEvent
import com.evp.payment.ksher.function.report.ReportAuditActivity
import com.evp.payment.ksher.function.report.ReportSummaryActivity
import com.evp.payment.ksher.function.voided.InputTransactionActivity
import com.evp.payment.ksher.utils.DeviceUtil
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.utils.constant.InvokeConstant
import com.evp.payment.ksher.utils.constant.PasswordType
import com.evp.payment.ksher.utils.constant.PaymentAction

class PaymentSelectorActivity : BaseTimeoutActivity(), PaymentSelectorContact.View {

    @BindView(R.id.layout_menu)
    lateinit var layoutMenu: LinearLayout

    @BindView(R.id.layout_sub_header)
    lateinit var layoutSubHeader: LinearLayout

    @BindView(R.id.tv_sub_header_detail)
    lateinit var txtHeaderDetail: TextView

    @BindView(R.id.iv_sub_header_channel)
    lateinit var ivBannerHeader: AppCompatImageView

    @BindView(R.id.tv_sub_header_amount)
    lateinit var ivSubAmount: AppCompatTextView

    @BindView(R.id.tv_sub_header_title)
    lateinit var tvHeaderTitle: AppCompatTextView

    private var isCustomerScan = false

    private lateinit var ACTION_METHOD_TYPE: String
    private val payAmount by extraNotNull<String>("pay_amount")
    private val payTitle by extra("pay_title", "")
    private val payChannel by extra("pay_channel", "")
    override fun initViews() {
        if (intent != null) {
            isCustomerScan = intent.getBooleanExtra("show_input_intent", false)
            if (isCustomerScan) {
                showAmountFromCustomerScan()
                ivSubAmount.text = "${configModel.data?.config?.defaultCurrencyUnit ?: "THB"}  $payAmount"
            } else {
                hideAmountFromBusinessScan()
            }

            if (intent.hasExtra(PaymentAction.KEY_ACTION)) {
                ACTION_METHOD_TYPE = intent.getStringExtra(PaymentAction.KEY_ACTION)!!

            }

        }

        if(!payChannel.isNullOrEmpty()){
            startNextActivity(payChannel)
        }else {
            initMenu()
        }
    }

    override fun loadParam() {
    }

    override val layoutId: Int
        get() = R.layout.activity_payment_selector

    override fun onResume() {
        super.onResume()
        // Disable status bar, home, recent key
        DeviceUtil.setDeviceStatus()
    }

    @OnClick(R.id.layout_back)
    fun onBackClick() {
        onBackPressed()
    }

    override fun initMenu() {
        when (ACTION_METHOD_TYPE) {
            PaymentAction.SCAN -> {
                val menuView: View = GridMenu.Builder(applicationContext)
                    .addAction(configModel.data?.menusMain?.get(0)?.payment?.get(0)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(0)?.payment?.get(0)?.icon))  { linePayFunction() }
                    .addAction(configModel.data?.menusMain?.get(0)?.payment?.get(1)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(0)?.payment?.get(1)?.icon)) { promptPayFunction() }
                    .addAction(configModel.data?.menusMain?.get(0)?.payment?.get(2)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(0)?.payment?.get(2)?.icon)) { aliPayFunction() }
                    .addAction(configModel.data?.menusMain?.get(0)?.payment?.get(3)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(0)?.payment?.get(3)?.icon)) { wechatPayFunction() }
                    .addAction(configModel.data?.menusMain?.get(0)?.payment?.get(4)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(0)?.payment?.get(4)?.icon)) { trueMoneyFunction() }
                    .addAction(configModel.data?.menusMain?.get(0)?.payment?.get(5)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(0)?.payment?.get(5)?.icon)) { shopeeFunction() }.create()
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
                )
                layoutMenu.addView(menuView, params)

            }
            PaymentAction.VOID -> {
                val menuView: View = GridMenu.Builder(applicationContext)
                    .addAction(configModel.data?.menusMain?.get(1)?.payment?.get(0)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(1)?.payment?.get(0)?.icon))  { linePayFunction() }
                    .addAction(configModel.data?.menusMain?.get(1)?.payment?.get(1)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(1)?.payment?.get(1)?.icon)) { promptPayFunction() }
                    .addAction(configModel.data?.menusMain?.get(1)?.payment?.get(2)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(1)?.payment?.get(2)?.icon)) { aliPayFunction() }
                    .addAction(configModel.data?.menusMain?.get(1)?.payment?.get(3)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(1)?.payment?.get(3)?.icon)) { wechatPayFunction() }
                    .addAction(configModel.data?.menusMain?.get(1)?.payment?.get(4)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(1)?.payment?.get(4)?.icon)) { trueMoneyFunction() }
                    .addAction(configModel.data?.menusMain?.get(1)?.payment?.get(5)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(1)?.payment?.get(5)?.icon)) { shopeeFunction() }.create()
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
                )
                layoutMenu.addView(menuView, params)
            }
            PaymentAction.PRINT -> {
                val menuView: View = GridMenu.Builder(applicationContext)
                    .addAction(configModel.data?.menusMain?.get(4)?.payment?.get(0)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(4)?.payment?.get(0)?.icon))  { linePayFunction() }
                    .addAction(configModel.data?.menusMain?.get(4)?.payment?.get(1)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(4)?.payment?.get(1)?.icon)) { promptPayFunction() }
                    .addAction(configModel.data?.menusMain?.get(4)?.payment?.get(2)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(4)?.payment?.get(2)?.icon)) { aliPayFunction() }
                    .addAction(configModel.data?.menusMain?.get(4)?.payment?.get(3)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(4)?.payment?.get(3)?.icon)) { wechatPayFunction() }
                    .addAction(configModel.data?.menusMain?.get(4)?.payment?.get(4)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(4)?.payment?.get(4)?.icon)) { trueMoneyFunction() }
                    .addAction(configModel.data?.menusMain?.get(4)?.payment?.get(5)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(4)?.payment?.get(5)?.icon)) { shopeeFunction() }.create()
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
                )
                layoutMenu.addView(menuView, params)
            }
            PaymentAction.GENERATOR_QR -> {
                val menuView: View = GridMenu.Builder(applicationContext)
                    .addAction(configModel.data?.menusSale?.payment?.get(0)?.display, "",StringUtils.getImage(configModel.data?.menusSale?.payment?.get(0)?.icon))  { linePayFunction() }
                    .addAction(configModel.data?.menusSale?.payment?.get(1)?.display, "",StringUtils.getImage(configModel.data?.menusSale?.payment?.get(1)?.icon)) { promptPayFunction() }
                    .addAction(configModel.data?.menusSale?.payment?.get(2)?.display, "",StringUtils.getImage(configModel.data?.menusSale?.payment?.get(2)?.icon)) { aliPayFunction() }
                    .addAction(configModel.data?.menusSale?.payment?.get(3)?.display, "",StringUtils.getImage(configModel.data?.menusSale?.payment?.get(3)?.icon)) { wechatPayFunction() }
                    .addAction(configModel.data?.menusSale?.payment?.get(4)?.display, "",StringUtils.getImage(configModel.data?.menusSale?.payment?.get(4)?.icon)) { trueMoneyFunction() }
                    .addAction(configModel.data?.menusSale?.payment?.get(5)?.display, "",StringUtils.getImage(configModel.data?.menusSale?.payment?.get(5)?.icon)) { shopeeFunction() }.create()
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
                )
                layoutMenu.addView(menuView, params)
            }
            PaymentAction.REPORT -> {
                val menuView: View = GridMenu.Builder(applicationContext)
                    .addAction(configModel.data?.menusMain?.get(0)?.payment?.get(0)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(0)?.payment?.get(0)?.icon))  { linePayFunction() }
                    .addAction(configModel.data?.menusMain?.get(0)?.payment?.get(1)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(0)?.payment?.get(1)?.icon)) { promptPayFunction() }
                    .addAction(configModel.data?.menusMain?.get(0)?.payment?.get(2)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(0)?.payment?.get(2)?.icon)) { aliPayFunction() }
                    .addAction(configModel.data?.menusMain?.get(0)?.payment?.get(3)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(0)?.payment?.get(3)?.icon)) { wechatPayFunction() }
                    .addAction(configModel.data?.menusMain?.get(0)?.payment?.get(4)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(0)?.payment?.get(4)?.icon)) { trueMoneyFunction() }
                    .addAction(configModel.data?.menusMain?.get(0)?.payment?.get(5)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(0)?.payment?.get(5)?.icon)) { shopeeFunction() }.create()
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
                )
                layoutMenu.addView(menuView, params)
            }
            PaymentAction.AUDIT_REPORT -> {
            val menuView: View = GridMenu.Builder(applicationContext)
                .addAction(configModel.data?.menusMain?.get(0)?.payment?.get(0)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(0)?.payment?.get(0)?.icon))  { linePayFunction() }
                .addAction(configModel.data?.menusMain?.get(0)?.payment?.get(1)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(0)?.payment?.get(1)?.icon)) { promptPayFunction() }
                .addAction(configModel.data?.menusMain?.get(0)?.payment?.get(2)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(0)?.payment?.get(2)?.icon)) { aliPayFunction() }
                .addAction(configModel.data?.menusMain?.get(0)?.payment?.get(3)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(0)?.payment?.get(3)?.icon)) { wechatPayFunction() }
                .addAction(configModel.data?.menusMain?.get(0)?.payment?.get(4)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(0)?.payment?.get(4)?.icon)) { trueMoneyFunction() }
                .addAction(configModel.data?.menusMain?.get(0)?.payment?.get(5)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(0)?.payment?.get(5)?.icon)) { shopeeFunction() }.create()
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
            )
            layoutMenu.addView(menuView, params)
            }
            PaymentAction.HISTORY_DETAIL_BY_PAYMENT_TYPE -> {
                val menuView: View = GridMenu.Builder(applicationContext)
                    .addAction(configModel.data?.menusMain?.get(0)?.payment?.get(0)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(0)?.payment?.get(0)?.icon))  { linePayFunction() }
                    .addAction(configModel.data?.menusMain?.get(0)?.payment?.get(1)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(0)?.payment?.get(1)?.icon)) { promptPayFunction() }
                    .addAction(configModel.data?.menusMain?.get(0)?.payment?.get(2)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(0)?.payment?.get(2)?.icon)) { aliPayFunction() }
                    .addAction(configModel.data?.menusMain?.get(0)?.payment?.get(3)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(0)?.payment?.get(3)?.icon)) { wechatPayFunction() }
                    .addAction(configModel.data?.menusMain?.get(0)?.payment?.get(4)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(0)?.payment?.get(4)?.icon)) { trueMoneyFunction() }
                    .addAction(configModel.data?.menusMain?.get(0)?.payment?.get(5)?.display, "",StringUtils.getImage(configModel.data?.menusMain?.get(0)?.payment?.get(5)?.icon)) { shopeeFunction() }.create()
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
                )
                layoutMenu.addView(menuView, params)
            }
        }

    }

    override fun showAmountFromCustomerScan() {
        layoutSubHeader.visibility = View.VISIBLE
        ivBannerHeader.visibility = View.GONE
        ivSubAmount.visibility = View.VISIBLE
        txtHeaderDetail.visibility = View.GONE
    }

    override fun hideAmountFromBusinessScan() {
        layoutSubHeader.visibility = View.VISIBLE
//        ivBannerHeader.visibility = View.INVISIBLE
        ivSubAmount.visibility = View.GONE
        tvHeaderTitle.visibility = View.VISIBLE
        tvHeaderTitle.text = payTitle
        txtHeaderDetail.visibility = View.VISIBLE
        txtHeaderDetail.text = StringUtils.getText(configModel.data?.stringFile?.selectPaymentTypeLabel?.label)
    }

    override fun linePayFunction() {
        startNextActivity("linepay")
    }

    override fun promptPayFunction() {
        startNextActivity("promptpay")
    }

    override fun aliPayFunction() {
        startNextActivity("alipay")
    }

    override fun wechatPayFunction() {
        startNextActivity("wechat")
    }

    override fun trueMoneyFunction() {
        startNextActivity("truemoney")
    }

    override fun shopeeFunction() {
        startNextActivity("airpay")
    }

    private fun startActivity(cls: Class<*>?, bundle: Bundle) {
        val context: Context = BaseApplication.appContext!!
        val intent = Intent(context, cls)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtras(bundle)
        context.startActivity(intent)
    }

    private fun startNextActivity(channel: String?) {

        when (ACTION_METHOD_TYPE) {
            PaymentAction.SCAN -> {
                ScanCodeActivity.start(this, ACTION_METHOD_TYPE, payAmount, channel)
            }
            PaymentAction.VOID -> {
                val passwordDialog = InputPasswordDialog(this, InputPasswordDialog.Builder(this, PasswordType.VOID, object:
                    InputPasswordDialog.OnPasswordActionListener{
                    override fun onSuccess(dialogFragment: InputPasswordDialog) {
                        val intent = Intent(this@PaymentSelectorActivity, InputTransactionActivity::class.java)
                        intent.putExtra(PaymentAction.KEY_ACTION, ACTION_METHOD_TYPE)
                        intent.putExtra("pay_channel", channel)
                        startActivity(intent)
                        finish()
                        dialogFragment.dismiss()
                    }

                    override fun onFail(dialogFragment: InputPasswordDialog) {
                        dialogFragment.dismiss()
                    }
                }))
                passwordDialog.show()
            }
            PaymentAction.PRINT -> {
                val intent = Intent(this, PaymentActionTypeActivity::class.java)
                intent.putExtra("pay_channel", channel)
                intent.putExtra("pay_title", payTitle)
                intent.putExtra(PaymentAction.KEY_ACTION, PaymentAction.PRINT)
                startActivity(intent)
                finish()
            }
            PaymentAction.QUERY -> {
                val intent = Intent(this, PaymentActionTypeActivity::class.java)
                intent.putExtra("pay_channel", channel)
                intent.putExtra("pay_title", payTitle)
                intent.putExtra(PaymentAction.KEY_ACTION, PaymentAction.QUERY)
                startActivity(intent)
                finish()
            }
            PaymentAction.REPORT -> {
                val intent = Intent(this, ReportSummaryActivity::class.java)
                intent.putExtra("pay_channel", channel)
                intent.putExtra("pay_title", payTitle)
                startActivity(intent)
                finish()
            }
            PaymentAction.AUDIT_REPORT -> {
                val intent = Intent(this, ReportAuditActivity::class.java)
                intent.putExtra("pay_channel", channel)
                intent.putExtra("pay_title", payTitle)
                startActivity(intent)
                finish()
            }
            PaymentAction.SETTLEMENT -> {

            }
            PaymentAction.GENERATOR_QR -> {
                val intent = Intent(this, GeneratorQRActivity::class.java)
                intent.putExtra("pay_amount", payAmount)
                intent.putExtra("pay_channel", channel)
                startActivity(intent)
                finish()
            }
            PaymentAction.HISTORY_DETAIL_BY_PAYMENT_TYPE -> {
                HistoryDetailByChannelActivity.start(this, channel)
            }
        }

    }

    companion object {
        @JvmStatic
        fun start(context: Activity?, keyAction: String, isShowIntent: Boolean, title: String) {
            val intent =  Intent(context, PaymentSelectorActivity::class.java)
            intent.putExtra("show_input_intent", isShowIntent)
            intent.putExtra("pay_title", title)
            intent.putExtra(PaymentAction.KEY_ACTION, keyAction)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
        }

        @JvmStatic
        fun startWithInvoke(context: Activity?, keyAction: String, isShowIntent: Boolean, title: String, payChannel: String?) {
            val intent =  Intent(context, PaymentSelectorActivity::class.java)
            intent.putExtra("show_input_intent", isShowIntent)
            intent.putExtra("pay_title", title)
            intent.putExtra("pay_channel", payChannel)
            intent.putExtra(PaymentAction.KEY_ACTION, keyAction)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
            context?.finish()
        }

    }


}