package com.evp.payment.ksher.parameter;

import com.evp.payment.ksher.R;
import com.evp.payment.ksher.utils.LanguageSettingUtil;
import com.evp.payment.ksher.utils.StringUtils;
import com.evp.payment.ksher.utils.sharedpreferences.SharedPreferencesUtil;
import com.evp.payment.ksher.utils.tlv.TLVDataListV2;
import com.evp.payment.ksher.utils.tlv.TLVDataV2;
import com.f2prateek.rx.preferences2.Preference;
import com.google.common.base.Strings;
import com.evp.eos.utils.LogUtil;
public class OperationalParam extends AbstractParam {

    private static final String TAG = "OperationalParam";

    private static final String PREFIX = "OperationalParam";

    /**
     * operational parameter version
     */
    public static final Preference<String> version = SharedPreferencesUtil.getString(PREFIX + ".version", "000000");
    /**
     * Whether to update parameter after settlement
     */
    public static final Preference<Boolean> updateAfterSettle = SharedPreferencesUtil.getBoolean(PREFIX + ".updateAfterSettle", true);
    /**
     * The operational parameter data to be updated
     */
    public static final Preference<String> paramsRawData = SharedPreferencesUtil.getString(PREFIX + ".paramsRawData", "");
    /**
     * Whether settlement needs to online
     */
    public static final Preference<Boolean> settleNeedOnline = SharedPreferencesUtil.getBoolean(PREFIX + ".settleNeedOnline", false);
    /**
     * Automatic execution of consumption queries
     */
    public static final Preference<Boolean> autoSaleInquiry = SharedPreferencesUtil.getBoolean(PREFIX + ".autoSaleInquiry", false);
    /**
     * First sale automatic inquiry delay time, unit s
     */
    public static final Preference<Integer> firstAutoSaleInquiryDelay = SharedPreferencesUtil.getInteger(PREFIX + ".firstAutoSaleInquiryDelay", 5);
    /**
     * Sale automatic inquiry interval time, unit s
     */
    public static final Preference<Integer> autoSaleInquiryDelay = SharedPreferencesUtil.getInteger(PREFIX + ".autoSaleInquiryDelay", 1);
    /**
     * Sale automatic inquiry timeout, unit s
     */
    public static final Preference<Integer> autoSaleInquiryTimeout = SharedPreferencesUtil.getInteger(PREFIX + ".autoSaleInquiryTimeout", 25);
    /**
     * The number of request to sale automatic inquiry
     */
    public static final Preference<Integer> autoSaleInquiryRequestCount = SharedPreferencesUtil.getInteger(PREFIX + ".autoSaleInquiryRequestCount", 3);
    /**
     * Sale manual inquiry prompt text (English)
     */
    public static final Preference<String> manualSaleInquiryPromptEn = SharedPreferencesUtil.getString(PREFIX + ".manualSaleInquiryPromptEn", "");
    /**
     * Sale manual inquiry confirm button text (English)
     */
    public static final Preference<String> manualSaleInquiryCheckBtnTextEn = SharedPreferencesUtil.getString(PREFIX + ".manualSaleInquiryCheckBtnTextEn", "");
    /**
     * Sale manual inquiry cancel button text (English)
     */
    public static final Preference<String> manualSaleInquiryCancelBtnTextEn = SharedPreferencesUtil.getString(PREFIX + ".manualSaleInquiryCancelBtnTextEn", "");
    /**
     * Sale manual inquiry prompt text (Thai)
     */
    public static final Preference<String> manualSaleInquiryPromptThai = SharedPreferencesUtil.getString(PREFIX + ".manualSaleInquiryPromptThai", "");
    /**
     * Sale manual inquiry confirm button text (Thai)
     */
    public static final Preference<String> manualSaleInquiryCheckBtnTextThai = SharedPreferencesUtil.getString(PREFIX + ".manualSaleInquiryCheckBtnTextThai", "");
    /**
     * Sale manual inquiry cancel button text (Thai)
     */
    public static final Preference<String> manualSaleInquiryCancelBtnTextThai = SharedPreferencesUtil.getString(PREFIX + ".manualSaleInquiryCancelBtnTextThai", "");

