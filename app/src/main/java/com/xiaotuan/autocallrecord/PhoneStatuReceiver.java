package com.xiaotuan.autocallrecord;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneStatuReceiver extends BroadcastReceiver {
    private static final String RECORD_ACTION = "com.tengruifeng.autocallrecord.okrecord";
    private static final String TAG = "PhoneStatuReceiver";
    private static int mCallSimId;
    private static String phoneNumber;
    private TelephonyManager telephonyManager;

    static {
        phoneNumber = null;
        mCallSimId = 1;
    }

    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceiver()=> action: " + intent.getAction());
        Intent service = new Intent(context, RecordService.class);
        this.telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            phoneNumber = intent.getStringExtra("android.intent.extra.PHONE_NUMBER");
            Log.d(TAG, "new outgoing call=> phoneNumber: " + phoneNumber);
        }
        if (RECORD_ACTION.equals(intent.getAction())) {
            Log.d(TAG, "onReceiver() => phoneNumber: " + phoneNumber);
            service.putExtra("startRecord", true);
            service.putExtra("number", phoneNumber);
            context.startService(service);
        }
        switch (this.telephonyManager.getCallState()) {
            case RecordUtils.PHONE /*0*/:
                Log.d(TAG, "call state idle: start cancle service");
                service.putExtra("startRecord", false);
                service.putExtra("simId", mCallSimId);
                service.putExtra("number", phoneNumber);
                context.startService(service);
            case RecordUtils.SDCARD /*1*/:
                phoneNumber = intent.getStringExtra("incoming_number");
                Log.d(TAG, "incoming call => phoneNumber: " + phoneNumber);
            default:
        }
    }
}
