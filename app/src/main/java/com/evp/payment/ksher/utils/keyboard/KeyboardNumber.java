//package com.evp.payment.ksher.utils.keyboard;
//
//import android.inputmethodservice.Keyboard;
//import android.inputmethodservice.KeyboardView;
//import android.text.Editable;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.EditText;
//
//import com.evp.payment.ksher.R;
//
//public class KeyboardNumber implements KeyboardView.OnKeyboardActionListener {
//
////    private boolean keypadToneEnabled = SystemParam.keypadToneEnabled.get();
//
//    private ViewGroup layoutKeyboard;
//    private EditText editText;
//
//    private KeyboardView keyboardView;
//
//    public KeyboardNumber(ViewGroup layoutKeyboard, EditText editText) {
//        this.layoutKeyboard = layoutKeyboard;
//        this.editText = editText;
//        init();
//    }
//
//    private void init() {
//        layoutKeyboard.removeAllViews();
//        View view = LayoutInflater.from(layoutKeyboard.getContext()).inflate(R.layout.keyboard_view_number, layoutKeyboard);
//        keyboardView = view.findViewById(R.id.keyboard_view);
//        keyboardView.setKeyboard(new Keyboard(layoutKeyboard.getContext(), R.xml.keyboard_number));
//        keyboardView.setEnabled(true);
//        keyboardView.setPreviewEnabled(false);
//        keyboardView.setOnKeyboardActionListener(this);
//    }
//
//    public void show() {
//        if (keyboardView == null) return;
//
//        keyboardView.setVisibility(View.VISIBLE);
//    }
//
//    public void hide() {
//        if (keyboardView == null) return;
//
//        keyboardView.setVisibility(View.GONE);
//    }
//
//    @Override
//    public void onKey(int primaryCode, int[] keyCodes) {
////        if (keypadToneEnabled) {
////            DeviceUtil.beepPrompt();
////        }
//        Editable editable = editText.getEditableText();
//        int selectionStart = editText.getSelectionStart();
//        switch (primaryCode) {
//            case Keyboard.KEYCODE_DELETE:
//                if (editable.length() > 0 && selectionStart > 0) {
//                    editable.delete(selectionStart - 1, selectionStart);
//                }
//                break;
//            case 0:
//                editable.clear();
//                break;
//            default:
//                // 标准 ascii 码表可见字符
//                // Standard ASCII code table for visible characters
//                if (primaryCode == 20 || (primaryCode >= 32 && primaryCode <= 126)) {
//                    editable.insert(selectionStart, Character.toString((char) primaryCode));
//                }
//        }
//    }
//
//    @Override
//    public void onPress(int primaryCode) {
//
//    }
//
//    @Override
//    public void onRelease(int primaryCode) {
//
//    }
//
//    @Override
//    public void onText(CharSequence text) {
//
//    }
//
//    @Override
//    public void swipeLeft() {
//
//    }
//
//    @Override
//    public void swipeRight() {
//
//    }
//
//    @Override
//    public void swipeDown() {
//
//    }
//
//    @Override
//    public void swipeUp() {
//
//    }
//}