    public static void update() {
        if (Strings.isNullOrEmpty(paramsRawData.get())) return;

        try {
            TLVDataListV2 list = TLVDataListV2.fromBinary(paramsRawData.get());
            for (TLVDataV2 tlv : list.asList()) {
                switch (tlv.getTag()) {
                    case "01":
                        // Default request timeout in seconds (max 3 digits). Also applies to manual sale inquiry request
                        if (StringUtils.INSTANCE.validTimeout(tlv.getValue())) {
                            CommunicationParam.INSTANCE.getConnectTimeout().set(tlv.getValue());
                            CommunicationParam.INSTANCE.getReceiveTimeout().set(tlv.getValue());
                        }
                        break;
                    case "02":
                        // Sale acknowledgement (advice) required (0 = false, 1 = true)
                        break;
                    case "03":
                        // Settlement report mode (1 = online, 2 = clear batch only)
                        settleNeedOnline.set(!"2".equals(tlv.getValue()));
                        break;
                    case "04":
                        // Poll server on startup (0 = false, 1 = true)
                        break;
                    case "05":
                        // Server polling idle time in minutes (1 to 9999 minutes, 0 = disabled)
                        break;
                    case "06":
                        // B-scan-C sale inquiry mode (1 = automatic, 2 = manual)
                        autoSaleInquiry.set("1".equals(tlv.getValue()));
                        break;
                    case "07":
                        // B-scan-C first automatic sale inquiry delay in seconds (can be zero)
                        try {
                            firstAutoSaleInquiryDelay.set(Integer.parseInt(tlv.getValue()));
                        } catch (Exception ignore) {
                        }
                        break;
                    case "08":
                        // B-scan-C second (and later) automatic sale inquiry delay in seconds (can be zero)
                        try {
                            autoSaleInquiryDelay.set(Integer.parseInt(tlv.getValue()));
                        } catch (Exception ignore) {
                        }
                        break;
                    case "09":
                        // B-scan-C automatic sale inquiry timeout in seconds (1 to 180)
                        try {
                            autoSaleInquiryTimeout.set(Integer.parseInt(tlv.getValue()));
                        } catch (Exception ignore) {
                        }
                        break;
                    case "0A":
                        // B-scan-C automatic sale inquiry request count (1 to 20)
                        try {
                            autoSaleInquiryRequestCount.set(Integer.parseInt(tlv.getValue()));
                        } catch (Exception ignore) {
                        }
                        break;
                    case "0B":
                        // B-scan-C manual sale inquiry prompt text in English (0x0A for line breaks)
                        if (!Strings.isNullOrEmpty(tlv.getValue())) {
                            String text = tlv.getValue().replaceAll("0A", "\n");
                            manualSaleInquiryPromptEn.set(text);
                        }
                        break;
                    case "0C":
                        // B-scan-C manual sale inquiry ‘check’ button text in English
                        manualSaleInquiryCheckBtnTextEn.set(tlv.getValue());
                        break;
                    case "0D":
                        // B-scan-C manual sale inquiry ‘cancel’ button text in English
                        manualSaleInquiryCancelBtnTextEn.set(tlv.getValue());
                        break;
                    case "0E":
                        // B-scan-C manual sale inquiry prompt text in Thai (0x0A for line breaks)
                        manualSaleInquiryPromptThai.set(tlv.getValue());
                        break;
                    case "0F":
                        // B-scan-C manual sale inquiry ‘check’ button text in Thai
                        manualSaleInquiryCheckBtnTextThai.set(tlv.getValue());
                        break;
                    case "10":
                        // B-scan-C manual sale inquiry ‘cancel’ button text in Thai
                        manualSaleInquiryCancelBtnTextThai.set(tlv.getValue());
                        break;
                    default:
                        // C-scan-B
                }
            }
        } catch (Exception e) {
            LogUtil.e(TAG, e);
        }

        paramsRawData.set(null);
    }

    public static String getManualSaleInquiryPrompt() {
        String text;
        if (LanguageSettingUtil.THAILAND.equals(SystemParam.Companion.getLanguage().get())) {
            text = manualSaleInquiryPromptThai.get();
        } else {
            text = manualSaleInquiryPromptEn.get();
        }

        if (Strings.isNullOrEmpty(text)) {
            text = AbstractParam.Companion.getStr(R.string.transaction_in_progress_press_enter_to_check_or_cancel_to_cancel);
        }
        return text;
    }

    public static String getManualSaleInquiryCheckBtnText() {
        String text;
        if (LanguageSettingUtil.THAILAND.equals(SystemParam.Companion.getLanguage().get())) {
            text = manualSaleInquiryCheckBtnTextThai.get();
        } else {
            text = manualSaleInquiryCheckBtnTextEn.get();
        }
        if (Strings.isNullOrEmpty(text)) {
            text = AbstractParam.Companion.getStr(R.string.enter);
        }
        return text;
    }

    public static String getManualSaleInquiryCancelBtnText() {
        String text;
        if (LanguageSettingUtil.THAILAND.equals(SystemParam.Companion.getLanguage().get())) {
            text = manualSaleInquiryCancelBtnTextThai.get();
        } else {
            text = manualSaleInquiryCancelBtnTextEn.get();
        }
        if (Strings.isNullOrEmpty(text)) {
            text = AbstractParam.Companion.getStr(R.string.cancel);
        }
        return text;
    }

}
