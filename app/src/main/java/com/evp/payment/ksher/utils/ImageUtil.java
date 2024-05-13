package com.evp.payment.ksher.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

public class ImageUtil {

    /**
     * Get bitmap of a view
     *
     * @param view source view
     * @return generated bitmap object
     */
    public static Bitmap getBitmapFromView(View view) {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.layout(0, 0, view.getWidth(), view.getHeight());
        Log.d("", "combineImages: width: " + view.getWidth());
        Log.d("", "combineImages: height: " + view.getHeight());
        view.draw(canvas);
        return bitmap;
    }

    /**
     * Stitch two images one below another
     *
     * @param listOfBitmapsToStitch List of bitmaps to stitch
     * @return resulting stitched bitmap
     */
    public static Bitmap combineImages(ArrayList<Bitmap> listOfBitmapsToStitch) {
        Bitmap bitmapResult = null;

        int width = 0, height = 0;

        for (Bitmap bitmap : listOfBitmapsToStitch) {
            width = Math.max(width, bitmap.getWidth());
            height = height + bitmap.getHeight();
        }

        bitmapResult = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImageCanvas = new Canvas(bitmapResult);

        int currentHeight = 0;
        for (Bitmap bitmap : listOfBitmapsToStitch) {
            comboImageCanvas.drawBitmap(bitmap, 0f, currentHeight, null);
            currentHeight = currentHeight + bitmap.getHeight();
        }

        return bitmapResult;
    }
}