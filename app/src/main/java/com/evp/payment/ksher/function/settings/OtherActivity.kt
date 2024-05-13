package com.evp.payment.ksher.function.settings

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import butterknife.BindView
import butterknife.OnClick
import com.evp.payment.ksher.R
import com.evp.payment.ksher.customview.gridmenu.GridMenu
import com.evp.payment.ksher.function.main.BaseTimeoutActivity
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.view.progressbar.ProgressNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL


class OtherActivity : BaseTimeoutActivity(){

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
        get() = R.layout.activity_other

    @OnClick(R.id.layout_back)
    fun onBackClick() {
        onBackPressed()
    }

    private fun initMenu() {
        tvHeaderTitle.text = StringUtils.getText(configModel.data?.button?.buttonOther?.label)
        val menuView: View = GridMenu.Builder(applicationContext)
            .addAction(configModel.data?.menusOther?.get(0)?.display,
                StringUtils.getText(configModel.data?.menusOther?.get(0)?.label), StringUtils.getImage(configModel.data?.menusOther?.get(0)?.icon) ) { echoTestFunction() }.create()
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
        )
        layoutMenu.addView(menuView, params)

    }

    private fun echoTestFunction() {
        CoroutineScope(Dispatchers.IO).launch {
            ProgressNotifier.getInstance().show()
            ProgressNotifier.getInstance().primaryContent(StringUtils.getText(configModel.data?.stringFile?.processLabel?.label))
            runTest()
        }
    }

    private fun runTest(){
        val url = URL("https://api.mch.ksher.net")
        val urlc: HttpURLConnection = url.openConnection() as HttpURLConnection

        try {
            urlc.setRequestProperty("User-Agent", "Android Application")
            urlc.setRequestProperty("Connection", "close")
            urlc.connectTimeout = 1000 * 30 // mTimeout is in seconds
            urlc.connect()

            CoroutineScope(Dispatchers.Main).launch {
                ProgressNotifier.getInstance().dismiss()
            }
                if (urlc.responseCode == 200) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(this@OtherActivity, "Success Connection", Toast.LENGTH_LONG)
                            .show()
                    }
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(this@OtherActivity, "Fail Connection", Toast.LENGTH_LONG)
                            .show()
                    }
                }

        } catch (e1: MalformedURLException) {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(this@OtherActivity, "Fail Connection", Toast.LENGTH_LONG).show()
                ProgressNotifier.getInstance().dismiss()
            }
            e1.printStackTrace()
        } catch (e: IOException) {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(this@OtherActivity, "Fail Connection", Toast.LENGTH_LONG).show()
                ProgressNotifier.getInstance().dismiss()
            }
            e.printStackTrace()
        }
    }

    companion object {

        @JvmStatic
        fun start(context: Activity?) {
            val intent = Intent(context, OtherActivity::class.java)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
        }

    }
}