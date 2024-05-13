package com.evp.appstore;

import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.Nullable;

import com.evp.eos.utils.LogUtil;
import com.pax.market.android.app.sdk.StoreSdk;
import com.pax.market.android.app.sdk.util.NotificationUtils;
import com.pax.market.api.sdk.java.base.dto.DownloadResultObject;
import com.pax.market.api.sdk.java.base.exception.NotInitException;

import java.io.File;

/**
 * Parameter download service
 */
public class DownloadParamService extends IntentService {

    private static final String TAG = "DownloadParamService";

    public DownloadParamService() {
        super(TAG);
    }

    protected void onHandleIntent(@Nullable Intent intent) {
        //Specifies the download path for the parameter file, you can replace the path to your app's internal storage for security.
        //todo Call this method to download into your specific directory, you can add some log here to monitor
        DownloadResultObject downloadResult = null;
        try {
            deleteOldFile();
            downloadResult = StoreSdk.getInstance().paramApi().downloadParamToPath(getApplication().getPackageName(), getVersion(), DownloadParamManager.getInstance().getSaveFilePath());
        } catch (NotInitException e) {
            LogUtil.e(TAG, "e:" + e);
        }

        //businesscode==0, means download successful, if not equal to 0, please check the return message when need.
        if(downloadResult != null && downloadResult.getBusinessCode()==0){
            LogUtil.i(TAG, "download successful");
            //file download to saveFilePath above.
            //todo can start to add your logic.
        }else{
            //todo check the Error Code and Error Message for fail reason
            LogUtil.e(TAG, "ErrorCode: "+downloadResult.getBusinessCode()+"ErrorMessage: "+downloadResult.getMessage());
        }
    }

//    @Override
//    protected void onHandleIntent(@Nullable Intent intent) {
//        try {
//            DownloadResultObject result = StoreSdk.getInstance().paramApi().downloadParamToPath(getApplication().getPackageName(), getVersion(),
//                    DownloadParamManager.getInstance().getSaveFilePath());
//            LogUtil.i(TAG, "downloadParamToPath code=" + result.getBusinessCode() + ", msg=" + result.getMessage());
//        } catch (NotInitException e) {
//            LogUtil.e(TAG, "downloadParamToPath failed", e);
//        }
//    }


    private void deleteOldFile(){
        try {
            DownloadParamManager.getInstance().deleteParamFile("app-evpksher-params.p");

            File dir = new File(DownloadParamManager.getInstance().getSaveFilePath());
            File[] files = dir.listFiles();

            for (File file : files) {
                if (file.getName().endsWith(".zip")) {
                    if (file.delete()) {
                        System.out.println("file  zip Deleted :");
                    } else {
                        System.out.println("file  zip not Deleted :");
                    }
                }

                if (file.getName().endsWith(".json")) {
                    if (file.delete()) {
                        System.out.println("file  zip Deleted :");
                    } else {
                        System.out.println("file  zip not Deleted :");
                    }
                }
            }

        } catch (Exception e) {
            LogUtil.e(TAG, e);
        }
    }


    private int getVersion() {
        try {
            PackageManager manager = getPackageManager();
            PackageInfo packageInfo = manager.getPackageInfo(getPackageName(), 0);
            if (packageInfo != null) {
                return packageInfo.versionCode;
            }
        } catch (Exception e) {
            LogUtil.w(TAG, e.getMessage());
        }
        return 0;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationUtils.showForeGround(this, "Downloading params");
        return super.onStartCommand(intent, flags, startId);
    }
}
