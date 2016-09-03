package com.xiaotuan.autocallrecord;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class RecordAdapter extends BaseAdapter {
    private static final String TAG = "RecordAdapter";
    private boolean isShowCount;
    private LayoutInflater mInflater;
    private ArrayList<FileInfo> mList;

    class ViewHolder {
        TextView fileCount;
        TextView fileName;
        ImageView fileType;

        ViewHolder() {
        }
    }

    public RecordAdapter(Context context, ArrayList<FileInfo> list, boolean isShow) {
        this.mList = (ArrayList) list.clone();
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.isShowCount = isShow;
    }

    public void setList(ArrayList<FileInfo> list) {
        this.mList.clear();
        this.mList = (ArrayList) list.clone();
    }

    public void setShowCount(boolean flag) {
        this.isShowCount = flag;
    }

    public int getCount() {
        return this.mList == null ? 0 : this.mList.size();
    }

    public Object getItem(int position) {
        return this.mList.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mHolder;
        if (convertView == null) {
            convertView = this.mInflater.inflate(R.layout.adapter_layout, parent, false);
            mHolder = new ViewHolder();
            mHolder.fileType = (ImageView) convertView.findViewById(R.id.iv_file_type);
            mHolder.fileName = (TextView) convertView.findViewById(R.id.tv_file_name);
            mHolder.fileCount = (TextView) convertView.findViewById(R.id.tv_count);
            convertView.setTag(mHolder);
        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }
        FileInfo info = (FileInfo) this.mList.get(position);
        if (info.isDirectory()) {
            mHolder.fileType.setImageResource(R.drawable.img_floader);
        } else {
            mHolder.fileType.setImageResource(R.drawable.img_file);
        }
        mHolder.fileName.setText(info.getFileName());
        if (!info.isDirectory()) {
            mHolder.fileCount.setVisibility(View.INVISIBLE);
        } else if (this.isShowCount) {
            mHolder.fileCount.setText("(" + info.getCount() + ")");
            mHolder.fileCount.setVisibility(View.VISIBLE);
        } else {
            mHolder.fileCount.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }
}
