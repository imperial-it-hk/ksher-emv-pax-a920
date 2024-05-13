package com.evp.appstore;

import android.content.Context;
import android.os.RemoteException;
import android.widget.Toast;

import com.evp.eos.utils.LogUtil;
import com.google.gson.JsonParseException;
import com.pax.market.android.app.sdk.BaseApiService;
import com.pax.market.android.app.sdk.StoreSdk;
import com.pax.market.api.sdk.java.base.exception.NotInitException;
import com.pax.market.api.sdk.java.base.exception.ParseXMLException;

import java.io.File;
import java.util.Map;

/**
 * Parameter download manager
 */
public class DownloadParamManager {

    private static final String TAG = "DownloadParamManager";

    private static DownloadParamManager instance;
    private boolean isReadyToUpdate = true;
    private Context context;
    /**
     * Parameter file save path
     */
    private String saveFilePath;

    public static synchronized DownloadParamManager getInstance() {
        if (instance == null) {
            instance = new DownloadParamManager();
        }
        return instance;
    }

    public void init(Context context, String appKey, String appSecret, String sn) {
        this.context = context;
        this.saveFilePath = context.getFilesDir() + File.separator + "ParamDownload/";
        LogUtil.d(TAG, "saveFilePath" + saveFilePath);

        StoreSdk.getInstance().init(context, appKey, appSecret, new BaseApiService.Callback() {
            @Override
            public void initSuccess() {
                LogUtil.d(TAG, "StoreSdk init success");
                initInquirer();
            }

            @Override
            public void initFailed(RemoteException e) {
                Toast.makeText(context, "Cannot get API URL from PAXSTORE, Please install PAXSTORE first.", Toast.LENGTH_LONG).show();
                LogUtil.e(TAG, "StoreSdk init failed", e);
            }
        });
    }

    /**
     * Parse parameter file
     */
    public Map<String, String> parseParamFile(String fileName) {
        Map<String, String> map = null;
        try {
            File file = new File(saveFilePath + fileName);
            if (file.exists() && file.isFile()) {
                map = StoreSdk.getInstance().paramApi().parseDownloadParamXml(file);
                LogUtil.d(TAG, "map" + map);
            }
        } catch (ParseXMLException | NotInitException e) {
            LogUtil.e(TAG, e);
        }
        return map;
    }

    /**
     * Delete parameter file
     */
    public void deleteParamFile(String fileName) {
        try {
            File file = new File(saveFilePath + fileName);
            if (!file.delete()) {
                LogUtil.w(TAG, "Delete parameter file failed");
            }
        } catch (Exception e) {
            LogUtil.e(TAG, e);
        }
    }

    public String getSaveFilePath() {
        return saveFilePath;
    }

    public void setSaveFilePath(String saveFilePath) {
        this.saveFilePath = saveFilePath;
    }

    private void initInquirer() {
        LogUtil.d(TAG, "initInquirer");
        StoreSdk.getInstance().initInquirer(new StoreSdk.Inquirer() {
            @Override
            public boolean isReadyUpdate() {
                LogUtil.d(TAG, "call business function....isReadyUpdate = " + isReadyToUpdate);
                //todo call your business function here while is ready to update or not
                return isReadyToUpdate;
            }
        });
    }

    public boolean isReadyToUpdate() {
        return isReadyToUpdate;
    }

    public void setReadyToUpdate(boolean readyToUpdate) {
        isReadyToUpdate = readyToUpdate;
        if (isReadyToUpdate) {
            Toast.makeText(context, "Ready to update", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Not ready to update", Toast.LENGTH_SHORT).show();
        }
    }
}
