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
import com.evp.payment.ksher.extension.extraNotNull
import com.evp.payment.ksher.function.main.BaseTimeoutActivity
import com.evp.payment.ksher.function.password.view.PasswordEditContact
import com.evp.payment.ksher.parameter.PasswordParam
import com.evp.payment.ksher.parameter.SystemParam
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.utils.constant.PasswordType
import kotlinx.android.synthetic.main.activity_password_admin.*
import kotlinx.android.synthetic.main.activity_password_admin.btn_ok
import kotlinx.android.synthetic.main.activity_password_edit.*


class PasswordEditActivity : BaseTimeoutActivity(), PasswordEditContact.View {

    @BindView(R.id.header_title)
    lateinit var tvHeaderTitle: AppCompatTextView

    @BindView(R.id.layout_menu)
    lateinit var layoutMenu: LinearLayout

    @BindView(R.id.tv_new_password_title)
    lateinit var tvPasswordTitle: AppCompatTextView

    @BindView(R.id.tv_verify_password_title)
    lateinit var tvVerifyTitle: AppCompatTextView

    @BindView(R.id.ed_new_password)
    lateinit var edPassword: AppCompatEditText

    @BindView(R.id.ed_verify_password)
    lateinit var edVerify: AppCompatEditText

    private val typePassword by extraNotNull<String>("type")

    override fun initViews() {
        initMenu()
    }

    override fun loadParam() {
    }

    override val layoutId: Int
        get() = R.layout.activity_password_edit

    @OnClick(R.id.layout_back)
    fun onBackClick() {
        onBackPressed()
    }

    override fun initMenu() {
        when {
            typePassword.equals(PasswordType.MERCHANT, true) -> {
                tvHeaderTitle.text = StringUtils.getText(configModel.data?.stringFile?.merchantLabel?.label)
            }
            typePassword.equals(PasswordType.VOID, true) -> {
                tvHeaderTitle.text = StringUtils.getText(configModel.data?.stringFile?.voidAndRefundLabel?.label)
            }
            typePassword.equals(PasswordType.SETTLEMENT, true) -> {
                tvHeaderTitle.text = StringUtils.getText(configModel.data?.stringFile?.settlementLabel?.label)
            }
        }

        tvPasswordTitle.text =  StringUtils.getText(configModel.data?.stringFile?.passwordNewLabel?.label)
        tvVerifyTitle.text =  StringUtils.getText(configModel.data?.stringFile?.passwordVerifyNewLabel?.label)

        setThemePrimaryColor(tvPasswordTitle)
        setThemePrimaryColor(tvVerifyTitle)
        setThemePrimaryColor(btn_ok)
    }

    @OnClick(R.id.btn_ok)
    override fun updatePassword() {

        if((!edPassword.text.isNullOrEmpty() && !edVerify.text.isNullOrEmpty()) && (edPassword.text.toString() == edVerify.text.toString())) {
            when {
                typePassword.equals(PasswordType.MERCHANT, true) -> {
                    PasswordParam.merchant.set(edVerify.text.toString())
                    Toast.makeText(this, StringUtils.getText(configModel.data?.stringFile?.passwordSuccessLabel?.label), Toast.LENGTH_LONG).show()
                    finish()
                }
                typePassword.equals(PasswordType.VOID, true) -> {
                    PasswordParam.void.set(edVerify.text.toString())
                    Toast.makeText(this, StringUtils.getText(configModel.data?.stringFile?.passwordSuccessLabel?.label), Toast.LENGTH_LONG).show()
                    finish()
                }
                typePassword.equals(PasswordType.SETTLEMENT, true) -> {
                    PasswordParam.settlement.set(edVerify.text.toString())
                    Toast.makeText(this, StringUtils.getText(configModel.data?.stringFile?.passwordSuccessLabel?.label), Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }else{
            Toast.makeText(this, StringUtils.getText(configModel.data?.stringFile?.passwordFailLabel?.label), Toast.LENGTH_LONG).show()
        }
    }

    companion object {

        @JvmStatic
        fun start(context: Activity?, type: String?) {
            val intent = Intent(context, PasswordEditActivity::class.java)
            intent.putExtra("type", type)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
        }

    }
}