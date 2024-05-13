package com.evp.payment.ksher.function.config

import android.app.Activity
import android.content.Intent
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import butterknife.BindView
import butterknife.OnClick
import com.evp.payment.ksher.R
import com.evp.payment.ksher.database.repository.SettlementRepository
import com.evp.payment.ksher.function.config.view.AcquirerContact
import com.evp.payment.ksher.function.main.BaseTimeoutActivity
import com.evp.payment.ksher.function.main.InputPasswordDialog
import com.evp.payment.ksher.function.settlement.SettlementActivity
import com.evp.payment.ksher.parameter.MerchantParam
import com.evp.payment.ksher.parameter.SystemParam
import com.evp.payment.ksher.parameter.TerminalParam
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.utils.constant.PasswordType
import com.evp.payment.ksher.utils.constant.PaymentAction
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AcquirerActivity : BaseTimeoutActivity(), AcquirerContact.View {
    @Inject
    lateinit var settlementRepository: SettlementRepository

    @BindView(R.id.header_title)
    lateinit var tvHeaderTitle: AppCompatTextView

    //Acquirer
    @BindView(R.id.tv_acquirer_title)
    lateinit var tvAcquirerTitle: AppCompatTextView

    @BindView(R.id.tv_acquirer_detail)
    lateinit var tvAcquirerDetail: AppCompatTextView

    //Transaction ID.
    @BindView(R.id.tv_terminal_id_title)
    lateinit var tvTerminalIdTitle: AppCompatTextView

    @BindView(R.id.tv_tv_terminal_id_detail)
    lateinit var tvTerminalIdDetail: AppCompatTextView

    //Merchant ID.
    @BindView(R.id.tv_merchant_title)
    lateinit var tvMerchantTitle: AppCompatTextView

    @BindView(R.id.tv_merchant_detail)
    lateinit var tvMerchantDetail: AppCompatTextView

    //Batch No.
    @BindView(R.id.tv_batch_no_title)
    lateinit var tvBatchNoTitle: AppCompatTextView

    @BindView(R.id.tv_batch_no_detail)
    lateinit var tvBatchNoDetail: AppCompatTextView

//    private val typePassword by extraNotNull<String>("type")

    override fun initViews() {
        initMenu()
    }

    override fun loadParam() {
    }

    override val layoutId: Int
        get() = R.layout.activity_acquirer

    @OnClick(R.id.layout_back)
    fun onBackClick() {
        onBackPressed()
    }

    override fun initMenu() {
        tvHeaderTitle.text = StringUtils.getText(configModel.data?.stringFile?.acquirerLabel?.label)
        tvAcquirerTitle.text = StringUtils.getText(configModel.data?.stringFile?.acquirerLabel?.label)
        tvTerminalIdTitle.text = StringUtils.getText(configModel.data?.stringFile?.terminalIDLabel?.label)
        tvMerchantTitle.text =  StringUtils.getText(configModel.data?.stringFile?.merchantIDLabel?.label)
        tvBatchNoTitle.text = StringUtils.getText(configModel.data?.stringFile?.batchNoLabel?.label)

        tvAcquirerDetail.text = configModel.data?.config?.acquirer.orEmpty()
        tvTerminalIdDetail.text = TerminalParam.number.get()
        tvMerchantDetail.text = MerchantParam.merchantId.get()
        tvBatchNoDetail.text = SystemParam.batchNo.get()
    }

    override fun acquirerFunction() {

    }

    @OnClick(R.id.btn_terminal_id)
    override fun terminalIDFunction() {
        CoroutineScope(Dispatchers.Main).launch {
            val inputDialog = InputTextDialog(
                this@AcquirerActivity, InputTextDialog.Builder(this@AcquirerActivity, StringUtils.getText(configModel.data?.stringFile?.terminalIDLabel?.label),TerminalParam.number.get().orEmpty(), false ,object : InputTextDialog.OnActionListener {
                    override fun onSuccess(result: String, dialogFragment: InputTextDialog) {
                        TerminalParam.number.set(result)
                        tvTerminalIdDetail.text = TerminalParam.number.get()
                        startCountDown()
                        dialogFragment.dismiss()
                    }

                    override fun onFail(dialogFragment: InputTextDialog) {
                        startCountDown()
                        dialogFragment.dismiss()
                    }

                })
            )
            inputDialog.show()
        }
    }

    @OnClick(R.id.btn_merchant_id)
    override fun merchantIDFunction() {
        CoroutineScope(Dispatchers.Main).launch {
            val inputDialog = InputTextDialog(
                this@AcquirerActivity,
                InputTextDialog.Builder(this@AcquirerActivity, StringUtils.getText(configModel.data?.stringFile?.merchantIDLabel?.label),MerchantParam.merchantId.get().orEmpty(), false ,object : InputTextDialog.OnActionListener {
                    override fun onSuccess(result: String, dialogFragment: InputTextDialog) {
                        MerchantParam.merchantId.set(result)
                        tvMerchantDetail.text = MerchantParam.merchantId.get()
                        startCountDown()
                        dialogFragment.dismiss()
                    }

                    override fun onFail(dialogFragment: InputTextDialog) {
                        startCountDown()
                        dialogFragment.dismiss()
                    }

                })
            )
            inputDialog.show()
        }
    }

    @OnClick(R.id.btn_batch_no)
    override fun batchNoFunction() {
        CoroutineScope(Dispatchers.IO).launch {
            settlementRepository.getAllSettlementSaleAndRefundCount().collect {
                if (it?.getOrNull()?.saleTotalAmount ?: 0 == 0 && it?.getOrNull()?.refundTotalAmount ?: 0 == 0) {
                    updateBatchId()
                } else {
                    showDialog(msg = StringUtils.getText(configModel.data?.stringFile?.allTransactionNeedSettledLabel?.label),
                        actionCancel = {},
                        actionConfirm = {
                            val passwordDialog =
                                InputPasswordDialog(
                                    this@AcquirerActivity,
                                    InputPasswordDialog.Builder(
                                        this@AcquirerActivity,
                                        PasswordType.SETTLEMENT,
                                        object :
                                            InputPasswordDialog.OnPasswordActionListener {
                                            override fun onSuccess(dialogFragment: InputPasswordDialog) {
                                                SettlementActivity.start(
                                                    this@AcquirerActivity,
                                                    PaymentAction.SETTLEMENT,
                                                    false
                                                )
                                                dialogFragment.dismiss()
                                            }

                                            override fun onFail(dialogFragment: InputPasswordDialog) {
                                                dialogFragment.dismiss()
                                            }
                                        })
                                )
                            passwordDialog.show()
                        })
                }
            }
        }


    }

    private fun updateBatchId(){
        CoroutineScope(Dispatchers.Main).launch {
            val inputDialog = InputTextDialog(
                this@AcquirerActivity,
                InputTextDialog.Builder(this@AcquirerActivity, StringUtils.getText(configModel.data?.stringFile?.batchNoLabel?.label),SystemParam.batchNo.get().orEmpty(), false ,object : InputTextDialog.OnActionListener {
                    override fun onSuccess(result: String, dialogFragment: InputTextDialog) {
                        SystemParam.batchNo.set(result)
                        tvBatchNoDetail.text = SystemParam.batchNo.get()
                        startCountDown()
                        dialogFragment.dismiss()
                    }

                    override fun onFail(dialogFragment: InputTextDialog) {
                        startCountDown()
                        dialogFragment.dismiss()
                    }

                })
            )
            inputDialog.show()
        }
    }

    companion object {

        @JvmStatic
        fun start(context: Activity?) {
            val intent = Intent(context, AcquirerActivity::class.java)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
        }

    }
}