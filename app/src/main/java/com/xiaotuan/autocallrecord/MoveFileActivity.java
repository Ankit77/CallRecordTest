package com.xiaotuan.autocallrecord;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class MoveFileActivity extends ListActivity implements OnItemClickListener, OnClickListener {
    private static final String TAG = "MoveFileActivity";
    private static final int UPDATE = 1;
    private ImageButton cancle;
    private ArrayList<FileInfo> fileList;
    private boolean isPause;
    private RecordAdapter mAdapter;
    private Handler mHandler;
    private LayoutInflater mInflater;
    private ListView mList;
    private SDCardReceiver mReceiver;
    private ImageButton operate;
    private ArrayList<String> operateList;
    private int operateType;
    private ArrayList<String> pathList;
    private Resources res;

    /* renamed from: com.xiaotuan.autocallrecord.MoveFileActivity.1 */
    class C00121 extends Handler {
        C00121() {
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MoveFileActivity.UPDATE /*1*/:
                    Log.d(MoveFileActivity.TAG, "mHandler=> initValue");
                    MoveFileActivity.this.resetAdapter();
                default:
            }
        }
    }

    class SDCardReceiver extends BroadcastReceiver {
        SDCardReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (!("android.intent.action.MEDIA_MOUNTED".equals(intent.getAction()) || "android.intent.action.MEDIA_UNMOUNTED".equals(intent.getAction()) || !"android.intent.action.MEDIA_EJECT".equals(intent.getAction()))) {
            }
            MoveFileActivity.this.mHandler.removeMessages(MoveFileActivity.UPDATE);
            MoveFileActivity.this.mHandler.sendEmptyMessage(MoveFileActivity.UPDATE);
        }
    }

    public MoveFileActivity() {
        this.isPause = false;
        this.mHandler = new C00121();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.move_layout);
        initValues();
        initView();
    }

    private void initView() {
        this.cancle = (ImageButton) findViewById(R.id.ib_cancle);
        this.operate = (ImageButton) findViewById(R.id.ib_operate);
        this.mList = getListView();
        this.mList.setEmptyView(findViewById(R.id.tv_empty_view));
        this.mList.setAdapter(this.mAdapter);
        this.mList.setOnItemClickListener(this);
        this.cancle.setOnClickListener(this);
        this.operate.setOnClickListener(this);
    }

    private void initValues() {
        this.res = getResources();
        this.mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mReceiver = new SDCardReceiver();
        this.pathList = new ArrayList();
        this.pathList.add(new File(RecordUtils.getExternalStoragePath()).getParent());
        this.operateList = getIntent().getStringArrayListExtra("paths");
        this.operateType = getIntent().getIntExtra("operate", 0);
        getActionBar().setTitle(R.string.action_bar_title);
        ActionBar actionBar = getActionBar();
        Resources resources = getResources();
        int size = this.operateList.size();
        Object[] objArr = new Object[UPDATE];
        objArr[0] = Integer.valueOf(this.operateList.size());
        actionBar.setSubtitle(resources.getQuantityString(R.plurals.paste_file_title, size, objArr));
        this.fileList = new ArrayList();
        this.fileList = RecordUtils.getFoalderList((String) this.pathList.get(this.pathList.size() - 1), this);
        this.mAdapter = new RecordAdapter(this, this.fileList, false);
    }

    private void resetAdapter() {
        this.pathList.clear();
        String path = Environment.getExternalStorageDirectory().getParent();
        this.pathList.add(path);
        Log.d(TAG, "currentPath=>" + path);
        this.fileList.clear();
        this.fileList = RecordUtils.getList(path, this);
        this.mAdapter.setList(this.fileList);
        this.mAdapter.notifyDataSetChanged();
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

    protected void onPause() {
        super.onPause();
        this.isPause = true;
    }

    protected void onStop() {
        super.onStop();
        unregisterReceiver(this.mReceiver);
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        FileInfo info = (FileInfo) this.mAdapter.getItem(position);
        Log.d(TAG, "onClick=>path: " + info.getPath());
        File file = new File(info.getPath());
        if (file.isDirectory()) {
            this.fileList.clear();
            String path = file.getAbsolutePath();
            this.pathList.add(path);
            this.fileList = RecordUtils.getFoalderList(path, this);
            this.mAdapter.setList(this.fileList);
            this.mAdapter.notifyDataSetChanged();
            getActionBar().setTitle(file.getName());
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (this.pathList.size() <= UPDATE) {
            return super.onKeyDown(keyCode, event);
        }
        this.pathList.remove(this.pathList.size() - 1);
        this.fileList.clear();
        this.fileList = RecordUtils.getFoalderList((String) this.pathList.get(this.pathList.size() - 1), this);
        this.mAdapter.setList(this.fileList);
        this.mAdapter.notifyDataSetChanged();
        getActionBar().setTitle(new File((String) this.pathList.get(this.pathList.size() - 1)).getName());
        return true;
    }

    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                if (this.pathList.size() > UPDATE) {
                    this.pathList.remove(this.pathList.size() - 1);
                    this.fileList.clear();
                    this.fileList = RecordUtils.getFoalderList((String) this.pathList.get(this.pathList.size() - 1), this);
                    this.mAdapter.setList(this.fileList);
                    this.mAdapter.notifyDataSetChanged();
                    getActionBar().setTitle(new File((String) this.pathList.get(this.pathList.size() - 1)).getName());
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
            case R.id.ib_cancle:
                finish();
            case R.id.ib_operate:
                if (!new File((String) this.pathList.get(this.pathList.size() - 1)).exists()) {
                    Toast.makeText(this, getResources().getString(R.string.targe_directory_not_exsit), Toast.LENGTH_LONG).show();
                } else if (this.operateType == 0) {
                    if (RecordUtils.copyAllFile(this.operateList, (String) this.pathList.get(this.pathList.size() - 1), false)) {
                        Toast.makeText(this, getResources().getString(R.string.copy_success), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, getResources().getString(R.string.copy_fail), Toast.LENGTH_LONG).show();
                    }
                } else if (RecordUtils.cutAllFile(this.operateList, (String) this.pathList.get(this.pathList.size() - 1))) {
                    Toast.makeText(this, getResources().getString(R.string.move_success), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, getResources().getString(R.string.move_fail), Toast.LENGTH_LONG).show();
                }
                finish();
            default:
        }
    }
}
