package com.evp.payment.ksher.function.config

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.net.wifi.WifiManager
import android.util.Log
import androidx.appcompat.widget.AppCompatTextView
import butterknife.BindView
import butterknife.OnClick
import com.evp.payment.ksher.R
import com.evp.payment.ksher.function.config.view.CommContact
import com.evp.payment.ksher.function.main.BaseTimeoutActivity
import com.evp.payment.ksher.parameter.MerchantParam
import com.evp.payment.ksher.parameter.SystemParam
import com.evp.payment.ksher.utils.DeviceUtil
import com.evp.payment.ksher.utils.StringUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class CommDetailActivity : BaseTimeoutActivity(), CommContact.View {

    @BindView(R.id.header_title)
    lateinit var tvHeaderTitle: AppCompatTextView

    //Communication Type
    @BindView(R.id.tv_communication_type_title)
    lateinit var tvCommunicationTitle: AppCompatTextView

    @BindView(R.id.tv_communication_type_detail)
    lateinit var tvCommunicationDetail: AppCompatTextView

    //Timeout
    @BindView(R.id.tv_timeout_title)
    lateinit var tvTimeoutTitle: AppCompatTextView

    @BindView(R.id.tv_timeout_detail)
    lateinit var tvTimeoutDetail: AppCompatTextView

//    //APN
//    @BindView(R.id.tv_apn_title)
//    lateinit var tvApnTitle: AppCompatTextView
//
//    @BindView(R.id.tv_apn_detail)
//    lateinit var tvApnDetail: AppCompatTextView

    //WIFI
    @BindView(R.id.tv_wifi_title)
    lateinit var tvWifiTitle: AppCompatTextView

//    private val typePassword by extraNotNull<String>("type")

    override fun initViews() {
        initMenu()
    }

    override fun loadParam() {
    }

    override val layoutId: Int
        get() = R.layout.activity_comm_detail

    @OnClick(R.id.layout_back)
    fun onBackClick() {
        onBackPressed()
    }

    override fun initMenu() {
        tvHeaderTitle.text = StringUtils.getText(configModel.data?.stringFile?.commLabel?.label)
        tvCommunicationTitle.text = StringUtils.getText(configModel.data?.stringFile?.communicationLabel?.label)
        tvTimeoutTitle.text = StringUtils.getText(configModel.data?.stringFile?.connectionTimeoutLabel?.label)
//        tvApnTitle.text = StringUtils.getText(configModel.data?.stringFile?.apnLabel?.label)
        tvWifiTitle.text = StringUtils.getText(configModel.data?.stringFile?.openWifiLabel?.label)

        tvCommunicationDetail.text = configModel.data?.config?.communicationType.orEmpty()
        tvTimeoutDetail.text = SystemParam.connectionTimeout.get().orEmpty()
//        tvApnDetail.text = MerchantParam.apn.get().orEmpty()
    }

    @OnClick(R.id.btn_comm_communication_type)
    override fun communicationTypeFunction() {
//        CommunicationTypeActivity.start(this)
    }

    @OnClick(R.id.btn_comm_timeout)
    override fun connectionTimeoutFunction() {
        CoroutineScope(Dispatchers.Main).launch {
            val timeoutDialog = TimeoutSelectDialog(
                this@CommDetailActivity,
                TimeoutSelectDialog.Builder(this@CommDetailActivity, StringUtils.getText(configModel.data?.stringFile?.connectionTimeoutLabel?.label), SystemParam.connectionTimeout.get().orEmpty(), object : TimeoutSelectDialog.OnActionListener {
                    override fun onSuccess(time: String, dialogFragment: TimeoutSelectDialog) {
                        SystemParam.connectionTimeout.set(time)
                        tvTimeoutDetail.text = SystemParam.connectionTimeout.get().orEmpty()
                        startCountDown()
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

    override fun apnFunction() {

//        CoroutineScope(Dispatchers.Main).launch {
//            val inputDialog = InputTextDialog(
//                this@CommDetailActivity, InputTextDialog.Builder(this@CommDetailActivity, StringUtils.getText(configModel.data?.stringFile?.apnLabel?.label), MerchantParam.apn.get().orEmpty(), true,object : InputTextDialog.OnActionListener {
//                    override fun onSuccess(result: String, dialogFragment: InputTextDialog) {
//                        MerchantParam.apn.set(result)
//                        tvApnDetail.text =  MerchantParam.apn.get().orEmpty()
//                        startCountDown()
//                        dialogFragment.dismiss()
//                    }
//
//                    override fun onFail(dialogFragment: InputTextDialog) {
//                        startCountDown()
//                        dialogFragment.dismiss()
//                    }
//
//                })
//            )
//            inputDialog.show()
//        }
    }

//    private fun getAPN(){
//        val c: Cursor? = applicationContext.contentResolver.query(
//            Uri.parse("content://telephony/carriers/current"),
//            null,
//            null,
//            null,
//            null
//        )
//
//        Log.e(
//            "MainActivity", "getColumnNames: " + Arrays.toString(c?.getColumnNames())
//        ) //get the column names from here.
//
//        if (c!!.moveToFirst()) {
//            do {
//                val data: String =c.getString(c.getColumnIndex("name")) //one of the column name to get the APN names.
//                tvApnDetail.text =  data
//            } while (c.moveToNext())
//        }
//        c.close()
//
//    }

    override fun onResume() {
        super.onResume()
        DeviceUtil.setDeviceStatus()
    }

    @OnClick(R.id.btn_comm_wifi)
    override fun openWifiFunction() {
        DeviceUtil.setDeviceEnableExit()
        startActivity(Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
    }

    companion object {

        @JvmStatic
        fun start(context: Activity?) {
            val intent = Intent(context, CommDetailActivity::class.java)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
        }

    }
}