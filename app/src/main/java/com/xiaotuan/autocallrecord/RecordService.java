package com.xiaotuan.autocallrecord;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import com.mediatek.telephony.TelephonyManagerEx;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RecordService extends Service {
    private static final boolean LOGDEBUG = false;
    private static final int MSG_CANCLE_RECORD = 4;
    private static final int MSG_GONE = 2;
    private static final int MSG_START_RECORD = 3;
    private static final int MSG_VISIBLE = 1;
    private static final int NEW_OUTGOING_CALL = 3;
    private static final String[] PHONE_PROJECTION;
    private static final String SIM1_PRE = "SIM1_Time";
    private static final String SIM2_PRE = "SIM2_Time";
    private static final String TAG = "RecordService";
    private static final boolean TOASTDEBUG = false;
    private static boolean startRecord;
    private BroadcastReceiver dialpadReceiver;
    private boolean isAdd;
    private boolean isDialpadShow;
    private int location;
    private int mCallSimId;
    private Handler mHandler;
    private View mMainView;
    private int mRecordMarkMarginTop;
    private WindowManager mWm;
    private View market;
    private MediaRecorder mediaRecorder;
    private LayoutParams f0p;
    private String path;
    private String phoneNumber;
    private boolean recordSwitch;
    private boolean recording;
    private int simId;
    private int statu;
    private TelephonyManager telephonyManager;

    /* renamed from: com.xiaotuan.autocallrecord.RecordService.1 */
    class C00141 extends Handler {
        C00141() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RecordService.MSG_VISIBLE /*1*/:
                    if (RecordService.this.recording && RecordService.this.inCallScreenisTop() && !RecordService.this.isDialpadShow) {
                        RecordService.this.updateView(true);
                    } else {
                        RecordService.this.updateView(RecordService.LOGDEBUG);
                    }
                    RecordService.this.mHandler.removeMessages(RecordService.MSG_VISIBLE);
                    Message gone = new Message();
                    gone.what = RecordService.MSG_GONE;
                    if (RecordService.this.recording) {
                        RecordService.this.mHandler.sendMessageDelayed(gone, 500);
                    }
                case RecordService.MSG_GONE /*2*/:
                    RecordService.this.updateView(RecordService.LOGDEBUG);
                    RecordService.this.mHandler.removeMessages(RecordService.MSG_GONE);
                    Message visible = new Message();
                    visible.what = RecordService.MSG_VISIBLE;
                    if (RecordService.this.recording) {
                        RecordService.this.mHandler.sendMessageDelayed(visible, 500);
                    }
                case RecordService.NEW_OUTGOING_CALL /*3*/:
                    if (!RecordService.this.recording) {
                        RecordService.this.startRecording();
                    }
                case RecordService.MSG_CANCLE_RECORD /*4*/:
                    RecordService.this.cancleRecord();
                default:
            }
        }
    }

    /* renamed from: com.xiaotuan.autocallrecord.RecordService.2 */
    class C00152 extends BroadcastReceiver {
        C00152() {
        }

        public void onReceive(Context context, Intent intent) {
            RecordService.this.isDialpadShow = intent.getBooleanExtra("dialpadstatu", RecordService.LOGDEBUG);
            RecordService.this.showToast("receive dialpad statu: " + RecordService.this.isDialpadShow);
        }
    }

    public RecordService() {
        this.recording = LOGDEBUG;
        this.recordSwitch = LOGDEBUG;
        this.isAdd = LOGDEBUG;
        this.mCallSimId = MSG_VISIBLE;
        this.mHandler = new C00141();
        this.isDialpadShow = LOGDEBUG;
        this.dialpadReceiver = new C00152();
    }

    static {
        PHONE_PROJECTION = new String[]{"_id", "data2", "data3", "data1", "display_name"};
        startRecord = LOGDEBUG;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        this.telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        this.mWm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        this.mRecordMarkMarginTop = getResources().getDimensionPixelSize(R.dimen.record_mark_margin_top);
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.xiaotuan.autocallrecord.dialpadstatu");
        registerReceiver(this.dialpadReceiver, filter);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        if (intent == null) {
            stopSelf();
        }

        startRecord = intent.getBooleanExtra("startRecord", LOGDEBUG);
        startRecord=true;
        this.location = RecordUtils.getStorageLocationSharedPreferencese(getBaseContext());
        this.recordSwitch = RecordUtils.getRecordStatuSharedPreferencese(getBaseContext());
        this.phoneNumber = intent.getStringExtra("number");
        Log.d(TAG, "onStartCommand() => location: " + this.location + " recordSwitch: " + this.recordSwitch + " startRecord: " + startRecord);
        switch (this.telephonyManager.getCallState()) {
            case RecordUtils.PHONE /*0*/:
                this.mHandler.removeMessages(MSG_CANCLE_RECORD);
                this.mHandler.sendEmptyMessage(MSG_CANCLE_RECORD);
                break;
            case MSG_GONE /*2*/:
                if (startRecord) {
                    TelephonyManagerEx telephonyManagerEx = null;
                    try {
                        telephonyManagerEx = TelephonyManagerEx.class.newInstance();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    int sim1State = telephonyManagerEx.getCallState(0);
                    int sim2State = telephonyManagerEx.getCallState(MSG_VISIBLE);
                    if (sim1State == MSG_GONE) {
                        this.mCallSimId = MSG_VISIBLE;
                    } else if (sim2State == MSG_GONE) {
                        this.mCallSimId = MSG_GONE;
                    }
                    this.mHandler.removeMessages(NEW_OUTGOING_CALL);
                    Message msg = new Message();
                    msg.what = NEW_OUTGOING_CALL;
                    this.mHandler.sendMessage(msg);
                    break;
                }
                break;
        }
        return MSG_VISIBLE;
    }

    private String getCurrentTimeString() {
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmm");
        Date date = new Date();
        Log.d(TAG, "getCurrentTimeString() => timeStr: " + sf.format(date));
        return sf.format(date);
    }

    private String getCurrentTimeformat() {
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date date = new Date();
        Log.d(TAG, "getCurrentTimeString() => timeStr: " + sf.format(date));
        return sf.format(date);
    }

    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(this.dialpadReceiver);
        Log.d(TAG, "onDestroy()");
        if (this.mediaRecorder != null) {
            try {
                this.mediaRecorder.stop();
                this.mediaRecorder.reset();
                this.mediaRecorder.release();
                this.mediaRecorder = null;
                this.recording = LOGDEBUG;
            } catch (IllegalStateException e) {
                e.printStackTrace();
                Log.d(TAG, "cancleRecord() => IllegalStateException error");
            } finally {
                this.mediaRecorder = null;
                this.recording = LOGDEBUG;
            }
        }
        startRecord = LOGDEBUG;
        this.mHandler.removeMessages(MSG_VISIBLE);
        this.mHandler.removeMessages(MSG_GONE);
        if (this.isAdd) {
            this.mWm.removeView(this.mMainView);
            this.isAdd = LOGDEBUG;
        }
    }

    private void updateView(boolean isShow) {
        Log.d(TAG, "updateView() => isShow: " + isShow);
        if (this.isAdd) {
            if (isShow) {
                this.mMainView.setVisibility(View.VISIBLE);
            } else {
                this.mMainView.setVisibility(View.GONE);
            }
            this.mWm.updateViewLayout(this.mMainView, this.f0p);
        }
    }

    private void showMarket() {
        Log.d(TAG, "showMarket()");
        this.mMainView = LayoutInflater.from(this).inflate(R.layout.record_market_layout, null);
        this.f0p = new LayoutParams();
        this.f0p.width = -2;
        this.f0p.height = -2;
        this.f0p.gravity = 51;
        this.f0p.x = 0;
        this.f0p.y = this.mRecordMarkMarginTop;
        this.f0p.flags = 8;
        this.f0p.windowAnimations = 16973910;
        this.f0p.dimAmount = 1.0f;
        this.f0p.format = -3;
        this.mMainView.setLayoutParams(this.f0p);
        this.mMainView.setVisibility(View.VISIBLE);
        this.mWm.addView(this.mMainView, this.f0p);
        this.isAdd = true;
    }

    private boolean inCallScreenisTop() {
        if (((RunningTaskInfo) ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(MSG_VISIBLE).get(0)).topActivity.getClassName().equals("com.android.incallui.InCallActivity")) {
            Log.d(TAG, "inCallScreenisTop() => isTop: true");
            return true;
        }
        Log.d(TAG, "inCallScreenisTop() => isTop: false");
        return LOGDEBUG;
    }

    private void startRecording() {
        Log.d(TAG, "startRecording()");
        if (this.recordSwitch && this.telephonyManager.getCallState() == MSG_GONE) {
            String path = getRecordFileName();
            if (path == null) {
                Log.d(TAG, "Path is null.");
                return;
            } else if (RecordUtils.isOkRecordStorageSpace(this, path.substring(0, path.lastIndexOf("/")), 1)) {
                try {
                    if (this.mediaRecorder == null) {
                        this.mediaRecorder = new MediaRecorder();
                    } else {
                        this.mediaRecorder.stop();
                        this.mediaRecorder.reset();
                        this.mediaRecorder.release();
                        this.mediaRecorder = null;
                        this.mediaRecorder = new MediaRecorder();
                    }
                    this.mediaRecorder.setAudioSource(MSG_VISIBLE);
                    this.mediaRecorder.setOutputFormat(NEW_OUTGOING_CALL);
                    this.mediaRecorder.setAudioEncoder(MSG_VISIBLE);
                    this.mediaRecorder.setOutputFile(path);
                    this.mediaRecorder.prepare();
                    this.mediaRecorder.start();
                    Log.d(TAG, "startRecording() => start record");
                    this.recording = true;
                    if (!this.isAdd) {
                        showMarket();
                    }
                    this.mHandler.sendEmptyMessage(MSG_VISIBLE);
                    Log.d(TAG, "startRecording() => show market");
                    return;
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    Log.d(TAG, "startRecording() => IllegalStateException error");
                    return;
                } catch (IOException e2) {
                    Log.d(TAG, "startRecording() => IOException error");
                    e2.printStackTrace();
                    return;
                }
            } else {
                Log.d(TAG, "Memory is Lower. or auto call record is off.");
                return;
            }
        }
        Log.d(TAG, "Call statu is not OFFHOOK.");
    }

    private String getRecordFileName() {
        if (this.location == 0) {
            this.path = RecordUtils.getPhonePath(getBaseContext());
        } else {
            this.path = RecordUtils.getSDCardPath(this);
        }
        File file = new File(this.path + "/" + "test");
        if (!file.exists()) {
            file.mkdirs();
        }
        if (file.exists()) {
            file.delete();
        } else {
            this.path = null;
        }
        Log.d(TAG, "getRecordFileName() => location path: " + this.path);
        if (this.path != null) {
            this.path += "/Auto Call Record/" + getPhoneName(this.phoneNumber);
            File diretory = new File(this.path);
            if (!diretory.exists()) {
                diretory.mkdirs();
            }
            if (this.mCallSimId == MSG_VISIBLE) {
                this.path += "/" + SIM1_PRE + getCurrentTimeString() + ".amr";
            } else {
                this.path += "/" + SIM2_PRE + getCurrentTimeString() + ".amr";
            }
            Log.d(TAG, "getRecordFileName() => storage file path: " + this.path);
            return this.path;
        }
        Log.d(TAG, "getRecordFileName() => storage file path: null");
        return null;
    }

    private String getPhoneName(String phoneNumber) {
        String phoneName = phoneNumber;
        Cursor c = getContentResolver().query(Phone.CONTENT_URI, PHONE_PROJECTION, "data1 NOT NULL", null, null);
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                if (c.getString(c.getColumnIndexOrThrow("data1")).equals(phoneName)) {
                    phoneName = c.getString(c.getColumnIndexOrThrow("display_name"));
                    break;
                }
            }
        }
        Log.d(TAG, "getPhoneName() => phoneName: " + phoneName);
        c.close();
        return phoneName;
    }

    public void cancleRecord() {
        Log.d(TAG, "cancleRecord() => stop record");
        if (this.recording && this.mediaRecorder != null) {
            try {
                this.mediaRecorder.stop();
                this.mediaRecorder.reset();
                this.mediaRecorder.release();
                this.mediaRecorder = null;
                this.recording = LOGDEBUG;
            } catch (IllegalStateException e) {
                e.printStackTrace();
                Log.d(TAG, "cancleRecord() => IllegalStateException error");
            } finally {
                this.mediaRecorder = null;
                this.recording = LOGDEBUG;
            }
        }
        startRecord = LOGDEBUG;
        this.mHandler.removeMessages(MSG_VISIBLE);
        this.mHandler.removeMessages(MSG_GONE);
        if (this.isAdd) {
            updateView(LOGDEBUG);
            this.mWm.removeView(this.mMainView);
            this.isAdd = LOGDEBUG;
        }
        stopSelf();
    }

    private void showLog(String log) {
    }

    private void showToast(String log) {
    }
}
