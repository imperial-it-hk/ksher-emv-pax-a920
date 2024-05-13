//package com.evp.payment.ksher.utils.keyboard;
//
//import android.app.Activity;
//import android.view.ViewGroup;
//import android.view.WindowManager;
//import android.view.inputmethod.EditorInfo;
//import android.widget.EditText;
//
//import com.icg.eos.utils.LogUtil;
//
//import java.lang.reflect.Method;
//
//public class KeyboardUtil {
//
//    private static final String TAG = "KeyboardUtil";
//
//    public static void bind(ViewGroup layoutKeyboard, EditText... editTexts) {
//        for (int i = 0, l = editTexts.length; i < l; i++) {
//            bind(layoutKeyboard, editTexts[i]);
//            if (i == 0) {
//                editTexts[i].requestFocus();
//            }
//        }
//    }
//
//    private static void bind(ViewGroup layoutKeyboard, EditText editText) {
//        if (!(editText.getContext() instanceof Activity)) return;
//
//        Activity activity = (Activity) editText.getContext();
//        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//        try {
//            Class<EditText> cls = EditText.class;
//            Method setShowSoftInputOnFocus;
//            setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
//            setShowSoftInputOnFocus.setAccessible(true);
//            setShowSoftInputOnFocus.invoke(editText, false);
//        } catch (Exception e) {
//            LogUtil.e(TAG, e);
//        }
//        editText.setOnFocusChangeListener((v, hasFocus) -> {
//            if (hasFocus) {
//                showKeyboard(layoutKeyboard, editText);
//            }
//        });
//        editText.setOnClickListener((v) -> showKeyboard(layoutKeyboard, editText));
//    }
//
//    private static void showKeyboard(ViewGroup layoutKeyboard, EditText editText) {
//        int inputType = editText.getInputType();
//        if ((inputType & EditorInfo.TYPE_CLASS_NUMBER) == EditorInfo.TYPE_CLASS_NUMBER) {
//            new KeyboardNumber(layoutKeyboard, editText).show();
//        }
//    }
//}
