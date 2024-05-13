package com.evp.payment.ksher.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.evp.payment.ksher.R
import com.evp.payment.ksher.view.dialog.DialogEvent
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.dialog_rx.view.*

class EVPRxDialog(
    val message: String,
    val actionCancel: () -> Unit,
    val actionConfirm: () -> Unit
) :


    DialogFragment() {

    private var notifier: PublishSubject<Int>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        getDialog()!!.getWindow()?.setBackgroundDrawableResource(R.drawable.round_corner);
        return inflater.inflate(R.layout.dialog_rx, container, false)
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.tv_content.text = message
        view.btn_cancel.setOnClickListener {
            notifier?.onNext(DialogEvent.CANCEL)
            notifier?.onComplete()
            this.dismiss()
        }
        view.btn_confirm.setOnClickListener {
            notifier?.onNext(DialogEvent.CONFIRM)
            notifier?.onComplete()
            this.dismiss()
        }
    }

    fun showDialog(supportFragmentManager: FragmentManager, s: String): Single<Int>? {
        super.show(supportFragmentManager, s)

        notifier = PublishSubject.create()

        return notifier?.singleOrError()
    }

}