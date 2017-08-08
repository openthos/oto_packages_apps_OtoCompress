package com.openthos.compress;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.openthos.compress.adapter.FileListAdapter;
import com.openthos.compress.utils.CompressUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FileChooseActivity extends BaseActivity {

    private TextView mTvFilePath;
    private ListView mListView;
    private LinearLayout mLlBottom;
    private Button mBtConfirm;
    private Button mBtCancel;

    private ArrayList<File> mFileList;
    private FileListAdapter mAdapter;

    private File mCurrentDir;
    private boolean mIsRoot;
    private boolean mChooseDir;
    private boolean mChooseFile;
    private boolean mChooseMulti;

    public static final String STRING_FILTER = "filter_name";
    public static final String STRING_RETURN = "return_string";

    public static final int FILTER_DIR = 0x1;
    public static final int FILTER_FILE = 0x10;
    public static final int FILTER_MULTI = 0x100;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_file_choose);
        mTvFilePath = (TextView) findViewById(R.id.tv_file_path);
        mListView = (ListView) findViewById(R.id.lv_file_list);
        mLlBottom = (LinearLayout) findViewById(R.id.ll_bottom);
        mBtConfirm = (Button) findViewById(R.id.bt_bottom_ok);
        mBtCancel = (Button) findViewById(R.id.bt_bottom_cancel);
    }

    @Override
    protected void initData() {
        mFileList = new ArrayList<File>();
        Intent intent = getIntent();
        int filter = intent.getIntExtra(STRING_FILTER, FILTER_FILE);
        mChooseDir = (filter & FILTER_DIR) != 0;
        mChooseFile = (filter & FILTER_FILE) != 0;
        mChooseMulti = (filter & FILTER_MULTI) != 0;
        if (mChooseDir || mChooseMulti) {
            mLlBottom.setVisibility(View.VISIBLE);
        }
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            mCurrentDir = Environment.getExternalStorageDirectory();
        } else {
            mCurrentDir = new File("/");
        }
        showSubFiles();
    }

    @Override
    protected void initListener() {
        FileItemClickListener itemListener = new FileItemClickListener();
        ButtonClickListener buttonListener = new ButtonClickListener();
        mListView.setOnItemClickListener(itemListener);
        mBtConfirm.setOnClickListener(buttonListener);
        mBtCancel.setOnClickListener(buttonListener);
    }

    @Override
    protected void startCommand() {
    }

    private class FileItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            File file = (File) mAdapter.getItem(position);
            if (file.isDirectory()) {
                mCurrentDir = file;
                showSubFiles();
            } else {
                Intent intent = new Intent();
                intent.putExtra(STRING_RETURN, file.getAbsolutePath());
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }

    private class ButtonClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.bt_bottom_ok:
                    Intent intent = new Intent();
                    String retStr;
                    if (mChooseMulti) {
                        StringBuilder sbStr = new StringBuilder("");
                        List<Boolean> flags = mAdapter.getCheckedIndexList();
                        for (int i = 0; i < flags.size(); i++) {
                            if (flags.get(i)) {
                                sbStr.append(mFileList.get(i) + "' '");
                            }
                        }
                        if (sbStr.length() > 3) {
                            sbStr.setLength(sbStr.length() - 3);
                        }
                        retStr = sbStr.toString();
                    } else {
                        retStr = mCurrentDir.getAbsolutePath();
                    }
                    intent.putExtra(STRING_RETURN, retStr);
                    setResult(RESULT_OK, intent);
                    finish();
                    break;
                case R.id.bt_bottom_cancel:
                    finish();
                    break;
                default:
                    break;
            }
        }
    }

    private void showSubFiles() {
        if (mCurrentDir == null) {
            return;
        }
        mIsRoot = (mCurrentDir.getParent() == null)
            || (mIsUserVersion && mCurrentDir.getAbsolutePath().equals(CompressUtils.SDCARD_PATH));
        mTvFilePath.setText(mCurrentDir.getAbsolutePath());
        mFileList.clear();
        readSubFiles();
        sortSubFiles();
        if (!mIsRoot) {
            mFileList.add(0, mCurrentDir.getParentFile());
        }
        mAdapter = new FileListAdapter(this, mFileList, mIsRoot, mChooseMulti);
        mListView.setAdapter(mAdapter);
    }

    private void readSubFiles() {
        File[] fileArray = mCurrentDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (!pathname.isDirectory() && !mChooseFile) {
                    return false;
                } else {
                    return true;
                }
            }
        });
        if (fileArray != null && fileArray.length > 0) {
            for (File file : fileArray) {
                mFileList.add(file);
            }
        }
    }

    private void sortSubFiles() {
        Collections.sort(mFileList, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                if (lhs.isDirectory() && !rhs.isDirectory()) {
                    return -1;
                } else if (!lhs.isDirectory() && rhs.isDirectory()) {
                    return 1;
                } else {
                    return lhs.getName().compareTo(rhs.getName());
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mIsUserVersion && mCurrentDir.getAbsolutePath().equals("/storage/emulated/0")) {
                mCurrentDir = null;
            } else {
                mCurrentDir = mCurrentDir.getParentFile();
            }
            if (mCurrentDir == null) {
                this.finish();
            } else {
                showSubFiles();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
