package com.evp.payment.ksher.function.history

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import butterknife.BindView
import butterknife.OnClick
import com.evp.payment.ksher.R
import com.evp.payment.ksher.config.ConfigModel
import com.evp.payment.ksher.extension.extra
import com.evp.payment.ksher.function.history.fragment.TransactionHistoryDetailFragment
import com.evp.payment.ksher.function.history.fragment.TransactionHistorySummaryFragment
import com.evp.payment.ksher.function.main.BaseTimeoutActivity
import com.evp.payment.ksher.utils.StringUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class TransactionHistoryActivity : BaseTimeoutActivity() {


    @BindView(R.id.layout_sub_header)
    lateinit var layoutTitle: LinearLayout

    @BindView(R.id.tv_sub_header_title)
    lateinit var tvHeaderTitle: AppCompatTextView


    @BindView(R.id.view_pager)
    lateinit var viewPager: ViewPager2

    @BindView(R.id.tab_layout)
    lateinit var tabLayout: TabLayout

    private lateinit var titles: Array<String>

    private val isInvoke by extra("is_invoke", false)

    override fun initViews() {
        titles = arrayOf(StringUtils.getText(configModel.data?.stringFile?.detailLabel?.label),
            StringUtils.getText(configModel.data?.stringFile?.summaryLabel?.label))

        supportActionBar?.elevation = 0f
        // removing toolbar elevation
        layoutTitle.visibility = View.VISIBLE
        tvHeaderTitle.visibility = View.VISIBLE
        tvHeaderTitle.text = StringUtils.getText(configModel.data?.stringFile?.transactionHistoryLabel?.label)
        viewPager.adapter = ViewPagerFragmentAdapter(this, titles, configModel, isInvoke)
        // attaching tab mediator
        TabLayoutMediator(tabLayout, viewPager) { tab: TabLayout.Tab, position: Int -> tab.text = titles[position]
        }.attach()
    }


    override fun loadParam() {
    }

    override val layoutId: Int
        get() = R.layout.activity_transaction_history

    @OnClick(R.id.layout_back)
    fun onBackClick() {
        onBackPressed()
    }

    private class ViewPagerFragmentAdapter(fragmentActivity: FragmentActivity?, title: Array<String>, configModel: ConfigModel, isInvoke: Boolean?) :
        FragmentStateAdapter(fragmentActivity!!) {
        val titles = title
        val configModels = configModel
        val isInvoke = isInvoke
        @NonNull
        override fun createFragment(position: Int): Fragment {
            when (position) {
                0 -> return TransactionHistoryDetailFragment(configModels, isInvoke!!)
                1 -> return TransactionHistorySummaryFragment(configModels, isInvoke!!)
            }
            return TransactionHistoryDetailFragment(configModels, isInvoke!!)
        }

        override fun getItemCount(): Int {
            return titles.size
        }
    }

    companion object {

        @JvmStatic
        fun start(context: Activity?) {
            val intent = Intent(context, TransactionHistoryActivity::class.java)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
        }

        @JvmStatic
        fun startWithInvoke(context: Activity?) {
            val intent = Intent(context, TransactionHistoryActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("is_invoke", true)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
        }

    }

}