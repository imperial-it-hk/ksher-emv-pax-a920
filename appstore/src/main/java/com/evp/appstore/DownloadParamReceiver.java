package com.evp.appstore;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.evp.eos.utils.LogUtil;

/**
 * Parameter download broadcast
 */
public class DownloadParamReceiver extends BroadcastReceiver {

    private static final String TAG = "DownloadParamReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.d(TAG, "Broadcast received: ACTION_TO_DOWNLOAD_PARAMS");
        Intent serviceIntent = new Intent(context, DownloadParamService.class);
        context.startService(serviceIntent);
    }

}

