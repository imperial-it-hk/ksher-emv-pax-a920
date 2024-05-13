package com.evp.eos.utils.qrcode;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.evp.eos.utils.LogUtil;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class QRCodeEncoder {
    private static final String TAG = "QRCodeEncoder";

    private static final Map<EncodeHintType, Object> HINTS = new EnumMap<>(EncodeHintType.class);

    static {
        HINTS.put(EncodeHintType.CHARACTER_SET, "utf-8");
        HINTS.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        HINTS.put(EncodeHintType.MARGIN, 0);
    }

    public static Map<EncodeHintType, Object> getHints() {
        return Collections.unmodifiableMap(HINTS);
    }

    private QRCodeEncoder() {
    }

    /**
     * 同步创建黑色前景色、白色背景色的二维码图片。该方法是耗时操作，请在子线程中调用。
     * Create the qr code image of black foreground color and white background color synchronously. This method is a time-consuming operation, which is called in a child thread.
     *
     * @param content 要生成的二维码图片内容
     *                QR code content
     * @param size    图片宽高，单位为px
     *                Image width and height, unit px
     */
    public static Bitmap syncEncodeQRCode(String content, int size) {
        return syncEncodeQRCode(content, size, Color.BLACK, Color.WHITE, null);
    }

    /**
     * 同步创建指定前景色、白色背景色的二维码图片。该方法是耗时操作，请在子线程中调用。
     * Synchronously create a qr code image with the specified foreground and white background colors. This method is a time-consuming operation, which is called in a child thread.
     *
     * @param content         要生成的二维码图片内容
     *                        QR code content
     * @param size            图片宽高，单位为px
     *                        Image width and height, unit px
     * @param foregroundColor 二维码图片的前景色
     *                        QR code image foreground color
     */
    public static Bitmap syncEncodeQRCode(String content, int size, int foregroundColor) {
        return syncEncodeQRCode(content, size, foregroundColor, Color.WHITE, null);
    }

    /**
     * 同步创建指定前景色、白色背景色、带logo的二维码图片。该方法是耗时操作，请在子线程中调用。
     * Synchronously create the specified foreground color, white background color, with logo two-dimensional code picture. This method is a time-consuming operation, which is called in a child thread.
     *
     * @param content         要生成的二维码图片内容
     *                        QR code content
     * @param size            图片宽高，单位为px
     *                        Image width and height, unit px
     * @param foregroundColor 二维码图片的前景色
     *                        QR code image foreground color
     * @param logo            二维码图片的logo
     *                        QR code image logo
     */
    public static Bitmap syncEncodeQRCode(String content, int size, int foregroundColor, Bitmap logo) {
        return syncEncodeQRCode(content, size, foregroundColor, Color.WHITE, logo);
    }

    /**
     * 同步创建指定前景色、指定背景色、带logo的二维码图片。该方法是耗时操作，请在子线程中调用。
     * Synchronously create the specified foreground color, the specified background color, and the two-dimensional code image with logo. This method is a time-consuming operation, which is called in a child thread.
     *
     * @param content         要生成的二维码图片内容
     *                        QR code content
     * @param size            图片宽高，单位为px
     *                        Image width and height, unit px
     * @param foregroundColor 二维码图片的前景色
     *                        QR code image foreground color
     * @param backgroundColor 二维码图片的背景色
     *                        QR code image background color
     * @param logo            二维码图片的logo
     *                        QR code image logo
     */
    public static Bitmap syncEncodeQRCode(String content, int size, int foregroundColor, int backgroundColor, Bitmap logo) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size, HINTS);
            int[] pixels = new int[size * size];
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    if (matrix.get(x, y)) {
                        pixels[y * size + x] = foregroundColor;
                    } else {
                        pixels[y * size + x] = backgroundColor;
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, size, 0, 0, size, size);
            return addLogoToQRCode(bitmap, logo);
        } catch (Exception e) {
            LogUtil.e(TAG, e);
            return null;
        }
    }

    /**
     * 添加logo到二维码图片上
     * Add logo to qr code image
     */
    private static Bitmap addLogoToQRCode(Bitmap src, Bitmap logo) {
        if (src == null || logo == null) {
            return src;
        }

        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();

        float scaleFactor = srcWidth * 1.0f / 5 / logoWidth;
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2f, srcHeight / 2f);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2f, (srcHeight - logoHeight) / 2f, null);
            canvas.save();
            canvas.restore();
        } catch (Exception e) {
            LogUtil.e(TAG, e);
            bitmap = null;
        }
        return bitmap;
    }

    /**
     * 同步创建条形码图片
     * Create barcode images synchronously
     *
     * @param content  要生成条形码包含的内容
     *                 Bar code content
     * @param width    条形码的宽度，单位px
     *                  Bar code width, unit px
     * @param height   条形码的高度，单位px
     *                 Bar code height, unit px
     * @param textSize 字体大小，单位px，如果等于0则不在底部绘制文字
     *                 Font size, unit px, If it's equal to 0, not draw text at the bottom
     * @return 返回生成条形的位图
     *         Return Bar code bitmap
     */
    public static Bitmap syncEncodeBarcode(String content, int width, int height, int textSize) {
        if (TextUtils.isEmpty(content)) {
            return null;
        }
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 0);

        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.CODE_128, width, height, hints);
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = 0xff000000;
                    } else {
                        pixels[y * width + x] = 0xffffffff;
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            if (textSize > 0) {
                bitmap = showContent(bitmap, content, textSize);
            }
            return bitmap;
        } catch (Exception e) {
            LogUtil.e(TAG, e);
        }

        return null;
    }

    /**
     * 显示条形的内容
     * Show Bbar code content
     *
     * @param barcodeBitmap 已生成的条形码的位图
     *                      Generated Bar code bitmap
     * @param content       条形码包含的内容
     *                      Bar code content
     * @param textSize      字体大小，单位px
     *                      Font size, unit px
     * @return 返回生成的新条形码位图
     *          Return generated new Bar code bitmap
     */
    private static Bitmap showContent(Bitmap barcodeBitmap, String content, int textSize) {
        if (TextUtils.isEmpty(content) || null == barcodeBitmap) {
            return null;
        }
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);
        int textWidth = (int) paint.measureText(content);
        Paint.FontMetrics fm = paint.getFontMetrics();
        int textHeight = (int) (fm.bottom - fm.top);
        float scaleRateX = barcodeBitmap.getWidth() * 1.0f / textWidth;
        if (scaleRateX < 1) {
            paint.setTextScaleX(scaleRateX);
        }
        int baseLine = barcodeBitmap.getHeight() + textHeight;
        Bitmap bitmap = Bitmap.createBitmap(barcodeBitmap.getWidth(), barcodeBitmap.getHeight() + 2 * textHeight, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas();
        canvas.drawColor(Color.WHITE);
        canvas.setBitmap(bitmap);
        canvas.drawBitmap(barcodeBitmap, 0, 0, null);
        canvas.drawText(content, barcodeBitmap.getWidth() / 2f, baseLine, paint);
        canvas.save();
        canvas.restore();
        return bitmap;
    }

}