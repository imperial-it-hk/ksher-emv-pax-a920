package com.evp.payment.ksher.function.main

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import butterknife.ButterKnife
import com.evp.eos.EosService
import com.evp.payment.ksher.R
import com.evp.payment.ksher.config.ConfigModel
import com.evp.payment.ksher.function.ActivityLifecycleCollector
import com.evp.payment.ksher.function.BaseApplication
import com.evp.payment.ksher.parameter.SystemParam.Companion.language
import com.evp.payment.ksher.utils.DeviceUtil
import com.evp.payment.ksher.utils.LanguageSettingUtil
import com.evp.payment.ksher.utils.Utils.keepScreenLongLight
import com.evp.payment.ksher.utils.Utils.setDefaultFont
import com.evp.payment.ksher.utils.sharedpreferences.SharedPreferencesUtil
import com.evp.payment.ksher.view.EVPDialog
import com.evp.payment.ksher.view.EVPRxDialog
import com.google.gson.Gson
import com.gyf.immersionbar.ImmersionBar
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity() {
    lateinit var configModel: ConfigModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )

            window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
//        if (ActivityLifecycleCollector.isIsAppRecycled) {
//            // After the application is recycled and resumed from the current activity, the current activity is finished and started from splash.
//            finish()
//            val intent = Intent(BaseApplication.appContext, SplashActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            BaseApplication.appContext?.startActivity(intent)
//            overridePendingTransition(0, 0)
//            return
//        }

        initConfig()
        loadParam()
        setContentView(layoutId)
        ButterKnife.bind(this)
        initStatusBar()
        initViews()
        DeviceUtil.setDeviceStatus()
    }

    fun setThemePrimaryColor(layoutMenu :LinearLayout) {
        layoutMenu.setBackgroundColor(Color.parseColor(configModel.data?.setting?.colorPrimary))
    }

    fun setThemePrimaryColor(layoutMenu :View) {
        layoutMenu.setBackgroundColor(Color.parseColor(configModel.data?.setting?.colorPrimary))
    }

    fun setThemePrimaryColor(textView: TextView) {
        textView.setTextColor(Color.parseColor(configModel.data?.setting?.colorPrimary))
    }

    fun setThemePrimaryColor(button: Button) {

        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        shape.setColor(Color.parseColor(configModel.data?.setting?.colorPrimary))
        shape.setStroke(6, Color.parseColor(configModel.data?.setting?.colorPrimary))
        shape.cornerRadii = floatArrayOf(6f, 6f, 6f, 6f, 6f, 6f, 6f, 6f)
        button.background = shape
    }

    fun setThemePrimaryColor(bg :ConstraintLayout) {
        bg.setBackgroundColor(Color.parseColor(configModel.data?.setting?.colorPrimary))
    }

    fun setThemeSecondaryColor(layoutMenu :ConstraintLayout) {
        layoutMenu.setBackgroundColor(Color.parseColor(configModel.data?.setting?.colorSecondary))
    }

    fun setThemeSecondaryColor(textView: TextView) {
        textView.setTextColor(Color.parseColor(configModel.data?.setting?.colorSecondary))
    }

    fun setThemeSecondaryColor(button: Button) {
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        shape.setColor(Color.parseColor(configModel.data?.setting?.colorPrimary))
        shape.setStroke(6, Color.parseColor(configModel.data?.setting?.colorSecondary))
        shape.cornerRadii = floatArrayOf(6f, 6f, 6f, 6f, 6f, 6f, 6f, 6f)
        button.background = shape
//        button.setBackgroundColor(Color.parseColor(configModel.data?.setting?.colorSecondary))
    }

    fun setThemeSecondaryColor(layoutMenu :LinearLayout) {
        layoutMenu.setBackgroundColor(Color.parseColor(configModel.data?.setting?.colorSecondary))
    }
    /**
     * Initialize config
     */
    protected fun initConfig() {
        // Set language.
        LanguageSettingUtil(this).setLanguage(language.get())
        // Set application font to default size.
        setDefaultFont(applicationContext)
        // Keep screen on.
        keepScreenLongLight(this, isKeepScreenOn)

        val json = SharedPreferencesUtil.getString("config_file", "")
        configModel = Gson().fromJson(json.get().toString(), ConfigModel::class.java)
//        SharedPreferencesUtil.getString("config_setting_language", "").set(configModel.data?.setting?.language)

    }

    /**
     * Keep screen on
     */
    protected val isKeepScreenOn: Boolean
        protected get() = true

    /**
     * Init status bar
     */
    protected fun initStatusBar() {
        try {
            val immersionBar = ImmersionBar.with(this)
            // Immersion status bar
            val statusBarView = findViewById<View>(R.id.v_status_bar)
            if (statusBarView != null) {
                immersionBar.statusBarView(statusBarView)
            } else {
                immersionBar.fitsSystemWindows(true)
                immersionBar.statusBarColor(configModel.data?.setting?.colorPrimary?.toInt()!!)
            }
            // Status font dark
            immersionBar.statusBarDarkFont(isStatusBarFontDark)
            // Navigation bar background color
            if (isNavBarTransparent) {
                immersionBar.transparentNavigationBar()
            } else {
                immersionBar.navigationBarColor(navigationBarColor)
            }
            immersionBar.init()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Status bar font color dark
     */
    protected val isStatusBarFontDark: Boolean
        protected get() = false

    /**
     * Navigation bar transparent
     */
    protected open val isNavBarTransparent: Boolean
        protected get() = false

    /**
     * Navigation bar color
     */
    protected val navigationBarColor: Int
        protected get() = R.color.colorPrimaryDark

    /**
     * Load invoke parameter
     */
    protected abstract fun loadParam()

    /**
     * Get layout file id.
     */
    protected abstract val layoutId: Int

    /**
     * Initialize view
     */
    protected abstract fun initViews()

    /**
     * Show system keyboard
     */
    protected fun showSystemKeyBoard() {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    /**
     * Hide system keyboard
     */
    protected fun hideSystemKeyBoard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
    }

    fun showDialog(msg: String, actionCancel: () -> Unit, actionConfirm: () -> Unit) {
        EVPDialog(msg, actionCancel, actionConfirm).show(supportFragmentManager, "custom_dialog")
    }

    fun showRxDialog(msg: String, actionCancel: () -> Unit, actionConfirm: () -> Unit) {
        EVPRxDialog(msg, actionCancel, actionConfirm).showDialog(
            supportFragmentManager,
            "custom_dialog"
        )
    }

}