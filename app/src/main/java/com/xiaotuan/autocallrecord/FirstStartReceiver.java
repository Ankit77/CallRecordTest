package com.xiaotuan.autocallrecord;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class FirstStartReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent service = new Intent(context, RecordService.class);
            service.putExtra(RecordUtils.SWITCH_STATU, 0);
            context.startService(service);
        }
    }
}
