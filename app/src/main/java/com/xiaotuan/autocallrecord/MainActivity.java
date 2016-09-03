package com.xiaotuan.autocallrecord;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TwoLineListItem;

public class MainActivity extends Activity implements OnCheckedChangeListener, OnClickListener {
    private static final int MIN_STORAGE_SPACE = 1;
    private static final String TAG = "MainActivity";
    private static final int UPDATE = 1;
    private static boolean debug;
    private TwoLineListItem browerRecordFile;
    private boolean isMountSdCard;
    private boolean isMounted;
    private int location;
    private TextView locationText;
    private Handler mHandler;
    private SDCardReceiver mReceiver;
    private Switch recordSwitch;
    private Resources res;
    private boolean statu;
    private TwoLineListItem storageLocation;
    private TwoLineListItem version;

    /* renamed from: com.xiaotuan.autocallrecord.MainActivity.1 */
    class C00031 implements DialogInterface.OnClickListener {
        C00031() {
        }

        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    }

    /* renamed from: com.xiaotuan.autocallrecord.MainActivity.2 */
    class C00042 implements DialogInterface.OnClickListener {
        C00042() {
        }

        public void onClick(DialogInterface dialog, int which) {
            Log.d(MainActivity.TAG, "showNoSdDialog()=> location: " + MainActivity.this.location);
            RecordUtils.putStorageLocationSharedPreferencese(MainActivity.this, MainActivity.this.location);
            MainActivity.this.setSummaryText(MainActivity.this.location);
        }
    }

    /* renamed from: com.xiaotuan.autocallrecord.MainActivity.3 */
    class C00053 implements DialogInterface.OnClickListener {
        C00053() {
        }

        public void onClick(DialogInterface dialog, int which) {
            MainActivity.this.location = which;
        }
    }

    /* renamed from: com.xiaotuan.autocallrecord.MainActivity.4 */
    class C00064 implements DialogInterface.OnClickListener {
        C00064() {
        }

        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    }

    /* renamed from: com.xiaotuan.autocallrecord.MainActivity.5 */
    class C00075 implements DialogInterface.OnClickListener {
        C00075() {
        }

        public void onClick(DialogInterface dialog, int which) {
            Log.d(MainActivity.TAG, "showHasSdDialog()=> location: " + MainActivity.this.location);
            RecordUtils.putStorageLocationSharedPreferencese(MainActivity.this, MainActivity.this.location);
            MainActivity.this.setSummaryText(MainActivity.this.location);
        }
    }

    /* renamed from: com.xiaotuan.autocallrecord.MainActivity.6 */
    class C00086 implements DialogInterface.OnClickListener {
        C00086() {
        }

        public void onClick(DialogInterface dialog, int which) {
            MainActivity.this.location = which;
        }
    }

    /* renamed from: com.xiaotuan.autocallrecord.MainActivity.7 */
    class C00097 implements DialogInterface.OnClickListener {
        C00097() {
        }

        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    }

    /* renamed from: com.xiaotuan.autocallrecord.MainActivity.8 */
    class C00108 implements DialogInterface.OnClickListener {
        C00108() {
        }

        public void onClick(DialogInterface dialog, int which) {
            MainActivity.this.recordSwitch.setChecked(false);
            dialog.dismiss();
        }
    }

