package com.evp.payment.ksher.function.settings

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import butterknife.BindView
import butterknife.OnClick
import com.evp.payment.ksher.R
import com.evp.payment.ksher.customview.gridmenu.GridMenu
import com.evp.payment.ksher.extension.visible
import com.evp.payment.ksher.function.about.AboutActivity
import com.evp.payment.ksher.function.history.TransactionHistoryActivity
import com.evp.payment.ksher.function.main.BaseTimeoutActivity
import com.evp.payment.ksher.function.main.InputPasswordDialog
import com.evp.payment.ksher.function.main.PaymentActionTypeActivity
import com.evp.payment.ksher.function.settings.view.MoreContact
import com.evp.payment.ksher.function.voided.InputTransactionActivity
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.utils.constant.PaymentAction


class MoreActivity : BaseTimeoutActivity(), MoreContact.View {

    @BindView(R.id.layout_menu)
    lateinit var layoutMenu: LinearLayout

    @BindView(R.id.layout_sub_header)
    lateinit var layoutTitle: LinearLayout

    @BindView(R.id.tv_sub_header_title)
    lateinit var tvHeaderTitle: AppCompatTextView

    @BindView(R.id.tv_sub_header_detail)
    lateinit var tvTitle: AppCompatTextView

    override fun initViews() {
        initMenu()
        layoutTitle.visible()
        tvHeaderTitle.visible()
        tvHeaderTitle.text = StringUtils.getText(configModel.data?.stringFile?.moreLabel?.label)
        tvTitle.visible()
        tvTitle.text = StringUtils.getText(configModel.data?.stringFile?.selectFunctionLabel?.label)
    }


    override fun loadParam() {
    }

    override val layoutId: Int
        get() = R.layout.activity_more

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    @OnClick(R.id.layout_back)
    fun onBackClick() {
        onBackPressed()
    }

    override fun initMenu() {
        val menuView: View = GridMenu.Builder(applicationContext)
            .addAction(configModel.data?.menusMain?.get(0)?.display,
                StringUtils.getText(configModel.data?.menusMore?.get(0)?.label), StringUtils.getImage(configModel.data?.menusMore?.get(0)?.icon) ) { historyFunction() }
            .addAction(configModel.data?.menusMain?.get(1)?.display,
                StringUtils.getText(configModel.data?.menusMore?.get(1)?.label),StringUtils.getImage(configModel.data?.menusMore?.get(1)?.icon))  { settingFunction() }
            .addAction(configModel.data?.menusMain?.get(2)?.display,
                StringUtils.getText(configModel.data?.menusMore?.get(2)?.label),StringUtils.getImage(configModel.data?.menusMore?.get(2)?.icon)) { aboutFunction() }.create()
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
        )
        layoutMenu.addView(menuView, params)

    }

    override fun historyFunction() {
        TransactionHistoryActivity.start(this)
    }


    override fun settingFunction() {
        SettingActivity.start(this)
    }

    override fun aboutFunction() {
        AboutActivity.start(this)
    }

    companion object {

        @JvmStatic
        fun start(context: Activity?) {
            val intent = Intent(context, MoreActivity::class.java)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
        }

    }

}