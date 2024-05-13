package com.evp.payment.ksher.function.main

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import butterknife.BindView
import butterknife.OnClick
import com.evp.eos.EosService
import com.evp.payment.ksher.R
import com.evp.payment.ksher.customview.gridmenu.GridMenu
import com.evp.payment.ksher.database.DAOAccess
import com.evp.payment.ksher.extension.gone
import com.evp.payment.ksher.extension.visible
import com.evp.payment.ksher.function.BaseApplication
import com.evp.payment.ksher.function.main.view.MainContact
import com.evp.payment.ksher.function.payment.action.InputAmountActivity
import com.evp.payment.ksher.function.settings.MoreActivity
import com.evp.payment.ksher.function.settlement.SettlementActivity
import com.evp.payment.ksher.function.voided.InputTransactionActivity
import com.evp.payment.ksher.utils.DeviceUtil
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.utils.ToastUtils
import com.evp.payment.ksher.utils.alarm.Util
import com.evp.payment.ksher.utils.constant.PasswordType
import com.evp.payment.ksher.utils.constant.PaymentAction
import com.evp.payment.ksher.utils.sharedpreferences.OnClickUtils
import com.ksher.ksher_sdk.Ksher_pay_sdk
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.system.exitProcess


@AndroidEntryPoint
class MainActivity : BaseActivity(), MainContact.View {
    @Inject
    lateinit var daoAccess: DAOAccess

    val REQUEST_SCAN = 115
    val REQUEST_SALE = 101

    @BindView(R.id.layout_menu)
    lateinit var layoutMenu: LinearLayout

    @BindView(R.id.v_bg)
    lateinit var inputView: View

    @BindView(R.id.rootView)
    lateinit var rootView: ConstraintLayout

    @BindView(R.id.ly_amount)
    lateinit var lyAmount: RelativeLayout


    override fun initViews() {
        initMenu()
        CoroutineScope(Dispatchers.IO).launch {
            Util.alermJob(applicationContext)

        }

    }

    override fun loadParam() {
    }

    override val layoutId: Int
        get() = R.layout.activity_main

    override fun onResume() {
        super.onResume()
        // Disable status bar, home, recent key
        DeviceUtil.setDeviceEnableExit()
    }

    override fun onBackPressed() {
        if (OnClickUtils.isOnDoubleClick) {
            val passwordDialog = InputPasswordDialog(this, InputPasswordDialog.Builder(
                this, PasswordType.ADMIN, object :
                InputPasswordDialog.OnPasswordActionListener {
                override fun onSuccess(dialogFragment: InputPasswordDialog) {
                    BaseApplication.exitApp()
                    dialogFragment.dismiss()
                }

                override fun onFail(dialogFragment: InputPasswordDialog) {
                    dialogFragment.dismiss()
                }
            }))
            passwordDialog.show()

        } else {
            ToastUtils.showMessage(StringUtils.getText(configModel.data?.stringFile?.clickAgainToExitLabel?.label))
        }
    }

