package com.evp.payment.ksher.function.print

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import butterknife.BindView
import butterknife.OnClick
import com.evp.payment.ksher.R
import com.evp.payment.ksher.database.repository.TransactionRepository
import com.evp.payment.ksher.extension.extraNotNull
import com.evp.payment.ksher.function.BaseApplication
import com.evp.payment.ksher.function.main.BaseTimeoutActivity
import com.evp.payment.ksher.function.payment.action.print.PrintActivity
import com.evp.payment.ksher.function.print.view.PrintInputTransactionContact
import com.evp.payment.ksher.parameter.ControllerParam
import com.evp.payment.ksher.printing.TransactionPrinting
import com.evp.payment.ksher.utils.DeviceUtil
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.utils.keyboard.KeyboardPasswordUtil
import com.evp.payment.ksher.utils.transactions.ETransType
import com.evp.payment.ksher.view.progressbar.ProgressNotifier
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Completable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
open class PrintInputTransactionActivity : BaseTimeoutActivity(),
    KeyboardPasswordUtil.KeyboardPasswordListener, PrintInputTransactionContact.View {

    @Inject
    lateinit var transactionRepository: TransactionRepository

    @BindView(R.id.iv_sub_header_channel)
    lateinit var ivSubHeader: AppCompatImageView

    @BindView(R.id.tv_transaction)
    lateinit var tvAmount: AppCompatTextView

    @BindView(R.id.keyboard_password)
    lateinit var keyboard: ConstraintLayout

    @BindView(R.id.layout_sub_header)
    lateinit var layoutTitle: LinearLayout

    @BindView(R.id.tv_sub_header_title)
    lateinit var tvHeaderTitle: AppCompatTextView

    @BindView(R.id.tv_sub_header_detail)
    lateinit var tvDetailTitle: AppCompatTextView

    @BindView(R.id.key_confirm)
    lateinit var btnConfirm: AppCompatButton

    @BindView(R.id.key_cancel)
    lateinit var btnCancel: AppCompatButton


    //    private val payChannel by extraNotNull<String>("pay_channel")
    private val payTitle by extraNotNull<String>("pay_title")
    private val paySubTitle by extraNotNull<String>("pay_sub_title")

    var keyboardPasswordUtil = KeyboardPasswordUtil()
    override fun loadParam() {
    }

    override val layoutId: Int
        get() = R.layout.activity_print_input_transaction

    override fun initViews() {
        keyboardPasswordUtil.init(keyboard, this)
        layoutTitle.visibility = View.VISIBLE
        tvHeaderTitle.visibility = View.VISIBLE
        tvHeaderTitle.text = payTitle
        tvDetailTitle.visibility = View.VISIBLE
        tvDetailTitle.text =
            StringUtils.getText(configModel.data?.stringFile?.inputTraceLabel?.label)
        ivSubHeader.visibility = View.VISIBLE
        btnConfirm.text = StringUtils.getText(configModel.data?.button?.buttonOk?.label)
        btnCancel.text = StringUtils.getText(configModel.data?.button?.buttonCancel?.label)

        setThemePrimaryColor(btnConfirm)
        setThemePrimaryColor(btnCancel)
//        setupSubHeaderLogo(ivSubHeader, payChannel)
    }

    override fun updateAmount(displayAmount: String?) {
        tvAmount.text = displayAmount
    }

    override fun confirm() {
        queryTransData(tvAmount.text.toString())
    }

    private fun queryTransData(traceNo: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            if (traceNo.isNullOrEmpty()) {
                DeviceUtil.beepErr()
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(
                        this@PrintInputTransactionActivity,
                        StringUtils.getText(configModel.data?.stringFile?.noTransactionLabel?.label),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                transactionRepository.getAnyTransaction(traceNo.toLong()).collect {
                    val transResult = it?.getOrNull()
                    if (transResult == null) {
                        DeviceUtil.beepErr()
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(
                                this@PrintInputTransactionActivity,
                                StringUtils.getText(configModel.data?.stringFile?.noTransactionLabel?.label),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        if (transResult.transType == ETransType.VOID.toString() || transResult.transType == ETransType.REFUND.toString()) {
                            transactionRepository.getTransactionDetailByOriginalTraceNo(
                                traceNo.toLong().toString()
                            ).collect {
                                val transVoidRefundResult = it?.getOrNull()
                                if (transVoidRefundResult == null) {
                                    DeviceUtil.beepErr()
                                    CoroutineScope(Dispatchers.Main).launch {
                                        Toast.makeText(
                                            this@PrintInputTransactionActivity,
                                            StringUtils.getText(configModel.data?.stringFile?.noTransactionLabel?.label),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                } else {
                                    val bundle = Bundle()
                                    val intent =
                                        Intent(
                                            this@PrintInputTransactionActivity,
                                            PrintActivity::class.java
                                        )
                                    intent.putExtra("pay_title", payTitle)
                                    intent.putExtra("pay_sub_title", paySubTitle)
                                    bundle.putParcelable("transData", transVoidRefundResult)
                                    intent.putExtra("re_print", true)
                                    intent.putExtra("slipCount", 0)
                                    intent.putExtras(bundle)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                        } else {
                            val bundle = Bundle()
                            val intent =
                                Intent(
                                    this@PrintInputTransactionActivity,
                                    PrintActivity::class.java
                                )
                            intent.putExtra("pay_title", payTitle)
                            intent.putExtra("pay_sub_title", paySubTitle)
                            bundle.putParcelable("transData", transResult)
                            intent.putExtra("re_print", true)
                            intent.putExtra("slipCount", 0)
                            intent.putExtras(bundle)
                            startActivity(intent)
                            finish()
                        }
                    }
                    keyboardPasswordUtil.clear()
                }
            }
        }
    }

    override fun cancel() {
        finish()
    }

    protected fun startActivity(cls: Class<*>?, bundle: Bundle) {
        val context: Context = BaseApplication.appContext!!
        val intent = Intent(context, cls)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtras(bundle)
        context.startActivity(intent)
    }

    @OnClick(R.id.layout_back)
    fun onBackClick() {
        onBackPressed()
    }

}
