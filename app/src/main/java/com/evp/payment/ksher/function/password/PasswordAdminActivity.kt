package com.evp.payment.ksher.function.password

import android.app.Activity
import android.content.Intent
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import butterknife.BindView
import butterknife.OnClick
import com.evp.payment.ksher.R
import com.evp.payment.ksher.function.main.BaseTimeoutActivity
import com.evp.payment.ksher.function.password.view.PasswordAdminContact
import com.evp.payment.ksher.parameter.PasswordParam
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.utils.constant.PasswordType
import kotlinx.android.synthetic.main.activity_password_admin.*


class PasswordAdminActivity : BaseTimeoutActivity(), PasswordAdminContact.View {

    @BindView(R.id.header_title)
    lateinit var tvHeaderTitle: AppCompatTextView

    @BindView(R.id.layout_menu)
    lateinit var layoutMenu: LinearLayout

    @BindView(R.id.tv_old_password_title)
    lateinit var tvOldPasswordTitle: AppCompatTextView

    @BindView(R.id.tv_new_password_title)
    lateinit var tvNewPasswordTitle: AppCompatTextView

    @BindView(R.id.tv_verify_password_title)
    lateinit var tvVerifyTitle: AppCompatTextView

    @BindView(R.id.ed_old_password)
    lateinit var edOldPassword: AppCompatEditText

    @BindView(R.id.ed_new_password)
    lateinit var edNewPassword: AppCompatEditText

    @BindView(R.id.ed_verify_password)
    lateinit var edVerify: AppCompatEditText

//    private val typePassword by extraNotNull<String>("type")

    override fun initViews() {
        initMenu()
    }

    override fun loadParam() {
    }

    override val layoutId: Int
        get() = R.layout.activity_password_admin

    @OnClick(R.id.layout_back)
    fun onBackClick() {
        onBackPressed()
    }

    override fun initMenu() {
        tvHeaderTitle.text = StringUtils.getText(configModel.data?.stringFile?.passwordLabel?.label)
        tvOldPasswordTitle.text = StringUtils.getText(configModel.data?.stringFile?.passwordOldLabel?.label)
        tvNewPasswordTitle.text = StringUtils.getText(configModel.data?.stringFile?.passwordNewLabel?.label)
        tvVerifyTitle.text = StringUtils.getText(configModel.data?.stringFile?.passwordVerifyNewLabel?.label)

        setThemePrimaryColor(tvOldPasswordTitle)
        setThemePrimaryColor(tvNewPasswordTitle)
        setThemePrimaryColor(tvVerifyTitle)
        setThemePrimaryColor(btn_ok)
    }

    @OnClick(R.id.btn_ok)
    override fun updatePassword() {
        if((!edOldPassword.text.isNullOrEmpty() && !edNewPassword.text.isNullOrEmpty() && !edVerify.text.isNullOrEmpty()) && (edOldPassword.text.toString() ==   PasswordParam.admin.get().toString())) {

           if(edNewPassword.text.toString() == edVerify.text.toString()) {
               PasswordParam.admin.set(edVerify.text.toString())
               Toast.makeText(this, StringUtils.getText(configModel.data?.stringFile?.passwordSuccessLabel?.label), Toast.LENGTH_LONG).show()
               finish()
           }else{
               Toast.makeText(this, StringUtils.getText(configModel.data?.stringFile?.passwordFailLabel?.label), Toast.LENGTH_LONG).show()
           }
        }else{
            Toast.makeText(this, StringUtils.getText(configModel.data?.stringFile?.passwordFailLabel?.label), Toast.LENGTH_LONG).show()
        }
    }

    companion object {

        @JvmStatic
        fun start(context: Activity?) {
            val intent = Intent(context, PasswordAdminActivity::class.java)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
        }

    }
}