package com.xiaotuan.autocallrecord;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import java.io.File;
import java.util.ArrayList;

public class BrowerRecordFile extends Activity implements OnItemClickListener, OnClickListener {
    private static final int COPY = 1;
    private static final int CUT = 2;
    private static final String TAG = "MainActivity";
    private static final int UPDATE = 1;
    private View control;
    private ImageButton copy;
    private ImageButton cut;
    private ImageButton delete;
    private View emptyView;
    private ListView fileList;
    private boolean isMounted;
    private boolean isPause;
    private ArrayList<FileInfo> list;
    private RecordAdapter mAdapter;
    private ModeCallback mCallback;
    private Handler mHandler;
    private LayoutInflater mInflater;
    private SDCardReceiver mReceiver;
    private ArrayList<String> pathList;
    private Resources res;
    private ImageButton share;

    /* renamed from: com.xiaotuan.autocallrecord.BrowerRecordFile.1 */
    class C00001 implements DialogInterface.OnClickListener {
        C00001() {
        }

        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    }

    /* renamed from: com.xiaotuan.autocallrecord.BrowerRecordFile.2 */
    class C00012 implements DialogInterface.OnClickListener {
        C00012() {
        }

        public void onClick(DialogInterface dialog, int which) {
            Log.d(BrowerRecordFile.TAG, "dialog=>which: " + which);
            RecordUtils.deleteAllFile(BrowerRecordFile.this.getCheckFilePath());
            if (BrowerRecordFile.this.mCallback.getMode() != null) {
                BrowerRecordFile.this.mCallback.getMode().finish();
            }
            BrowerRecordFile.this.list.clear();
            BrowerRecordFile.this.list = RecordUtils.getList((String) BrowerRecordFile.this.pathList.get(BrowerRecordFile.this.pathList.size() - 1), BrowerRecordFile.this);
            BrowerRecordFile.this.mAdapter.setList(BrowerRecordFile.this.list);
            BrowerRecordFile.this.mAdapter.notifyDataSetChanged();
        }
    }

