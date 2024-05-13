package com.evp.payment.ksher.function.settings

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import butterknife.BindView
import butterknife.OnClick
import com.evp.payment.ksher.R
import com.evp.payment.ksher.config.ConfigModel
import com.evp.payment.ksher.customview.gridmenu.GridMenu
import com.evp.payment.ksher.database.SettlementItemModel
import com.evp.payment.ksher.database.repository.SettlementRepository
import com.evp.payment.ksher.extension.gone
import com.evp.payment.ksher.function.config.ConfigActivity
import com.evp.payment.ksher.function.config.LanguageSelectDialog
import com.evp.payment.ksher.function.config.TimeoutSelectDialog
import com.evp.payment.ksher.function.main.BaseTimeoutActivity
import com.evp.payment.ksher.function.main.InputPasswordDialog
import com.evp.payment.ksher.function.main.MainActivity
import com.evp.payment.ksher.function.password.PasswordActivity
import com.evp.payment.ksher.function.settings.view.SettingContact
import com.evp.payment.ksher.function.settlement.AutoSettlementActivity
import com.evp.payment.ksher.function.settlement.SettlementActivity
import com.evp.payment.ksher.parameter.AppStoreParam
import com.evp.payment.ksher.parameter.SystemParam
import com.evp.payment.ksher.utils.DeviceUtil
import com.evp.payment.ksher.utils.LanguageSettingUtil
import com.evp.payment.ksher.utils.Normalizer
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.utils.alarm.Util
import com.evp.payment.ksher.utils.constant.PasswordType
import com.evp.payment.ksher.utils.constant.PaymentAction
import com.evp.payment.ksher.utils.sharedpreferences.SharedPreferencesUtil
import com.evp.payment.ksher.view.dialog.DialogUtils
import com.evp.payment.ksher.view.progressbar.ProgressNotifier
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Completable
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SettingActivity : BaseTimeoutActivity(), SettingContact.View {
    @Inject
    lateinit var settlementRepository: SettlementRepository

    @BindView(R.id.header_title)
    lateinit var tvHeaderTitle: AppCompatTextView

    @BindView(R.id.layout_menu)
    lateinit var layoutMenu: LinearLayout

    override fun initViews() {
        tvHeaderTitle.text = StringUtils.getText(configModel.data?.stringFile?.settingLabel?.label)
        initMenu()
    }


    override fun loadParam() {
    }

    override val layoutId: Int
        get() = R.layout.activity_setting

    @OnClick(R.id.layout_back)
    fun onBackClick() {
        onBackPressed()
    }

    override fun initMenu() {
        setThemePrimaryColor(layoutHeader)
        setThemeSecondaryColor(rootView)
        val menuView: View = GridMenu.Builder(applicationContext)
            .addAction(
                configModel.data?.menusSetting?.get(0)?.display,
                StringUtils.getText(configModel.data?.menusSetting?.get(0)?.label),
                StringUtils.getImage(configModel.data?.menusSetting?.get(0)?.icon)
            ) { passwordFunction() }
            .addAction(
                configModel.data?.menusSetting?.get(1)?.display,
                StringUtils.getText(configModel.data?.menusSetting?.get(1)?.label),
                StringUtils.getImage(configModel.data?.menusSetting?.get(1)?.icon)
            ) { configFunction() }
            .addAction(
                configModel.data?.menusSetting?.get(2)?.display,
                StringUtils.getText(configModel.data?.menusSetting?.get(2)?.label),
                StringUtils.getImage(configModel.data?.menusSetting?.get(2)?.icon)
            ) { uploadParamFunction() }
            .addAction(
                configModel.data?.menusSetting?.get(3)?.display,
                StringUtils.getText(configModel.data?.menusSetting?.get(3)?.label),
                StringUtils.getImage(configModel.data?.menusSetting?.get(3)?.icon)
            ) { languageFunction() }.create()
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
        )
        layoutMenu.addView(menuView, params)

    }

    override fun passwordFunction() {
        val passwordDialog = InputPasswordDialog(
            this,
            InputPasswordDialog.Builder(this, PasswordType.MERCHANT, object :
                InputPasswordDialog.OnPasswordActionListener {
                override fun onSuccess(dialogFragment: InputPasswordDialog) {
                    startCountDown()
                    PasswordActivity.start(this@SettingActivity)
                    dialogFragment.dismiss()
                }

                override fun onFail(dialogFragment: InputPasswordDialog) {
                    startCountDown()
                    dialogFragment.dismiss()
                }
            })
        )
        passwordDialog.show()
    }

    override fun configFunction() {
        val passwordDialog =
            InputPasswordDialog(this, InputPasswordDialog.Builder(this, PasswordType.ADMIN, object :
                InputPasswordDialog.OnPasswordActionListener {
                override fun onSuccess(dialogFragment: InputPasswordDialog) {
                    startCountDown()
                    ConfigActivity.start(this@SettingActivity)
                    dialogFragment.dismiss()
                }

                override fun onFail(dialogFragment: InputPasswordDialog) {
                    startCountDown()
                    dialogFragment.dismiss()
                }
            }))
        passwordDialog.show()
    }

    override fun uploadParamFunction() {
        CoroutineScope(Dispatchers.IO).launch {
            settlementRepository.getAllSettlementSaleAndRefundCount().collect {
                if (it?.getOrNull()?.saleTotalAmount ?: 0 == 0 && it?.getOrNull()?.refundTotalAmount ?: 0 == 0) {
                    updateConfigFile()
                } else {
                    showDialog(msg = StringUtils.getText(configModel.data?.stringFile?.allTransactionNeedSettledLabel?.label),
                        actionCancel = {},
                        actionConfirm = {
                            val passwordDialog =
                                InputPasswordDialog(
                                    this@SettingActivity,
                                    InputPasswordDialog.Builder(
                                        this@SettingActivity,
                                        PasswordType.SETTLEMENT,
                                        object :
                                            InputPasswordDialog.OnPasswordActionListener {
                                            override fun onSuccess(dialogFragment: InputPasswordDialog) {
                                                SettlementActivity.start(
                                                    this@SettingActivity,
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
        AppStoreParam.updateParams(true)

        withContext(Dispatchers.Main) {
            val json = SharedPreferencesUtil.getString("config_file", "")
            configModel = Gson().fromJson(json.get().toString(), ConfigModel::class.java)
            ProgressNotifier.getInstance()
                .showApprove(StringUtils.getText(configModel.data?.stringFile?.updateSuccessLabel?.label))
            delay(1000)
            ProgressNotifier.getInstance().dismiss()
            MainActivity.startClearTop(this@SettingActivity)
        }
    }


    override fun languageFunction() {
        CoroutineScope(Dispatchers.Main).launch {
            val languageDialog = LanguageSelectDialog(
                this@SettingActivity,
                LanguageSelectDialog.Builder(
                    this@SettingActivity,
                    StringUtils.getText(configModel.data?.stringFile?.languageLabel?.label),
                    SystemParam.language.get().orEmpty(),
                    object : LanguageSelectDialog.OnActionListener {
                        override fun onSuccess(
                            language: String,
                            dialogFragment: LanguageSelectDialog
                        ) {
                            startCountDown()
                            if (SystemParam.language.get() != language) {
                                SystemParam.language.set(language)
                                MainActivity.startClearTop(this@SettingActivity)
                            }
                            dialogFragment.dismiss()
                        }

                        override fun onFail(dialogFragment: LanguageSelectDialog) {
                            startCountDown()
                            dialogFragment.dismiss()
                        }
                    })
            )
            languageDialog.show()
        }
    }

    companion object {

        @JvmStatic
        fun start(context: Activity?) {
            val intent = Intent(context, SettingActivity::class.java)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
        }

        @JvmStatic
        fun startWithInvoke(context: Activity?) {
            val intent = Intent(context, SettingActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
        }

    }
}