    /* renamed from: com.xiaotuan.autocallrecord.MainActivity.9 */
    class C00119 extends Handler {
        C00119() {
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MainActivity.UPDATE /*1*/:
                    MainActivity.this.updateView();
                default:
            }
        }
    }

    class SDCardReceiver extends BroadcastReceiver {
        SDCardReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.MEDIA_MOUNTED".equals(intent.getAction())) {
                MainActivity.this.isMounted = true;
                Log.d(MainActivity.TAG, "MOUNTED=>isMounted: " + MainActivity.this.isMounted);
            } else if ("android.intent.action.MEDIA_UNMOUNTED".equals(intent.getAction())) {
                MainActivity.this.isMounted = false;
                Log.d(MainActivity.TAG, "UNMOUNTED=>isMounted: " + MainActivity.this.isMounted);
            } else if ("android.intent.action.MEDIA_EJECT".equals(intent.getAction())) {
                MainActivity.this.isMounted = false;
                Log.d(MainActivity.TAG, "EJECT=>isMounted: " + MainActivity.this.isMounted);
            }
            MainActivity.this.mHandler.removeMessages(MainActivity.UPDATE);
            MainActivity.this.mHandler.sendEmptyMessage(MainActivity.UPDATE);
        }
    }

    public MainActivity() {
        this.isMountSdCard = false;
        this.isMounted = false;
        this.mHandler = new C00119();
    }

    static {
        debug = true;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initValues();
        initView();
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
        updateView();
    }

    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.MEDIA_MOUNTED");
        filter.addAction("android.intent.action.MEDIA_UNMOUNTED");
        filter.addAction("android.intent.action.MEDIA_EJECT");
        filter.addDataScheme("file");
        registerReceiver(this.mReceiver, filter);
    }

    protected void onStop() {
        super.onStop();
        unregisterReceiver(this.mReceiver);
    }

    private void initView() {
        this.recordSwitch = (Switch) findViewById(R.id.s_switch);
        this.storageLocation = (TwoLineListItem) findViewById(R.id.tli_storage_location);
        this.browerRecordFile = (TwoLineListItem) findViewById(R.id.tli_record_file);
        this.version = (TwoLineListItem) findViewById(R.id.tli_version);
        this.locationText = (TextView) findViewById(R.id.tv_location_text);
        this.recordSwitch.setOnCheckedChangeListener(this);
        this.storageLocation.setOnClickListener(this);
        this.browerRecordFile.setOnClickListener(this);
        this.version.setOnClickListener(this);
        this.recordSwitch.setChecked(this.statu);
        setSummaryText(this.location);
    }

    private void setSummaryText(int location) {
        if (location == 0) {
            this.locationText.setText(R.string.phone);
        } else {
            this.locationText.setText(R.string.sd_card);
        }
    }

    private void initValues() {
        this.res = getResources();
        this.statu = RecordUtils.getRecordStatuSharedPreferencese(this);
        this.location = RecordUtils.getStorageLocationSharedPreferencese(this);
        this.mReceiver = new SDCardReceiver();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tli_storage_location:
                if (RecordUtils.isExistSDCard(this)) {
                    showHasSdDialog();
                } else {
                    showNoSdDialog();
                }
            case R.id.tli_record_file:
                startActivity(new Intent(this, BrowerRecordFile.class));
            case R.id.tli_version:
                showVersionDialog();
            default:
        }
    }

    private void showNoSdDialog() {
        Builder title = new Builder(this).setTitle(R.string.dialog_title);
        CharSequence[] charSequenceArr = new String[UPDATE];
        charSequenceArr[0] = this.res.getString(R.string.phone);
        title.setSingleChoiceItems(charSequenceArr, this.location, new C00053()).setPositiveButton(R.string.ok, new C00042()).setNegativeButton(R.string.cancle, new C00031()).create().show();
    }

    private void showHasSdDialog() {
        new Builder(this).setTitle(R.string.dialog_title).setSingleChoiceItems(new String[]{this.res.getString(R.string.phone), this.res.getString(R.string.sd_card)}, this.location, new C00086()).setPositiveButton(R.string.ok, new C00075()).setNegativeButton(R.string.cancle, new C00064()).create().show();
    }

    public void showVersionDialog() {
        new Builder(this).setTitle(R.string.version).setMessage(R.string.current_version).setPositiveButton(R.string.ok, new C00097()).create().show();
    }

    private void showNoMoreSpaceDialog() {
        new Builder(this).setTitle(R.string.low_memory).setMessage(R.string.low_memory_message).setPositiveButton(R.string.ok, new C00108()).create().show();
    }

    public void onCheckedChanged(CompoundButton button, boolean isChecked) {
        if (RecordUtils.isHasEnoughStorageSpace(this, 1) || !isChecked) {
            this.statu = isChecked;
            RecordUtils.putRecordStatuSharedPreferencese(this, isChecked);
            return;
        }
        showNoMoreSpaceDialog();
    }

    protected void updateView() {
        updateSwitchView();
        updateStorageLocation();
    }

    public void updateSwitchView() {
        if (!RecordUtils.isHasEnoughStorageSpace(this, 1) && this.recordSwitch.isChecked() && this.location == 0) {
            this.recordSwitch.setChecked(false);
        } else {
            this.recordSwitch.setChecked(this.statu);
        }
    }

    public void updateStorageLocation() {
        if (RecordUtils.isExistSDCard(this)) {
            setSummaryText(this.location);
            return;
        }
        if (this.location != 0) {
            this.location = 0;
        }
        setSummaryText(0);
    }
}
