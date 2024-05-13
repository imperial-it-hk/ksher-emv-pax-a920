package com.evp.payment.ksher.function.qr

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.OnClick
import com.evp.payment.ksher.R
import com.evp.payment.ksher.database.YesterdayTransactionModel
import com.evp.payment.ksher.database.repository.SettlementRepository
import com.evp.payment.ksher.database.repository.SuspendedRepository
import com.evp.payment.ksher.database.repository.TransactionRepository
import com.evp.payment.ksher.database.table.TransDataModel
import com.evp.payment.ksher.extension.*
import com.evp.payment.ksher.function.BaseApplication
import com.evp.payment.ksher.function.main.BaseTimeoutActivity
import com.evp.payment.ksher.function.main.InputPasswordDialog
import com.evp.payment.ksher.function.payment.action.print.PrintActivity
import com.evp.payment.ksher.function.qr.view.GeneratorQRContact
import com.evp.payment.ksher.function.settlement.SettlementActivity
import com.evp.payment.ksher.parameter.ControllerParam
import com.evp.payment.ksher.parameter.OperationalParam
import com.evp.payment.ksher.printing.Receipter
import com.evp.payment.ksher.printing.TransactionPrinting
import com.evp.payment.ksher.utils.DateUtils.getAbbreviatedFromDateTime
import com.evp.payment.ksher.utils.DeviceUtil
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.utils.constant.PasswordType
import com.evp.payment.ksher.utils.constant.PaymentAction
import com.evp.payment.ksher.view.dialog.DialogUtils
import com.evp.payment.ksher.view.progressbar.ProgressNotifier
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_generator_qr.*
import kotlinx.android.synthetic.main.qr_print_layout.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class GeneratorQRActivity : BaseTimeoutActivity(), GeneratorQRContact.View {

    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Inject
    lateinit var suspendedRepository: SuspendedRepository

    @Inject
    lateinit var settlementRepository: SettlementRepository

    private val receipter: Receipter by lazy { Receipter(this@GeneratorQRActivity.applicationContext) }

    private val presenter: GeneratorQRPresenter by lazy {
        GeneratorQRPresenter(
            this,
            transactionRepository,
            suspendedRepository,
            configModel
        )
    }

    @BindView(R.id.layout_menu)
    lateinit var layoutMenu: LinearLayout

    @BindView(R.id.layout_sub_header)
    lateinit var layoutSubHeader: LinearLayout

    @BindView(R.id.qr_image)
    lateinit var qrImage: ImageView

    @BindView(R.id.btn_cancel)
    lateinit var btnCancel: Button

    @BindView(R.id.btn_confirm)
    lateinit var btnConfirm: Button

    @BindView(R.id.tv_sub_header_amount)
    lateinit var tvAmount: AppCompatTextView

    @BindView(R.id.iv_sub_header_channel)
    lateinit var tvTitleImage: AppCompatImageView

    private var bitmap: Bitmap? = null
    private var qrgEncoder: QRGEncoder? = null

    private val payChannel by extraNotNull<String>("pay_channel")
    private val payAmount by extraNotNull<String>("pay_amount")
    private val mediaType by extra<String>("media_type", "")

    var subscribeAutoInquiry: Disposable? = null
    var subscribeEnableCancelButton: Disposable? = null
    var isProcessTransactionDone = false

    private fun checkForceSettlement(action: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                transactionRepository.getYesterdayTransaction().collect { transaction ->
                    Timber.d("transaction : " + transaction?.getOrNull())
                    if (transaction?.getOrNull() == null || (transaction.getOrNull()!!.saleTotalAmount == 0 && transaction.getOrNull()!!.refundTotalAmount == 0)) {
                        CoroutineScope(Dispatchers.Main).launch {
                            action.invoke()
                        }
                    } else {
                        val yesterdayTransaction = transaction.getOrNull()
                        settlementRepository.getLastSettlement().collect { settlement ->
                            if (settlement?.getOrNull() == null) {
                                if (yesterdayTransaction!!.saleTotalAmount > 0 || yesterdayTransaction.refundTotalAmount > 0) {
                                    showForceSettlementDialog()
                                } else {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        action.invoke()
                                    }
                                }
                            } else {
                                val lastSettlement = settlement.getOrNull()
                                if (yesterdayTransaction!!.batchNo?.toInt()!! > lastSettlement!!.batchNo?.toInt() ?: 0)
                                    if (yesterdayTransaction.saleTotalAmount > 0 || yesterdayTransaction.refundTotalAmount > 0) {
                                        showForceSettlementDialog()
                                    } else {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            action.invoke()
                                        }
                                    }
                                else {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        action.invoke()
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
                e.printStackTrace()
                action.invoke()
            }
        }
    }

    private fun showForceSettlementDialog() {
        showDialog(
            msg = StringUtils.getText(configModel.data?.stringFile?.forceSettlementLabel?.label),
            actionCancel = { finish() },
            actionConfirm = {
                val passwordDialog =
                    InputPasswordDialog(
                        this@GeneratorQRActivity,
                        InputPasswordDialog.Builder(
                            this@GeneratorQRActivity,
                            PasswordType.SETTLEMENT,
                            object :
                                InputPasswordDialog.OnPasswordActionListener {
                                override fun onSuccess(
                                    dialogFragment: InputPasswordDialog
                                ) {
                                    SettlementActivity.start(
                                        this@GeneratorQRActivity,
                                        PaymentAction.SETTLEMENT,
                                        false
                                    )
                                    dialogFragment.dismiss()
                                    finish()
                                }

                                override fun onFail(dialogFragment: InputPasswordDialog) {
                                    dialogFragment.dismiss()
                                    finish()
                                }
                            })
                    )
                passwordDialog.show()
            })
    }

    override fun initViews() {
        layoutSubHeader.visibility = View.VISIBLE
        checkForceSettlement {
            presenter.initialTransaction(payChannel, payAmount)
        }
        checkDisplayPrintButton()
        btnPrint.setOnClickListener {
            val view = createScreenShot()
            receipter.shareReceipt(
                view = view,
                viewHeight = resources.getDimension(R.dimen.qr_print).toInt(),
                onSuccess = {
                    Timber.d("INFO : " + it.path)
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, it)
                    ControllerParam.needPrintLastTrans.set(true)
                    Completable.fromAction(ProgressNotifier.getInstance()::show)
                        .andThen(TransactionPrinting().printAnyTrans(bitmap))
                        .doOnComplete {
                            ControllerParam.needPrintLastTrans.set(false)
                        }
                        .doFinally(ProgressNotifier.getInstance()::dismiss)
                        .doOnError { e ->
                            Timber.e(e)
                            e.printStackTrace()
                            DeviceUtil.beepErr()
                            DialogUtils.showAlertTimeout(
                                e.message,
                                DialogUtils.TIMEOUT_FAIL
                            )
                        }
                        .subscribe()


                },
                onError = {
                    Timber.d("ERROR INFO : " + it)
                })
        }

        super.disableCountDown()
        initMenu()
    }


    private fun checkDisplayPrintButton(){
        when {
            "linepay".equals(payChannel, true) -> {
                if(configModel.data?.qrLogo?.get(0)?.display!!){
                    btnPrint.visible()
                }
            }
            "promptpay".equals(payChannel, true) -> {
                if(configModel.data?.qrLogo?.get(1)?.display!!){
                    btnPrint.visible()
                }
            }
            "alipay".equals(payChannel, true) -> {
                if(configModel.data?.qrLogo?.get(2)?.display!!){
                    btnPrint.visible()
                }
            }
            "wechat".equals(payChannel, true) -> {
                if(configModel.data?.qrLogo?.get(3)?.display!!){
                    btnPrint.visible()
                }
            }
            "truemoney".equals(payChannel, true) -> {
                if(configModel.data?.qrLogo?.get(4)?.display!!){
                    btnPrint.visible()
                }
            }
            "airpay".equals(payChannel, true) -> {
                if(configModel.data?.qrLogo?.get(5)?.display!!){
                    btnPrint.visible()
                }
            }
        }

    }

    private fun createScreenShot() =
        View.inflate(this, R.layout.qr_print_layout, null).apply {
            bitmap?.let {
                this.qr_image.setImageBitmap(it)
            }
            val calendar = Calendar.getInstance()
            this.tvDate.text = "DATE: ${calendar.getAbbreviatedFromDateTime("dd/MM/YYYY")}"
            this.tvTime.text = "TIME: ${calendar.getAbbreviatedFromDateTime("HH:mm:ss")}"
//            this.tvBillerId.text = "BILLER ID: ${calendar.getAbbreviatedFromDateTime("HH:mm:ss")}"
            this.tvBillerId.gone()
            this.tvAmtLabel.text = "AMT: THB"
            this.tvAmt.text = StringUtils.toDisplayAmount(payAmount, 2)

            this.iv_logo_mini.invisible()
            when {
//                "linepay".equals(payChannel, true) -> {
//                    this.iv_logo_mini.setImageBitmap(
//                        StringUtils.getImage(
//                            configModel.data?.qrLogo?.get(
//                                0
//                            )?.icon
//                        )
//                    )
//                    this.banner.setImageBitmap(StringUtils.getImage(configModel.data?.qrLogo?.get(0)?.banner))
//                }
                "promptpay".equals(payChannel, true) -> {
//                    this.iv_logo_mini.setImageBitmap(
//                        StringUtils.getImage(
//                            configModel.data?.qrLogo?.get(
//                                1
//                            )?.icon
//                        )
//                    )
                    this.banner.setImageBitmap(StringUtils.getImage(configModel.data?.qrLogo?.get(1)?.banner))
                }
                "alipay".equals(payChannel, true) -> {
//                    this.iv_logo_mini.setImageBitmap(
//                        StringUtils.getImage(
//                            configModel.data?.qrLogo?.get(
//                                2
//                            )?.icon
//                        )
//                    )
                    this.banner.setImageBitmap(StringUtils.getImage(configModel.data?.qrLogo?.get(2)?.banner))
                }
                "wechat".equals(payChannel, true) -> {
//                    this.iv_logo_mini.setImageBitmap(
//                        StringUtils.getImage(
//                            configModel.data?.qrLogo?.get(
//                                3
//                            )?.icon
//                        )
//                    )
                    this.banner.setImageBitmap(StringUtils.getImage(configModel.data?.qrLogo?.get(3)?.banner))
                }
                "truemoney".equals(payChannel, true) -> {
//                    this.iv_logo_mini.setImageBitmap(
//                        StringUtils.getImage(
//                            configModel.data?.qrLogo?.get(
//                                4
//                            )?.icon
//                        )
//                    )
                    this.banner.setImageBitmap(StringUtils.getImage(configModel.data?.qrLogo?.get(4)?.banner))
                }
                "airpay".equals(payChannel, true) -> {
//                    this.iv_logo_mini.setImageBitmap(
//                        StringUtils.getImage(
//                            configModel.data?.qrLogo?.get(
//                                5
//                            )?.icon
//                        )
//                    )
                    this.banner.setImageBitmap(StringUtils.getImage(configModel.data?.qrLogo?.get(5)?.banner))
                }
            }
        }


    override fun loadParam() {
    }

    override val layoutId: Int
        get() = R.layout.activity_generator_qr


    override fun onResume() {
        super.onResume()
        // Disable status bar, home, recent key
        DeviceUtil.setDeviceStatus()
    }

    @OnClick(R.id.layout_back)
    fun onBackClick() {
        onBackPressed()
    }

    private fun startAutoInquiry(isShowProgress: Boolean = false) {
        subscribeAutoInquiry?.dispose()
        val startTimeMillis = System.currentTimeMillis();

        if (isShowProgress) {
            ProgressNotifier.getInstance().show()
            val _countDownSecond: Long =
                (OperationalParam.autoSaleInquiryTimeout.get()?.toLong() ?: 25) + 1
            var countDownSecond = _countDownSecond - 1
            val autoSaleInquiryDelay = OperationalParam.autoSaleInquiryDelay.get() ?: 1
            subscribeAutoInquiry = Observable.timer(1000, TimeUnit.MILLISECONDS)
                .take(_countDownSecond)
                .repeatUntil {
                    System.currentTimeMillis() - startTimeMillis > (_countDownSecond * 1000)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnDispose { ProgressNotifier.getInstance().dismiss() }
                .subscribe {
                    if (countDownSecond <= 0) {
                        subscribeAutoInquiry?.dispose()
                        ProgressNotifier.getInstance().dismiss()
                        presenter.inquiryLastTransactionFinal()
                    }
                    if (countDownSecond >= 0) {
                        ProgressNotifier.getInstance()
                            .primaryContent(StringUtils.getText(configModel.data?.stringFile?.processLabel?.label) + " " + countDownSecond)
                    }

                    countDownSecond--

                    if (countDownSecond.toInt() % autoSaleInquiryDelay == 0) presenter.inquiryLastTransaction()
                }
        } else {
            subscribeAutoInquiry = Observable.timer(5000, TimeUnit.MILLISECONDS)
                .take(12)
                .repeatUntil {
                    System.currentTimeMillis() - startTimeMillis > 60000
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Timber.d("startAutoInquiry : ${System.currentTimeMillis() - startTimeMillis}")
                    presenter.inquiryLastTransaction()
                }
        }
    }

    override fun onPause() {
        super.onPause()
        if (subscribeAutoInquiry?.isDisposed == false) {
            subscribeAutoInquiry?.dispose()
        }
//        if (subscribeEnableCancelButton?.isDisposed == false) {
//            subscribeEnableCancelButton?.dispose()
//        }
    }

    override fun initMenu() {
        if (intent != null) {
            tvAmount.visible()
            tvAmount.text = "${configModel.data?.config?.defaultCurrencyUnit ?: "THB"}  $payAmount"
            btnCancel.text = StringUtils.getText(configModel.data?.button?.buttonCancel?.label)
            btnConfirm.text = StringUtils.getText(configModel.data?.button?.buttonConfirm?.label)

            setThemePrimaryColor(btnConfirm)
            if (!payChannel.isNullOrEmpty()) {
                tvTitleImage.visible()
                setupSubHeaderLogo(tvTitleImage, payChannel)
            }

            if (!intent.getStringExtra("input_qr_generator_intent").isNullOrEmpty()) {
                val inputValue = intent.getStringExtra("input_qr_generator_intent")
                if (inputValue!!.isNotEmpty()) {
                    val manager = getSystemService(WINDOW_SERVICE) as WindowManager
                    val display = manager.defaultDisplay
                    val point = Point()
                    display.getSize(point)
                    val width = point.x
                    val height = point.y
                    var smallerDimension = if (width < height) width else height
                    smallerDimension = smallerDimension * 3 / 4
                    qrgEncoder = QRGEncoder(
                        inputValue, null, QRGContents.Type.TEXT, smallerDimension
                    )
                    qrgEncoder?.colorBlack = ContextCompat.getColor(this, R.color.colorPrimary)
                    qrgEncoder?.colorWhite = Color.WHITE
                    try {
                        bitmap = qrgEncoder?.bitmap
                        qrImage.setImageBitmap(bitmap)
                        setMiniLogoQr()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun setMiniLogoQr(){
        when {
            "linepay".equals(payChannel, true) -> {
                if(configModel.data?.qrLogo?.get(0)?.display!!){
                    iv_logo_mini.setImageBitmap(StringUtils.getImage(configModel.data?.qrLogo?.get(0)?.icon))
                    iv_logo_mini.visible()
                }
            }
            "promptpay".equals(payChannel, true) -> {
                if(configModel.data?.qrLogo?.get(1)?.display!!){
                    iv_logo_mini.setImageBitmap(StringUtils.getImage(configModel.data?.qrLogo?.get(1)?.icon))
                    iv_logo_mini.visible()
                }
            }
            "alipay".equals(payChannel, true) -> {
                if(configModel.data?.qrLogo?.get(2)?.display!!){
                    iv_logo_mini.setImageBitmap(StringUtils.getImage(configModel.data?.qrLogo?.get(2)?.icon))
                    iv_logo_mini.visible()
                }
            }
            "wechat".equals(payChannel, true) -> {
                if(configModel.data?.qrLogo?.get(3)?.display!!){
                    iv_logo_mini.setImageBitmap(StringUtils.getImage(configModel.data?.qrLogo?.get(3)?.icon))
                    iv_logo_mini.visible()
                }
            }
            "truemoney".equals(payChannel, true) -> {
                if(configModel.data?.qrLogo?.get(4)?.display!!){
                    iv_logo_mini.setImageBitmap(StringUtils.getImage(configModel.data?.qrLogo?.get(4)?.icon))
                    iv_logo_mini.visible()
                }
            }
            "airpay".equals(payChannel, true) -> {
                if(configModel.data?.qrLogo?.get(5)?.display!!){
                    iv_logo_mini.setImageBitmap(StringUtils.getImage(configModel.data?.qrLogo?.get(5)?.icon))
                    iv_logo_mini.visible()
                }
            }
        }

    }

    @OnClick(R.id.btn_confirm)
    override fun confirm() {
        disableAndHideCountDown()
        startAutoInquiry(true)
    }

    @OnClick(R.id.btn_cancel)
    override fun cancel() {
        onBackPressed()
    }

    override fun displayAmount() {
//        Toast.makeText(this, "amount", Toast.LENGTH_SHORT).show()
    }

    override fun showQr(qrString: String) {
        qrString.let {
            val manager = getSystemService(WINDOW_SERVICE) as WindowManager
            val display = manager.defaultDisplay
            val point = Point()
            display.getSize(point)
            val width = point.x
            val height = point.y
            var smallerDimension = if (width < height) width else height
            smallerDimension = smallerDimension * 3 / 4
            qrgEncoder = QRGEncoder(
                it, null, QRGContents.Type.TEXT, smallerDimension
            )
            qrgEncoder?.colorBlack = ContextCompat.getColor(this, R.color.colorPrimaryLight)
            qrgEncoder?.colorWhite = Color.WHITE
            try {
                bitmap = qrgEncoder?.bitmap
                qrImage.setImageBitmap(bitmap)
                setMiniLogoQr()
                startCountDown()
                startEnableCancelButtonCountdown()
                startAutoInquiry()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    override fun showQrImage64(string: String) {
        val imageData: ByteArray =
            Base64.decode(string.substring(string.indexOf(",") + 1), Base64.DEFAULT)
        val inputStream: InputStream = ByteArrayInputStream(imageData)
        val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
        qrImage.setImageBitmap(bitmap)
    }

    override fun stopCountdown() {
        super.disableCountDown()
    }

    private fun startEnableCancelButtonCountdown() {
        val startTimeMillis = System.currentTimeMillis();
        var countDownSecond: Long = 11
        subscribeEnableCancelButton = Observable.timer(1000, TimeUnit.MILLISECONDS)
            .take(11)
            .repeatUntil {
                System.currentTimeMillis() - startTimeMillis > (11 * 1000)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete {
                btnCancel.isEnabled = true
                btnCancel.text = getString(R.string.cancel)
            }
            .subscribe {
                Timber.d("enable cancel button after : ${System.currentTimeMillis() - startTimeMillis}")
                countDownSecond--
                btnCancel.text = "${getString(R.string.cancel)} ($countDownSecond)"
            }
    }

    override fun onGenerateQRError(message: String) {
        subscribeAutoInquiry?.dispose()
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onTransactionSuccess(transData: TransDataModel) {
//        if (presenter.isProcessTransactionDone()) return
        if (!mediaType.isNullOrEmpty()) {
            PrintActivity.startWithInvoke(this, mediaType, transData)
        } else {
            PrintActivity.start(this, transData)
        }
    }

    override fun onTransactionFailure(transData: TransDataModel) {
        subscribeAutoInquiry?.dispose()
        if (presenter.isProcessTransactionDone()) return
        showDialog(
            msg = StringUtils.getText(configModel.data?.stringFile?.transactionFailLabel?.label),
            actionConfirm = { finish() },
            actionCancel = { finish() })
    }

    override fun showDialogMessage(msg: String) {
        subscribeAutoInquiry?.dispose()
        if (presenter.isProcessTransactionDone()) return
        showDialog(
            msg = msg,
            actionConfirm = { finish() },
            actionCancel = { finish() })
    }

    override fun onTransactionTimeout(transData: TransDataModel) {
        subscribeAutoInquiry?.dispose()
        if (presenter.isProcessTransactionDone()) return
        Toast.makeText(
            this,
            StringUtils.getText(configModel.data?.stringFile?.transactionTimeoutLabel?.label),
            Toast.LENGTH_LONG
        ).show()
        finish()
    }

    override fun stopAutoInquiry() {
        subscribeAutoInquiry?.dispose()
    }

    companion object {

        @JvmStatic
        fun start(context: Activity?, amount: String, channel: String?) {
            val intent = Intent(context, GeneratorQRActivity::class.java)
            intent.putExtra("pay_amount", StringUtils.toDisplayAmount(amount, 2))
            intent.putExtra("pay_channel", channel)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
            context?.finish()
        }

        @JvmStatic
        fun startWithInvoke(
            context: Activity?,
            amount: String,
            channel: String?,
            mediaType: String
        ) {
            val intent = Intent(context, GeneratorQRActivity::class.java)
            intent.putExtra("pay_amount", StringUtils.toDisplayAmount(amount, 2))
            intent.putExtra("pay_channel", channel)
            intent.putExtra("media_type", mediaType)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
            context?.finish()
        }

    }

    fun cropBorderFromBitmap(bmp: Bitmap): Bitmap {
        //Convenience variables
        var bmp = bmp
        val width = bmp.width
        val height = bmp.height
        val pixels = IntArray(height * width)

        //Load the pixel data into the pixels array
        bmp.getPixels(pixels, 0, width, 0, 0, width, height)
        val length = pixels.size
        val borderColor = pixels[0]

        //Locate the start of the border
        var borderStart = 0
        for (i in 0 until length) {

            // 1. Compare the color of two pixels whether they differ
            // 2. Check whether the difference is significant
            if (pixels[i] != borderColor && !sameColor(borderColor, pixels[i])) {
                borderStart = i
                break
            }
        }

        //Locate the end of the border
        var borderEnd = 0
        for (i in length - 1 downTo 0) {
            if (pixels[i] != borderColor && !sameColor(borderColor, pixels[i])) {
                borderEnd = length - i
                break
            }
        }

        //Calculate the margins
        val leftMargin = borderStart % width
        val rightMargin = borderEnd % width
        val topMargin = borderStart / width
        val bottomMargin = borderEnd / width

        //Create the new, cropped version of the Bitmap
        bmp = Bitmap.createBitmap(
            bmp,
            leftMargin,
            topMargin,
            width - leftMargin - rightMargin,
            height - topMargin - bottomMargin
        )
        return bmp
    }

    private fun sameColor(color1: Int, color2: Int): Boolean {
        // Split colors into RGB values
        val r1 = (color1 and 0xFF).toLong()
        val g1 = (color1 shr 8 and 0xFF).toLong()
        val b1 = (color1 shr 16 and 0xFF).toLong()
        val r2 = (color2 and 0xFF).toLong()
        val g2 = (color2 shr 8 and 0xFF).toLong()
        val b2 = (color2 shr 16 and 0xFF).toLong()
        val dist = (r2 - r1) * (r2 - r1) + (g2 - g1) * (g2 - g1) + (b2 - b1) * (b2 - b1)

        // Check vs. threshold
        return dist < 200
    }
}