package com.evp.payment.ksher.function.config

import android.app.Activity
import android.content.Intent
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.OnClick
import com.evp.payment.ksher.R
import com.evp.payment.ksher.config.ConfigModel
import com.evp.payment.ksher.database.repository.SettlementRepository
import com.evp.payment.ksher.extension.gone
import com.evp.payment.ksher.extension.visible
import com.evp.payment.ksher.function.main.BaseTimeoutActivity
import com.evp.payment.ksher.function.main.InputPasswordDialog
import com.evp.payment.ksher.function.main.MainActivity
import com.evp.payment.ksher.function.password.PasswordAdminActivity
import com.evp.payment.ksher.function.password.view.PasswordAdminContact
import com.evp.payment.ksher.function.settings.OtherActivity
import com.evp.payment.ksher.function.settings.view.ConfigContact
import com.evp.payment.ksher.function.settlement.SettlementActivity
import com.evp.payment.ksher.parameter.AppStoreParam
import com.evp.payment.ksher.parameter.SystemParam
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.utils.constant.PasswordType
import com.evp.payment.ksher.utils.constant.PaymentAction
import com.evp.payment.ksher.utils.sharedpreferences.SharedPreferencesUtil
import com.evp.payment.ksher.view.progressbar.ProgressNotifier
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class ConfigActivity : BaseTimeoutActivity(), ConfigContact.View {

    @BindView(R.id.header_title)
    lateinit var tvHeaderTitle: AppCompatTextView

    //Communication Type
    @BindView(R.id.iv_communication_type)
    lateinit var ivCommunicationType: AppCompatImageView

    @BindView(R.id.btn_config_communication_type)
    lateinit var btnCommunicationType: LinearLayout

    @BindView(R.id.tv_communication_type_title)
    lateinit var tvCommunicationTypeTitle: AppCompatTextView

    //Comm Detail
    @BindView(R.id.iv_comm_detail)
    lateinit var ivCommDetail: AppCompatImageView

    @BindView(R.id.btn_config_comm_detail)
    lateinit var btnCommDetail: LinearLayout

    @BindView(R.id.tv_comm_detail_title)
    lateinit var tvCommDetailTitle: AppCompatTextView

    //Acquirer
    @BindView(R.id.iv_acquirer)
    lateinit var ivAcquirer: AppCompatImageView

    @BindView(R.id.btn_config_acquirer)
    lateinit var btnAcquirer: LinearLayout

    @BindView(R.id.tv_acquirer_title)
    lateinit var tvAcquirerTitle: AppCompatTextView

    //Password
    @BindView(R.id.iv_password)
    lateinit var ivPassword: AppCompatImageView

    @BindView(R.id.btn_config_password)
    lateinit var btnPassword: LinearLayout

    @BindView(R.id.tv_password_title)
    lateinit var tvPasswordTitle: AppCompatTextView

    //Transaction Setting
    @BindView(R.id.iv_transaction)
    lateinit var ivTransaction: AppCompatImageView

    @BindView(R.id.btn_config_transaction)
    lateinit var btnTransactionSetting: LinearLayout

    @BindView(R.id.tv_transaction_title)
    lateinit var tvTransactionSettingTitle: AppCompatTextView

    //Other
    @BindView(R.id.iv_other)
    lateinit var ivOther: AppCompatImageView

    @BindView(R.id.btn_config_other)
    lateinit var btnOther: LinearLayout

    @BindView(R.id.tv_other_title)
    lateinit var tvOtherTitle: AppCompatTextView

//    private val typePassword by extraNotNull<String>("type")

    @Inject
    lateinit var settlementRepository: SettlementRepository

    override fun initViews() {
        initMenu()
    }

    override fun loadParam() {
    }

    override val layoutId: Int
        get() = R.layout.activity_config

    @OnClick(R.id.layout_back)
    fun onBackClick() {
        onBackPressed()
    }

    override fun initMenu() {
        tvHeaderTitle.text = StringUtils.getText(configModel.data?.stringFile?.configLabel?.label)
        tvCommunicationTypeTitle.text = StringUtils.getText(configModel.data?.stringFile?.communicationLabel?.label)
        tvCommDetailTitle.text = StringUtils.getText(configModel.data?.stringFile?.commLabel?.label)
        tvAcquirerTitle.text = StringUtils.getText(configModel.data?.stringFile?.acquirerLabel?.label)
        tvPasswordTitle.text =  StringUtils.getText(configModel.data?.stringFile?.passwordLabel?.label)
        tvTransactionSettingTitle.text =  StringUtils.getText(configModel.data?.stringFile?.transactionSettingLabel?.label)
        tvOtherTitle.text =  StringUtils.getText(configModel.data?.stringFile?.otherLabel?.label)

        ivCommunicationType.setImageBitmap(StringUtils.getImage(configModel.data?.button?.buttonCommunication?.icon))
        ivCommDetail.setImageBitmap(StringUtils.getImage(configModel.data?.button?.buttonCommDetail?.icon))
        ivAcquirer.setImageBitmap(StringUtils.getImage(configModel.data?.button?.buttonAcquirer?.icon))
        ivPassword.setImageBitmap(StringUtils.getImage(configModel.data?.button?.buttonPassword?.icon))
        ivTransaction.setImageBitmap(StringUtils.getImage(configModel.data?.button?.buttonTransactionSetting?.icon))
        ivOther.setImageBitmap(StringUtils.getImage(configModel.data?.button?.buttonOther?.icon))

    }

    @OnClick(R.id.btn_config_communication_type)
    override fun communicationTypeFunction() {
//        CommunicationTypeActivity.start(this)
        CoroutineScope(Dispatchers.IO).launch {
            settlementRepository.getAllSettlementSaleAndRefundCount().collect {
                if (it?.getOrNull()?.saleTotalAmount ?: 0 == 0 && it?.getOrNull()?.refundTotalAmount ?: 0 == 0) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val commuDialog = CommunicationTypeDialog(
                            this@ConfigActivity,
                            CommunicationTypeDialog.Builder(
                                this@ConfigActivity,
                                StringUtils.getText(configModel.data?.stringFile?.communicationLabel?.label),
                                SystemParam.communicationMode.get().orEmpty(),
                                object : CommunicationTypeDialog.OnActionListener {
                                    override fun onSuccess(type: String, dialogFragment: CommunicationTypeDialog
                                    ) {
                                        if (SystemParam.communicationMode.get() != type) {
                                            SystemParam.communicationMode.set(type)
                                            CoroutineScope(Dispatchers.IO).launch {
                                                updateConfigFile()
                                            }
                                        }

                                        startCountDown()
                                        dialogFragment.dismiss()
                                    }

                                    override fun onFail(dialogFragment: CommunicationTypeDialog) {
                                        startCountDown()
                                        dialogFragment.dismiss()
                                    }
                                })
                        )
                        commuDialog.show()
                    }
                } else {
                    showDialog(msg = StringUtils.getText(configModel.data?.stringFile?.allTransactionNeedSettledLabel?.label),
                        actionCancel = {},
                        actionConfirm = {
                            val passwordDialog =
                                InputPasswordDialog(
                                    this@ConfigActivity,
                                    InputPasswordDialog.Builder(
                                        this@ConfigActivity,
                                        PasswordType.SETTLEMENT,
                                        object :
                                            InputPasswordDialog.OnPasswordActionListener {
                                            override fun onSuccess(dialogFragment: InputPasswordDialog) {
                                                SettlementActivity.start(
                                                    this@ConfigActivity,
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

    suspend fun updateConfigFile() {
        ProgressNotifier.getInstance().show()
        ProgressNotifier.getInstance()
            .primaryContent(StringUtils.getText(configModel.data?.stringFile?.loadingLabel?.label))
        delay(2000)

        settlementRepository.deleteAllTransaction().collect {  }
        settlementRepository.deleteAllSettlement().collect {  }
        settlementRepository.deleteAllSuspendQr().collect {  }
        AppStoreParam.updateParams(true)

        withContext(Dispatchers.Main) {
            val json = SharedPreferencesUtil.getString("config_file", "")
            configModel = Gson().fromJson(json.get().toString(), ConfigModel::class.java)
            ProgressNotifier.getInstance()
                .showApprove(StringUtils.getText(configModel.data?.stringFile?.updateSuccessLabel?.label))
            delay(1000)
            SystemParam.traceNo.set("000001")
            SystemParam.invoiceNo.set("000001")
            SystemParam.batchNo.set("000001")
            ProgressNotifier.getInstance().dismiss()
            MainActivity.startClearTop(this@ConfigActivity)
        }
    }

    @OnClick(R.id.btn_config_comm_detail)
    override fun commFunction() {
        CommDetailActivity.start(this)
    }

    @OnClick(R.id.btn_config_acquirer)
    override fun acquirerFunction() {
        AcquirerActivity.start(this)
    }

    @OnClick(R.id.btn_config_password)
    override fun passwordAdminFunction() {
        PasswordAdminActivity.start(this)
    }

    @OnClick(R.id.btn_config_transaction)
    override fun transactionSetting() {
        TransactionSettingActivity.start(this)
    }

    @OnClick(R.id.btn_config_other)
    override fun otherFunction() {
        OtherActivity.start(this)
    }

    companion object {

        @JvmStatic
        fun start(context: Activity?) {
            val intent = Intent(context, ConfigActivity::class.java)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
        }

    }
}