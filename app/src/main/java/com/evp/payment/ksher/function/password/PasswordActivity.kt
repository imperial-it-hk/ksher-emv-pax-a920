package com.evp.payment.ksher.function.password

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import butterknife.BindView
import butterknife.OnClick
import com.evp.payment.ksher.R
import com.evp.payment.ksher.customview.gridmenu.GridMenu
import com.evp.payment.ksher.extension.gone
import com.evp.payment.ksher.function.main.BaseTimeoutActivity
import com.evp.payment.ksher.function.settings.view.PasswordContact
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.utils.constant.PasswordType


class PasswordActivity : BaseTimeoutActivity(), PasswordContact.View {

    @BindView(R.id.header_title)
    lateinit var tvHeaderTitle: AppCompatTextView

    @BindView(R.id.layout_menu)
    lateinit var layoutMenu: LinearLayout

    override fun initViews() {
        initMenu()
    }

    override fun loadParam() {
    }

    override val layoutId: Int
        get() = R.layout.activity_password

    @OnClick(R.id.layout_back)
    fun onBackClick() {
        onBackPressed()
    }

    override fun initMenu() {
        tvHeaderTitle.text = StringUtils.getText(configModel.data?.stringFile?.passwordLabel?.label)
        val menuView: View = GridMenu.Builder(applicationContext)
            .addAction(configModel.data?.menusPassword?.get(0)?.display,StringUtils.getText(configModel.data?.menusPassword?.get(0)?.label),StringUtils.getImage(configModel.data?.menusPassword?.get(0)?.icon)){ merchantFunction() }
            .addAction(configModel.data?.menusPassword?.get(1)?.display,StringUtils.getText(configModel.data?.menusPassword?.get(1)?.label),StringUtils.getImage(configModel.data?.menusPassword?.get(1)?.icon)){ voidAndRefundFunction() }
            .addAction(configModel.data?.menusPassword?.get(2)?.display,StringUtils.getText(configModel.data?.menusPassword?.get(2)?.label),StringUtils.getImage(configModel.data?.menusPassword?.get(2)?.icon)){ settlementFunction() }.create()
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
        )
        layoutMenu.addView(menuView, params)
    }

    override fun merchantFunction() {
        PasswordEditActivity.start(this, PasswordType.MERCHANT)
    }

    override fun voidAndRefundFunction() {
        PasswordEditActivity.start(this, PasswordType.VOID)
    }

    override fun settlementFunction() {
        PasswordEditActivity.start(this, PasswordType.SETTLEMENT)
    }

    companion object {

        @JvmStatic
        fun start(context: Activity?) {
            val intent = Intent(context, PasswordActivity::class.java)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
        }

    }
}