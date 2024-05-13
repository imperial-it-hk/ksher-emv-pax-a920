package com.evp.payment.ksher.function.config

import android.app.Activity
import android.content.Intent
import android.net.wifi.WifiManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import butterknife.BindView
import butterknife.OnClick
import com.evp.payment.ksher.R
import com.evp.payment.ksher.function.config.view.CommContact
import com.evp.payment.ksher.function.main.BaseTimeoutActivity
import com.evp.payment.ksher.function.main.MainActivity
import com.evp.payment.ksher.parameter.SystemParam
import com.evp.payment.ksher.utils.StringUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class TransactionSettingActivity : BaseTimeoutActivity() {

    @BindView(R.id.header_title)
    lateinit var tvHeaderTitle: AppCompatTextView

    //Timeout
    @BindView(R.id.tv_timeout_title)
    lateinit var tvTimeoutTitle: AppCompatTextView

    @BindView(R.id.tv_timeout_detail)
    lateinit var tvTimeoutDetail: AppCompatTextView

    //Transaction No.
    @BindView(R.id.tv_transaction_no_title)
    lateinit var tvTransactionNoTitle: AppCompatTextView

    @BindView(R.id.tv_transaction_no_detail)
    lateinit var tvTransactionNoDetail: AppCompatTextView


//    private val typePassword by extraNotNull<String>("type")

    override fun initViews() {
        initMenu()
    }

    override fun loadParam() {
    }

    override val layoutId: Int
        get() = R.layout.activity_transaction_setting

    @OnClick(R.id.layout_back)
    fun onBackClick() {
        onBackPressed()
    }

    fun initMenu() {
        tvHeaderTitle.text = StringUtils.getText(configModel.data?.stringFile?.transactionSettingLabel?.label)

        tvTimeoutTitle.text = StringUtils.getText(configModel.data?.stringFile?.transactionTimeoutLabel?.label)
        tvTransactionNoTitle.text = StringUtils.getText(configModel.data?.stringFile?.traceNoLabel?.label)

        tvTimeoutDetail.text = SystemParam.transactionTimeout.get()
        tvTransactionNoDetail.text  = SystemParam.traceNo.get()
    }

    @OnClick(R.id.btn_comm_timeout)
    fun transactionTimeoutFunction() {
        stopCountDown()
        runOnUiThread {
            val timeoutDialog = TimeoutSelectDialog(
                this,
                TimeoutSelectDialog.Builder(this,tvTimeoutTitle.text.toString(), SystemParam.transactionTimeout.get().orEmpty(), object : TimeoutSelectDialog.OnActionListener {
                    override fun onSuccess(time: String, dialogFragment: TimeoutSelectDialog) {
                        if(SystemParam.transactionTimeout.get() != time){
                            SystemParam.transactionTimeout.set(time)
                            tvTimeoutDetail.text = SystemParam.transactionTimeout.get()
                            startCountDown()
                            MainActivity.startClearTop(this@TransactionSettingActivity)
                        }


                        dialogFragment.dismiss()
                    }

                    override fun onFail(dialogFragment: TimeoutSelectDialog) {
                        startCountDown()
                        dialogFragment.dismiss()
                    }

                })
            )
            timeoutDialog.show()
        }
    }

//    @OnClick(R.id.btn_transaction_no)
//    fun setTransactionNumberFunction() {
//         tvTransactionNoDetail.text = SystemParam.traceNo.get()
//    }

    companion object {

        @JvmStatic
        fun start(context: Activity?) {
            val intent = Intent(context, TransactionSettingActivity::class.java)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
        }

    }
}