    override fun initMenu() {
            setThemeSecondaryColor(rootView)
            setThemePrimaryColor(v_bg)

        if(configModel.data?.menusSale?.display!!){
            lyAmount.visible()
        }else{
            lyAmount.gone()
        }

        val menuView: View = GridMenu.Builder(applicationContext)
            .addAction(configModel.data?.menusMain?.get(0)?.display,StringUtils.getText(configModel.data?.menusMain?.get(0)?.label),StringUtils.getImage(configModel.data?.menusMain?.get(0)?.icon)) { scanFunction() }
            .addAction(configModel.data?.menusMain?.get(1)?.display,StringUtils.getText(configModel.data?.menusMain?.get(1)?.label),StringUtils.getImage(configModel.data?.menusMain?.get(1)?.icon)) { voidFunction() }
            .addAction(configModel.data?.menusMain?.get(2)?.display,StringUtils.getText(configModel.data?.menusMain?.get(2)?.label),StringUtils.getImage(configModel.data?.menusMain?.get(2)?.icon)){ settlementFunction() }
            .addAction(configModel.data?.menusMain?.get(3)?.display,StringUtils.getText(configModel.data?.menusMain?.get(3)?.label), StringUtils.getImage(configModel.data?.menusMain?.get(3)?.icon)){ queryFunction() }
            .addAction(configModel.data?.menusMain?.get(4)?.display,StringUtils.getText(configModel.data?.menusMain?.get(4)?.label), StringUtils.getImage(configModel.data?.menusMain?.get(4)?.icon)){ printFunction() }
            .addAction(configModel.data?.menusMain?.get(5)?.display,StringUtils.getText(configModel.data?.menusMain?.get(5)?.label), StringUtils.getImage(configModel.data?.menusMain?.get(5)?.icon)){ reportFunction() }
            .addAction(configModel.data?.menusMain?.get(6)?.display,StringUtils.getText(configModel.data?.menusMain?.get(6)?.label), StringUtils.getImage(configModel.data?.menusMain?.get(6)?.icon)){ moreFunction() }
            .create()
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
        )
        layoutMenu.addView(menuView, params)

    }

    @OnClick(R.id.v_bg)
    override fun inputFunction() {
        InputAmountActivity.start(this, PaymentAction.SALE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.d("onActivityResult requestCode" + requestCode)
        Timber.d("onActivityResult resultCode" + resultCode)
        Timber.d("onActivityResult data" + data)
    }


    override fun scanFunction() {
        InputAmountActivity. start(this, PaymentAction.SCAN)
    }

    override fun voidFunction() {
        val passwordDialog = InputPasswordDialog(this, InputPasswordDialog.Builder(this, PasswordType.VOID, object :
            InputPasswordDialog.OnPasswordActionListener {
            override fun onSuccess(dialogFragment: InputPasswordDialog) {
                val intent = Intent(this@MainActivity, InputTransactionActivity::class.java)
                intent.putExtra(PaymentAction.KEY_ACTION, PaymentAction.VOID)
                startActivity(intent)
                dialogFragment.dismiss()
            }

            override fun onFail(dialogFragment: InputPasswordDialog) {
                dialogFragment.dismiss()
            }
        }))
        passwordDialog.show()
    }

    override fun settlementFunction() {
        val passwordDialog =
            InputPasswordDialog(this, InputPasswordDialog.Builder(this, PasswordType.SETTLEMENT, object :
                InputPasswordDialog.OnPasswordActionListener {
                override fun onSuccess(dialogFragment: InputPasswordDialog) {
                    SettlementActivity.start(this@MainActivity, PaymentAction.SETTLEMENT, false)
                    dialogFragment.dismiss()
                }

                override fun onFail(dialogFragment: InputPasswordDialog) {
                    dialogFragment.dismiss()
                }
            }))
        passwordDialog.show()
//        lifecycleScope.launch(Dispatchers.IO) {
//            val ksherPay = Ksher_pay_sdk(KsherConstant.appid, KsherConstant.privateKey)
//
//            val response = async { gatewayOrderQuery(ksherPay) }.await()
//
//            Timber.d("abcdfg $response")
//        }
//        Toast.makeText(this, "settle", Toast.LENGTH_LONG).show()
    }

    suspend fun gatewayOrderQuery(ksherPay: Ksher_pay_sdk): String {
        return ksherPay.GatewayOrderQuery(
            "77721"
        )
    }

    override fun queryFunction() {
        PaymentActionTypeActivity.start(this, PaymentAction.QUERY,  StringUtils.getText(configModel.data?.stringFile?.qrInquiryLabel?.label))
//        val intent = Intent(this, PaymentSelectorActivity::class.java)
//        intent.putExtra("show_input_intent", false)
//        intent.putExtra("pay_title", "QR Inquiry")
//        intent.putExtra(PaymentAction.KEY_ACTION, PaymentAction.QUERY)
//        startActivity(intent)
//        Toast.makeText(this, "query", Toast.LENGTH_LONG).show()
//        lifecycleScope.launch(Dispatchers.IO) {
//            val ksherPay = Ksher_pay_sdk(KsherConstant.appid, KsherConstant.privateKey)
//
//            val response = async { orderRefund(ksherPay) }.await()
//
//            Timber.d("abcdfg $response")
//        }
//        Toast.makeText(this, "settle", Toast.LENGTH_LONG).show()
//    }
//
//    suspend fun orderRefund(ksherPay: Ksher_pay_sdk): String {
//        return ksherPay.OrderRefund(
//            "87721",
//            "THB",
//            "77721",
//            100,
//            100
//        )
    }

    override fun printFunction() {
        PaymentActionTypeActivity.start(this, PaymentAction.PRINT,  StringUtils.getText(configModel.data?.stringFile?.printLabel?.label))
    }

    override fun reportFunction() {
        PaymentActionTypeActivity.start(this, PaymentAction.REPORT,  StringUtils.getText(configModel.data?.stringFile?.selectReportTypeLabel?.label))
    }

    override fun moreFunction() {
        MoreActivity.start(this)
//        val intent = Intent(this, MoreActivity::class.java)
//        startActivity(intent)

//        Completable.fromAction(ProgressNotifier.getInstance()::show)
//            .andThen(TransactionPrinting(true).printLastTrans())
//            .doFinally(ProgressNotifier.getInstance()::dismiss)
//            .onErrorResumeNext { e ->
//                DeviceUtil.beepErr()
//                DialogUtils.showAlertTimeout(e.message, DialogUtils.TIMEOUT_FAIL)
//            }
//            .doFinally { }
//            .subscribe()
//        Toast.makeText(this, "report", Toast.LENGTH_LONG).show()
//        Toast.makeText(this, "more", Toast.LENGTH_LONG).show()

    }

//    override fun toConfig() {
//        val intent = Intent(
//            this, InputManagerPasswordActivity::class.java
//        )
//        intent.putExtra(InputManagerPasswordActivity.KEY_FINISH_FLAG, true)
//        startActivity(intent)
//        RxBus.getDefault().register(this, true, TransactionActionEvent::class.java) { event ->
//            RxBus.getDefault().unregister(this)
//            if (event is InputManagerPasswordResponseEvent) {
//                startActivity(Intent(this, SettingsActivity::class.java))
//            }
//        }
//    }

//    override fun toSetting(input: String?) {
//        startActivity(Intent(Settings.ACTION_SETTINGS))

//    }


    companion object {

        @JvmStatic
        fun startClearTop(context: Activity?) {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
        }

    }


}