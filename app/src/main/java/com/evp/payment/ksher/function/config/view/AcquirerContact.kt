package com.evp.payment.ksher.function.config.view

interface AcquirerContact {
    interface View {
        fun initMenu()
        fun acquirerFunction()
        fun terminalIDFunction()
        fun merchantIDFunction()
        fun batchNoFunction()
    }
}