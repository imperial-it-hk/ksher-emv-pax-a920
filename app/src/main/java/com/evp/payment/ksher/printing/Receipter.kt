package com.evp.payment.ksher.printing

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ScrollView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.evp.payment.ksher.BuildConfig
import com.evp.payment.ksher.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

open class Receipter constructor(
    private val context: Context
) {

    private var screenshotObs: Disposable? = null

    fun shareReceiptMapper(shareImage: Bitmap): Observable<Uri> {
        return writeImageToFile(shareImage, getShareCacheFile(context))
            ?.let {
                FileProvider.getUriForFile(
                    context,
                    "${BuildConfig.APPLICATION_ID}.fileprovider",
                    it
                )
            }
            ?.run { Observable.just(this) }
            ?: throw Error(SAVE_FILE_FAILED)
    }

    fun shareReceipt(
        view: View,
        onSuccess: (Uri) -> Unit,
        onError: (String) -> Unit,
        viewHeight: Int = 0
    ) {
        getViewShot(view, viewHeight)
            .let { toImageAction(it, this::shareReceiptMapper, onSuccess, onError) }
    }

    fun getShareCacheFile(context: Context): File =
        File("${context.cacheDir.absolutePath}/Pictures", createShareFileName())
            .let { file ->
                deleteNotUseShareFile(file)
                if (!file.parentFile.exists()) {
                    file.parentFile.mkdirs()
                }
                file
            }

    private fun createShareFileName(type: String = "jpg"): String {
        val currentDate = getCurrentDate()
        val fileName = createFileName(currentDate)
        return "${fileName.replace(":", "_").replace(" ", "_")}.$type"
    }

    fun getCurrentDate(): Date {
        val calendar = Calendar.getInstance()
        calendar.timeZone = appTimeZone
        return Date(calendar.timeInMillis)
    }

    private val appTimeZone by lazy {
        TimeZone.getTimeZone("Thailand/Bangkok")
    }


    private fun createFileName(date: Date): String =
        SimpleDateFormat("yyyy_MM_dd HH:mm:ss", Locale.getDefault())
            .let { "EvpDolfin_${it.format(date)}" }

    protected fun getViewShot(screenView: View, viewHeight: Int = 0): Bitmap {
        val widthPixels = Resources.getSystem().displayMetrics.widthPixels
            .also {
                Timber.d("width: $it")
            }
        val heightPixels =
            if (viewHeight != 0) viewHeight else Resources.getSystem().displayMetrics.heightPixels
                .also {
                    Timber.d("height: $it")
                }

        val view = if (screenView is ScrollView) {
            screenView.measure(
                View.MeasureSpec.makeMeasureSpec(widthPixels, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            screenView.getChildAt(0)
        } else {
//            screenView.measure(
//                View.MeasureSpec.makeMeasureSpec(540, View.MeasureSpec.EXACTLY),
//                View.MeasureSpec.makeMeasureSpec(940, View.MeasureSpec.EXACTLY)
//            )
            screenView.measure(
                View.MeasureSpec.makeMeasureSpec(widthPixels, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(heightPixels, View.MeasureSpec.EXACTLY)
            )
            screenView
        }

        val width = view.measuredWidth
        val height = view.measuredHeight

        val bitmapImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmapImage)
        canvas.drawColor(ContextCompat.getColor(context, R.color.colorBackgroundGray))
        screenView.layout(0, 0, width, height)
        screenView.draw(canvas)
        return bitmapImage
    }

    protected open fun getDirectoryPicture(): File? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        else
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

    private fun getPublicDirectoryName(): File =
        File(getDirectoryPicture(), MAIN_DIR)
            .let { file ->
                if (!file.exists()) {
                    file.mkdir()
                }
                file
            }

    private fun deleteNotUseShareFile(file: File) {
        if (file.parentFile.listFiles() != null) {
            for (f in file.parentFile.listFiles()) {
                f.delete()
            }
        }
    }

    private fun createStoreFile(fileName: String, type: String = "jpg"): File {
        val parent = getPublicDirectoryName().absolutePath
        val child = "${fileName.replace(":", "_")}.$type"
        return File(parent, child)
    }

    private fun writeImageToFile(
        bm: Bitmap,
        saveFile: File,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
    ): File? {
        return try {
            saveFile.createNewFile()
            val out = FileOutputStream(saveFile)
            bm.compress(format, IMAGE_QUAILITY, out)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                bm.recycle()
            }
            out.flush()
            out.close()
            saveFile
        } catch (e: Exception) {
            throw Error(e.message)
        }
    }

    protected fun toImageAction(
        bitmap: Bitmap,
        mapper: (Bitmap) -> Observable<Uri>,
        onSuccess: (Uri) -> Unit,
        onError: (String) -> Unit
    ) {
        screenshotObs = mapper(bitmap)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .doAfterTerminate { screenshotObs?.dispose() }
            .subscribe(
                { result: Uri -> onSuccess(result) },
                { onError(it.message ?: CREATE_RECEIPT_FAILED) }
            )
    }




    fun getFileFromUri(fileUri: Uri?): File? {
        fileUri?.let { uri ->
            val fileName = context.contentResolver.getFileName(uri)
            return File(fileName)
        } ?: run {
            return null
        }
    }

    private fun ContentResolver.getFileName(fileUri: Uri): String {
        var name = ""
        try {
            val returnCursor = this.query(fileUri, null, null, null, null)
            if (returnCursor != null) {
                val nameIndex = returnCursor.getColumnIndex(MediaStore.Images.Media.DATA)
                returnCursor.moveToFirst()
                name = returnCursor.getString(nameIndex)
                returnCursor.close()
            }
        } catch (_: Exception){
            name = ""
        }
        return name
    }

    companion object {
        private const val MAIN_DIR = "EvpDolfin"
        private const val CACHE_FILE_NAME = "share_qr_image.png"
        private const val SAVE_FILE_FAILED = "Save file fail."
        private const val CREATE_RECEIPT_FAILED = "Something wrong when create screenshot"
        private const val IMAGE_QUAILITY = 90
    }
}
