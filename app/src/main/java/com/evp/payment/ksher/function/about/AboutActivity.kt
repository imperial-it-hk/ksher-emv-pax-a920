package com.evp.payment.ksher.function.about

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import butterknife.BindView
import butterknife.OnClick
import com.evp.payment.ksher.R
import com.evp.payment.ksher.extension.gone
import com.evp.payment.ksher.function.about.view.AboutContact
import com.evp.payment.ksher.function.main.BaseTimeoutActivity
import com.evp.payment.ksher.function.settings.view.ConfigContact
import com.evp.payment.ksher.parameter.SystemParam
import com.evp.payment.ksher.utils.DeviceUtil
import com.evp.payment.ksher.utils.StringUtils
import java.util.*


class AboutActivity : BaseTimeoutActivity(), AboutContact.View {

    @BindView(R.id.tv_software_title)
    lateinit var tvSoftwareTitle: AppCompatTextView

    @BindView(R.id.tv_software_detail)
    lateinit var tvSoftwareDetail: AppCompatTextView

    @BindView(R.id.tv_config_title)
    lateinit var tvConfigTitle: AppCompatTextView

    @BindView(R.id.tv_config_detail)
    lateinit var tvConfigDetail: AppCompatTextView

    @BindView(R.id.tv_resource_title)
    lateinit var tvResourceTitle: AppCompatTextView

    @BindView(R.id.tv_resource_detail)
    lateinit var tvResourceDetail: AppCompatTextView


//    private val typePassword by extraNotNull<String>("type")

    override fun initViews() {
        initMenu()
    }

    override fun loadParam() {
    }

    override val layoutId: Int
        get() = R.layout.activity_abount

    @OnClick(R.id.layout_back)
    fun onBackClick() {
        onBackPressed()
    }

    override fun initMenu() {
        tvSoftwareTitle.text = StringUtils.getText(configModel.data?.stringFile?.softwareVersionLabel?.label)
        tvConfigTitle.text = StringUtils.getText(configModel.data?.stringFile?.configurationLabel?.label)
        tvResourceTitle.text = StringUtils.getText(configModel.data?.stringFile?.resourceLabel?.label)

        setThemePrimaryColor(tvSoftwareTitle)
        setThemePrimaryColor(tvConfigTitle)
        setThemePrimaryColor(tvResourceTitle)

        softwareVersionDisplay()
        configFileDisplay()
        resourceFileDisplay()
    }

    override fun softwareVersionDisplay() {
        tvSoftwareDetail.text = DeviceUtil.getVersionName(this, StringUtils.getText(configModel.data?.setting?.appName?.label))
    }

    override fun configFileDisplay() {
        tvConfigDetail.text = SystemParam.configName.get()
    }

    override fun resourceFileDisplay() {
        tvResourceDetail.text = SystemParam.resourceName.get()
    }

    companion object {

        @JvmStatic
        fun start(context: Activity?) {
            val intent = Intent(context, AboutActivity::class.java)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
        }

    }
}