package org.openthos.compress;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class FileListAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<File> mFiles;
    private boolean mIsRoot;
    private boolean mIsMulti;
    private LayoutInflater mInflater;
    private List<Boolean> mChecked;

    private static final String[] mFileExts = {
            "7z", "cab", "iso", "rar", "tar", "zip", "gz", "bz2"};

    private static final int[] mFileIcons = {
            R.mipmap.icon_7z, R.mipmap.icon_cab, R.mipmap.icon_iso,
            R.mipmap.icon_rar, R.mipmap.icon_tar, R.mipmap.icon_zip,
            R.mipmap.icon_gz, R.mipmap.icon_bz2};

    public FileListAdapter(Context context, ArrayList<File> files,
                           boolean isRoot, boolean isMulti) {
        mContext = context;
        mFiles = files;
        mIsRoot = isRoot;
        mIsMulti = isMulti;
        mInflater = LayoutInflater.from(context);

        if (mIsMulti) {
            mChecked = new ArrayList<Boolean>();
            for (int i = 0; i < mFiles.size(); i++) {
                mChecked.add(false);
            }
        }
    }

    @Override
    public int getCount() {
        return mFiles.size();
    }

    @Override
    public Object getItem(int position) {
        return mFiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private String getFileInfoString(File file) {
        long fileSize = file.length();
        String ret = new SimpleDateFormat("yyyy-MM-dd HH:mm  ").format(file.lastModified());
        if (file.isDirectory()) {
            File[] sub = file.listFiles();
            int subCount = 0;
            if (sub != null) {
                subCount = sub.length;
            }
            ret += subCount + " items";
        } else {
            float size = 0.0f;
            if (fileSize > 1024 * 1024 * 1024) {
                size = fileSize / (1024f * 1024f * 1024f);
                ret += new DecimalFormat("#.00").format(size) + "GB";
            } else if (fileSize > 1024 * 1024) {
                size = fileSize / (1024f * 1024f);
                ret += new DecimalFormat("#.00").format(size) + "MB";
            } else if (fileSize >= 1024) {
                size = fileSize / 1024;
                ret += new DecimalFormat("#.00").format(size) + "KB";
            } else {
                ret += fileSize + "B";
            }
        }
        return ret;
    }

    private int getFileIconId(File file) {
        int id = R.mipmap.icon_default;
        String fileName = file.getName();
        String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1);
        for (int i = 0; i < mFileExts.length; i++) {
            if (fileExt.endsWith(mFileExts[i])) {
                id = mFileIcons[i];
                break;
            }
        }
        return id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.file_list_item, null);
            convertView.setTag(viewHolder);
            viewHolder.fileIcon = (ImageView) convertView.findViewById(R.id.iv_file_icon);
            viewHolder.fileName = (TextView) convertView.findViewById(R.id.tv_fc_file_name);
            viewHolder.fileInfo = (TextView) convertView.findViewById(R.id.tv_file_info);
            viewHolder.isChecked = (CheckBox) convertView.findViewById(R.id.cb_choose_flag);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (position == 0 && !mIsRoot) {
            viewHolder.fileIcon.setImageResource(R.mipmap.icon_folder);
            viewHolder.fileName.setText("..");
            viewHolder.fileInfo.setText(mContext.getString(R.string.parent_folder));
            viewHolder.isChecked.setVisibility(View.GONE);
        } else {
            File file = (File) getItem(position);
            viewHolder.fileName.setText(file.getName());
            viewHolder.fileInfo.setText(getFileInfoString(file));
            if (file.isDirectory()) {
                viewHolder.fileIcon.setImageResource(R.mipmap.icon_folder);
                viewHolder.isChecked.setVisibility(mIsMulti ? View.VISIBLE : View.GONE);
            } else {
                viewHolder.fileIcon.setImageResource(getFileIconId(file));
                viewHolder.isChecked.setVisibility(mIsMulti ? View.VISIBLE : View.GONE);
            }
        }
        if (mIsMulti) {
            final int p = position;
            viewHolder.isChecked.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mChecked.set(p, isChecked);
                }
            });
        }
        return convertView;
    }

    public List<Boolean> getCheckedIndexList() {
        return mChecked;
    }

    class ViewHolder {
        private ImageView fileIcon;
        private TextView fileName;
        private TextView fileInfo;
        private CheckBox isChecked;
    }
}
