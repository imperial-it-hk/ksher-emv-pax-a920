

package com.evp.payment.ksher.parameter;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RawRes;

import com.evp.eos.utils.LogUtil;
import com.evp.payment.ksher.R;
import com.evp.payment.ksher.config.ConfigModel;
import com.evp.payment.ksher.utils.StringUtils;
import com.evp.payment.ksher.utils.ToastUtils;
import com.evp.payment.ksher.utils.ZipIO;
import com.evp.payment.ksher.utils.alarm.Util;
import com.evp.payment.ksher.utils.sharedpreferences.SharedPreferencesUtil;
import com.f2prateek.rx.preferences2.Preference;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.evp.appstore.DownloadParamManager;
import com.google.gson.JsonParseException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.reactivex.Completable;
import io.reactivex.Single;

public class AppStoreParam extends AbstractParam {

    private static final String TAG = "AppStoreParam";

    private static final String PREFIX = "AppStoreParam";

    /**
     * Pax store App Key
     */
    private static final String APP_KEY = "R0HF4N50Y9MNQ1VGWDWN";
    /**
     * Pax store App Secret
     */
    private static final String APP_SECRET = "SZ8JVKJXLJON6F6OHS6TW0T5RLXV8JJJ7H3O76L4";
    /**
     * Terminal serial number
     */
    private static final String SN = Build.SERIAL;

    private static final String paramFileName = "app-evpksher-params.p";

    /**
     * Parameter file version number
     */
    public static final Preference<String> paramFileVersion = SharedPreferencesUtil.getString(PREFIX + ".paramFileVersion", "");

    /**
     * The key of the parameter needs to be updated. If the keyList is empty, all of parameter will be updated; otherwise, only the parameters corresponding to the key are updated
     */
    private static List<String> updateKeyList = new ArrayList<>();
    private static Context contexts;

    public static void init(Context context) {
        contexts = context;
        DownloadParamManager.getInstance().init(context, APP_KEY, APP_SECRET, SN);
    }

    public static String readRawResource(Context context, @RawRes int res) {
        return readStream(context.getResources().openRawResource(res));
    }

    private static String readStream(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }


    public static String updateParams(Boolean isForceLoadConfig) {
//        try {
//            String configVersionFile = null;
//            try {
//                configVersionFile = new JSONObject(readRawResource(contexts, R.raw.config)).getJSONObject("data").getJSONObject("setting").optString("configVersion");
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            if (isForceLoadConfig || !SystemParam.getConfigVersion().get().equalsIgnoreCase(configVersionFile)) {
//                SharedPreferencesUtil.getString("config_file", "").set(readRawResource(contexts, R.raw.config));
//                if (SharedPreferencesUtil.getString("config_file", "").get().length() > 0) {
//                    ConfigModel configModel = new Gson().fromJson(SharedPreferencesUtil.getString("config_file", "").get(), ConfigModel.class);
//
//                    setSystemParam(configModel);
//                    setMerchantParam(configModel);
//                    setPasswordParam(configModel);
//                    setResourceParam(configModel);
//
//                    SystemParam.getConfigName().set(getConfigName());
//                    ToastUtils.INSTANCE.showMessage(StringUtils.INSTANCE.getText(configModel.getData().getStringFile().getUpdateSuccessLabel().getLabel()));
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return e.getMessage();
//        }
        try {

            Map<String, String> params = DownloadParamManager.getInstance().parseParamFile(paramFileName);
            if (params == null) {
                return "FAIL";
            }
            // Check version.
            if (shouldUpdateParameter(params)|| isForceLoadConfig) {
//                if(isUnzip()) {
                initResource();
//                }

                String configFile = readFromFile(contexts.getFilesDir().toString() + File.separator + "ParamDownload" + File.separator + getConfigName());
                String configVersionFile = new JSONObject(configFile).getJSONObject("data").getJSONObject("setting").optString("configVersion");

                if (shouldUpdateParameter(params) || isForceLoadConfig || !SystemParam.getConfigVersion().get().equalsIgnoreCase(configVersionFile)) {
                    SharedPreferencesUtil.getString("config_file", "").set(configFile);
                    if (SharedPreferencesUtil.getString("config_file", "").get().length() > 0) {
                        ConfigModel configModel = new Gson().fromJson(SharedPreferencesUtil.getString("config_file", "").get(), ConfigModel.class);

                        setSystemParam(configModel);
                        setMerchantParam(configModel);
                        setPasswordParam(configModel);
                        setResourceParam(configModel);
                        setAutoSettlementTime();

                        SystemParam.getConfigName().set(getConfigName());
                        paramFileVersion.set(params.get("key_version"));

                        // Update parameter success, delete parameter file
                        ToastUtils.INSTANCE.showMessage(StringUtils.INSTANCE.getText(configModel.getData().getStringFile().getUpdateSuccessLabel().getLabel()));
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            return "FAIL";
        }
        return "SUCCESS";
    }

    private static void setAutoSettlementTime() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                // your async action
                Util.alermJob(contexts);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                // update the UI (this is executed on UI thread)
                super.onPostExecute(aVoid);
            }
        }.execute();
    }

    public static String readFromFile(String path) {
        String ret = "";
        try {
            InputStream inputStream = new FileInputStream(new File(path));

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }
                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("FileToJson", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("FileToJson", "Can not read file: " + e.toString());
        }
        return ret;
    }

    private static void setSystemParam(ConfigModel configModel) {
        try {
            SystemParam.getAppIdOffline().set(configModel.getData().getSetting().getAppIdOffline());
            SystemParam.getAppIdOnline().set(configModel.getData().getSetting().getAppIdOnline());
            SystemParam.getTokenOffline().set(configModel.getData().getSetting().getTokenOffline());
            SystemParam.getTokenOnline().set(configModel.getData().getSetting().getTokenOnline());
            SystemParam.getPaymentDomain().set(configModel.getData().getSetting().getPaymentDomain());
            SystemParam.getGateWayDomain().set(configModel.getData().getSetting().getGateWayDomain());
            SystemParam.getPublicKey().set(configModel.getData().getSetting().getPublicKey());
            SystemParam.getTrueMoneyQRTime().set(configModel.getData().getConfig().getTrueMoneyQRTime());
            SystemParam.getPrpmptPayQRTime().set(configModel.getData().getConfig().getPrpmptPayQRTime());

            SystemParam.getConfigVersion().set(configModel.getData().getSetting().getConfigVersion());
            SystemParam.getConnectionTimeout().set(configModel.getData().getConfig().getConnectionTimeout());
            SystemParam.getTransactionTimeout().set(configModel.getData().getConfig().getTransactionTimeout());
            SystemParam.getLanguage().set(configModel.getData().getSetting().getLanguage());
            SystemParam.getSystemMaxTransNumberDefault().set(configModel.getData().getSetting().getSystemMaxTransNumberDefault());
            SystemParam.getSystemPrintGrayDefault().set(configModel.getData().getSetting().getSystemPrintGrayDefault());
        } catch (Exception e) {

        }
    }

    private static void setMerchantParam(ConfigModel configModel) {
        try {
            MerchantParam.getStoreId().set(configModel.getData().getSetting().getStoreId());
            MerchantParam.getAddress().set(configModel.getData().getSetting().getAddress());
            MerchantParam.getApn().set(configModel.getData().getConfig().getApn());
            MerchantParam.getName().set(configModel.getData().getConfig().getAcquirer());
            MerchantParam.getMerchantId().set(configModel.getData().getConfig().getMerchantId());
            TerminalParam.getNumber().set(configModel.getData().getConfig().getTerminalId());
        } catch (Exception e) {

        }
    }

    private static void setPasswordParam(ConfigModel configModel) {
        try {
            PasswordParam.getAdmin().set(configModel.getData().getPassword().getPasswordAdmin());
            PasswordParam.getMerchant().set(configModel.getData().getPassword().getPasswordMerchant());
            PasswordParam.getSettlement().set(configModel.getData().getPassword().getPasswordSettlement());
            PasswordParam.getVoid().set(configModel.getData().getPassword().getPasswordVoidAndRefund());
        } catch (Exception e) {

        }

    }

    private static void setResourceParam(ConfigModel configModel) {
        try {
            ResourceParam.getPrintLogoFileName().set(configModel.getData().getSetting().getLogo());
            ResourceParam.getPromotionFileName().set(configModel.getData().getFooter().getIcon());
            ResourceParam.getDisclaimerTxt().set(configModel.getData().getFooter().getDisclaimerTxt());
            ResourceParam.getLabelFooter().set(configModel.getData().getFooter().getLabelFooter());
        } catch (Exception e) {

        }
    }

    private static boolean shouldUpdateParameter(Map<String, String> params) {
        String oldVersion = paramFileVersion.get();
        String newVersion = params.get("key_version");
        return !Strings.isNullOrEmpty(newVersion) && newVersion.compareTo(oldVersion) > 0;
    }

    //
    private static void setNeedUpdateKey(Map<String, String> params) {
        updateKeyList.clear();
        if (params.containsKey("key_config_file")) {
            String keyStr = params.get("key_config_file");
            if (!Strings.isNullOrEmpty(keyStr)) {
                String[] keys = keyStr.split("[;|,]");
                updateKeyList.addAll(Arrays.asList(keys));
            }
        }
    }

    private static String getConfigName() {
        try {
            File dir = new File(contexts.getFilesDir().toString() + File.separator + "ParamDownload/");
            File[] files = dir.listFiles();

            for (File file : files) {
                if (file.getName().startsWith("config")) {
                    return file.getName();
                }
            }

        } catch (Exception e) {
            LogUtil.e(TAG, e);
            return "";
        }
        return "";
    }

    private static String getResourceName() {
        try {
            File dir = new File(contexts.getFilesDir().toString() + File.separator + "ParamDownload/");
            File[] files = dir.listFiles();

            for (File file : files) {
                if (file.getName().startsWith("resource_")) {
                    return file.getName();
                }
            }

        } catch (Exception e) {
            LogUtil.e(TAG, e);
            return "";
        }
        return "";
    }

    private static boolean isUnzip() {
        try {
            File file = new File(contexts.getFilesDir().toString() + File.separator + "ParamDownload/resource");
            if (file.exists() && file.isFile()) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            LogUtil.e(TAG, e);
            return true;
        }
    }

    private static void initResource() {
        try {
            File file = new File(contexts.getFilesDir().toString() + File.separator + "ParamDownload" + File.separator + getResourceName());
            if (file.exists() && file.isFile()) {
                SystemParam.getResourceName().set(getResourceName());
                unzip(file, new File(contexts.getFilesDir().toString() + File.separator + "ParamDownload/"));
                LogUtil.d(TAG, "unzip success");
            }
        } catch (Exception e) {
            LogUtil.e(TAG, e);
        }
    }

    private static void unzip(File zipFile, File targetDirectory) throws IOException {
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
        try {
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs()) {
                    throw new FileNotFoundException("Failed to ensure directory: " +
                            dir.getAbsolutePath());
                }
                if (ze.isDirectory()) { continue; }
                FileOutputStream fout = new FileOutputStream(file);
                try {
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                } finally {
                    fout.close();
                }
            /* if time should be restored as well
            long time = ze.getTime();
            if (time > 0)
                file.setLastModified(time);
            */
            }
        } finally {
            zis.close();
        }
    }

}