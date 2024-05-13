package com.evp.payment.ksher.view.dialog

import com.evp.payment.ksher.R
import com.evp.payment.ksher.utils.StringUtils.getString

internal class DialogParameter {
    var content: String? = null
        private set
    var isCancelEnabled = true
        private set
    var cancelText = getString(R.string.cancel)
        private set
    var isConfirmEnabled = true
        private set
    var confirmText = getString(R.string.confirm)
        private set

    /**
     * Display countdown on confirm button, 0 means not display countdown
     */
    var countDown = 0
        private set
    var timeout = 0
        private set

    fun setContent(content: String?): DialogParameter {
        this.content = content
        return this
    }

    fun setCancelEnabled(cancelEnabled: Boolean): DialogParameter {
        isCancelEnabled = cancelEnabled
        return this
    }

    fun setCancelText(cancelText: String): DialogParameter {
        this.cancelText = cancelText
        return this
    }

    fun setConfirmEnabled(confirmEnabled: Boolean): DialogParameter {
        isConfirmEnabled = confirmEnabled
        return this
    }

    fun setConfirmText(confirmText: String): DialogParameter {
        this.confirmText = confirmText
        return this
    }

    fun setCountDown(countDown: Int): DialogParameter {
        this.countDown = countDown
        return this
    }

    fun setTimeout(timeout: Int): DialogParameter {
        this.timeout = timeout
        return this
    }
}