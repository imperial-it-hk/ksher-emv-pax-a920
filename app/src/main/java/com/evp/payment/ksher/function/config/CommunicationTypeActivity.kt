package com.evp.payment.ksher.function.config

import android.app.Activity
import android.content.Intent
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.OnClick
import com.evp.payment.ksher.R
import com.evp.payment.ksher.function.main.BaseTimeoutActivity
import com.evp.payment.ksher.function.settings.view.ConfigContact
import com.evp.payment.ksher.utils.StringUtils


class CommunicationTypeActivity : BaseTimeoutActivity() {

    @BindView(R.id.header_title)
    lateinit var tvHeaderTitle: AppCompatTextView

    //Mobile Type
    @BindView(R.id.btn_mobile_type)
    lateinit var btnMobileType: LinearLayout

    @BindView(R.id.tv_mobile_title)
    lateinit var tvMobileTitle: AppCompatTextView

    //Wifi Type
    @BindView(R.id.btn_wifi_type)
    lateinit var btnWifiType: LinearLayout

    @BindView(R.id.tv_wifi_title)
    lateinit var tvWifiTitle: AppCompatTextView

    //Demo Type
    @BindView(R.id.btn_demo_type)
    lateinit var btnDemoType: LinearLayout

    @BindView(R.id.tv_demo_title)
    lateinit var tvDemoTitle: AppCompatTextView

    //Lan Type
    @BindView(R.id.btn_lan_type)
    lateinit var btnLanType: LinearLayout

    @BindView(R.id.tv_lan_title)
    lateinit var tvLanTitle: AppCompatTextView


    override fun initViews() {
        initMenu()
    }

    override fun loadParam() {
    }

    override val layoutId: Int
        get() = R.layout.activity_communicatoin_type

    @OnClick(R.id.layout_back)
    fun onBackClick() {
        onBackPressed()
    }

    private fun initMenu() {
        tvHeaderTitle.text = StringUtils.getText(configModel.data?.stringFile?.communicationLabel?.label)
        tvMobileTitle.text = "MOBILE"
        tvWifiTitle.text = "WIFI"
        tvDemoTitle.text = "DEMO"
        tvLanTitle.text = "LAN"
    }

    companion object {

        @JvmStatic
        fun start(context: Activity?) {
            val intent = Intent(context, CommunicationTypeActivity::class.java)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
        }

    }
}