    /* renamed from: com.xiaotuan.autocallrecord.BrowerRecordFile.3 */
    class C00023 extends Handler {
        C00023() {
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BrowerRecordFile.UPDATE /*1*/:
                    Log.d(BrowerRecordFile.TAG, "mHandler=> initValue");
                    BrowerRecordFile.this.resetAdapter();
                default:
            }
        }
    }

    private class ModeCallback implements MultiChoiceModeListener {
        private ActionMode mMode;

        private ModeCallback() {
        }

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = BrowerRecordFile.this.getMenuInflater();
            mode.setTitle(R.string.action_mode_title);
            BrowerRecordFile.this.control.setVisibility(View.VISIBLE);
            this.mMode = mode;
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return true;
        }

        public void onDestroyActionMode(ActionMode mode) {
            BrowerRecordFile.this.control.setVisibility(View.INVISIBLE);
        }

        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            int checkedCount = BrowerRecordFile.this.fileList.getCheckedItemCount();
            switch (checkedCount) {
                case RecordUtils.PHONE /*0*/:
                    mode.setSubtitle(null);
                case BrowerRecordFile.UPDATE /*1*/:
                    mode.setSubtitle(R.string.subtitle);
                default:
                    mode.setSubtitle("" + checkedCount + " " + BrowerRecordFile.this.res.getString(R.string.subtitle_suffix));
            }
        }

        public ActionMode getMode() {
            return this.mMode;
        }
    }

    class SDCardReceiver extends BroadcastReceiver {
        SDCardReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.MEDIA_MOUNTED".equals(intent.getAction())) {
                BrowerRecordFile.this.isMounted = true;
                Log.d(BrowerRecordFile.TAG, "MOUNTED=>isMounted: " + BrowerRecordFile.this.isMounted);
            } else if ("android.intent.action.MEDIA_UNMOUNTED".equals(intent.getAction())) {
                BrowerRecordFile.this.isMounted = false;
                Log.d(BrowerRecordFile.TAG, "UNMOUNTED=>isMounted: " + BrowerRecordFile.this.isMounted);
            } else if ("android.intent.action.MEDIA_EJECT".equals(intent.getAction())) {
                BrowerRecordFile.this.isMounted = false;
                Log.d(BrowerRecordFile.TAG, "EJECT=>isMounted: " + BrowerRecordFile.this.isMounted);
            }
            BrowerRecordFile.this.mHandler.removeMessages(BrowerRecordFile.UPDATE);
            BrowerRecordFile.this.mHandler.sendEmptyMessage(BrowerRecordFile.UPDATE);
        }
    }

    public BrowerRecordFile() {
        this.isMounted = true;
        this.isPause = false;
        this.mHandler = new C00023();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brower);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.record_file_list);
        this.mReceiver = new SDCardReceiver();
        this.mCallback = new ModeCallback();
        initValue();
        initView();
    }

    private void initValue() {
        this.res = getResources();
        this.mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.pathList = new ArrayList();
        String path = Environment.getExternalStorageDirectory().getParent();
        this.pathList.add(path);
        Log.d(TAG, "currentPath=>" + path);
        this.list = RecordUtils.getList(path, this);
        this.mAdapter = new RecordAdapter(this, this.list, true);
    }

    private void resetAdapter() {
        this.pathList.clear();
        String path = Environment.getExternalStorageDirectory().getParent();
        this.pathList.add(path);
        Log.d(TAG, "currentPath=>" + path);
        this.list.clear();
        this.list = RecordUtils.getList(path, this);
        this.mAdapter.setList(this.list);
        this.mAdapter.notifyDataSetChanged();
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
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

    protected void onResume() {
        super.onResume();
        if (this.isPause) {
            if (!new File((String) this.pathList.get(this.pathList.size() - 1)).exists()) {
                resetAdapter();
            }
            this.isPause = false;
        }
    }

    private void updateView() {
        this.fileList.setAdapter(this.mAdapter);
        getActionBar().setTitle(new File((String) this.pathList.get(this.pathList.size() - 1)).getName());
    }

    protected void onPause() {
        super.onPause();
        this.isPause = true;
    }

    protected void onStop() {
        super.onStop();
        unregisterReceiver(this.mReceiver);
    }

    private void initView() {
        this.emptyView = this.mInflater.inflate(R.layout.layout_list_view_empty, null);
        this.fileList = (ListView) findViewById(R.id.lv_file_list);
        this.control = findViewById(R.id.ll_control);
        this.share = (ImageButton) findViewById(R.id.ib_share);
        this.copy = (ImageButton) findViewById(R.id.ib_copy);
        this.cut = (ImageButton) findViewById(R.id.ib_cut);
        this.delete = (ImageButton) findViewById(R.id.ib_delete);
        this.control.setVisibility(View.VISIBLE);
        this.fileList.setMultiChoiceModeListener(this.mCallback);
        ((ViewGroup) this.fileList.getParent()).addView(this.emptyView, new LayoutParams(-1, -1));
        this.fileList.setEmptyView(this.emptyView);
        updateView();
        this.fileList.setOnItemClickListener(this);
        this.control.setOnClickListener(this);
        this.share.setOnClickListener(this);
        this.copy.setOnClickListener(this);
        this.cut.setOnClickListener(this);
        this.delete.setOnClickListener(this);
    }

    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                if (this.pathList.size() > UPDATE) {
                    this.pathList.remove(this.pathList.size() - 1);
                    if (new File((String) this.pathList.get(this.pathList.size() - 1)).exists()) {
                        this.list.clear();
                        this.list = RecordUtils.getList((String) this.pathList.get(this.pathList.size() - 1), this);
                        this.mAdapter.setList(this.list);
                        this.mAdapter.notifyDataSetChanged();
                        getActionBar().setTitle(new File((String) this.pathList.get(this.pathList.size() - 1)).getName());
                    }
                } else {
                    finish();
                }
                return true;
            default:
                return super.onMenuItemSelected(featureId, item);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_share:
                RecordUtils.sharedObject(getCheckFilePath(), this);
                if (this.mCallback.getMode() != null) {
                    this.mCallback.getMode().finish();
                }
            case R.id.ib_copy:
                ArrayList<String> copyPath = getCheckFilePath();
                Intent copyIntent = new Intent(this, MoveFileActivity.class);
                copyIntent.putExtra("operate", 0);
                copyIntent.putStringArrayListExtra("paths", copyPath);
                startActivityForResult(copyIntent, UPDATE);
                if (this.mCallback.getMode() != null) {
                    this.mCallback.getMode().finish();
                }
            case R.id.ib_cut:
                ArrayList<String> cutPath = getCheckFilePath();
                Intent cutIntent = new Intent(this, MoveFileActivity.class);
                cutIntent.putStringArrayListExtra("paths", cutPath);
                cutIntent.putExtra("operate", UPDATE);
                startActivityForResult(cutIntent, CUT);
                if (this.mCallback.getMode() != null) {
                    this.mCallback.getMode().finish();
                }
            case R.id.ib_delete:
                showDeleteDialog();
            default:
        }
    }

    private ArrayList<String> getCheckFilePath() {
        ArrayList<String> pathList = new ArrayList();
        long[] positions = this.fileList.getCheckItemIds();
        Log.d(TAG, "check ids=>" + positions.length);
        long[] arr$ = positions;
        int len$ = arr$.length;
        for (int i$ = 0; i$ < len$; i$ += UPDATE) {
            FileInfo info = (FileInfo) this.mAdapter.getItem((int) arr$[i$]);
            Log.d(TAG, "check:path=>" + info.getPath());
            pathList.add(info.getPath());
        }
        return pathList;
    }

    private void showDeleteDialog() {
        new Builder(this).setTitle(R.string.delete_dialog_title).setMessage(R.string.delete_message).setPositiveButton(R.string.yes, new C00012()).setNegativeButton(R.string.no, new C00001()).create().show();
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        File file = new File(((FileInfo) this.mAdapter.getItem(position)).getPath());
        if (file.isDirectory()) {
            this.list.clear();
            String path = file.getAbsolutePath();
            this.pathList.add(path);
            this.list = RecordUtils.getList(path, this);
            this.mAdapter.setList(this.list);
            this.mAdapter.notifyDataSetChanged();
            getActionBar().setTitle(file.getName());
        } else if (RecordUtils.isMediaFile(file)) {
            RecordUtils.sendViewIntent(file, this);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (this.pathList.size() > UPDATE) {
            this.pathList.remove(this.pathList.size() - 1);
            if (new File((String) this.pathList.get(this.pathList.size() - 1)).exists()) {
                this.list.clear();
                this.list = RecordUtils.getList((String) this.pathList.get(this.pathList.size() - 1), this);
                this.mAdapter.setList(this.list);
                this.mAdapter.notifyDataSetChanged();
                getActionBar().setTitle(new File((String) this.pathList.get(this.pathList.size() - 1)).getName());
                return true;
            }
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.list.clear();
        this.list = RecordUtils.getList((String) this.pathList.get(this.pathList.size() - 1), this);
        this.mAdapter.setList(this.list);
        this.mAdapter.notifyDataSetChanged();
    }
}
