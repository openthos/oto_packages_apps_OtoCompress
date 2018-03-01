package com.openthos.compress;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.EditText;

import com.openthos.compress.common.ProgressInfoDialog;

import java.io.File;
import java.io.IOException;

public abstract class BaseActivity extends Activity {
    public boolean mIsUserVersion;
    public String mDestPath;
    public String mDefaultDestination;
    public CompressUtils mUtils;
    public ProgressInfoDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsUserVersion = "user".equals(Build.TYPE);
        initView();
        initData();
        initListener();
    }

    public void checkDestination(EditText editText) {
        mDestPath = editText.getText().toString();
        if (TextUtils.isEmpty(mDestPath)) {
            mDestPath = mDefaultDestination;
        }
        try {
            File file = new File(mDestPath);
            if (mIsUserVersion && !file.getCanonicalPath().startsWith(CompressUtils.SDCARD_PATH)) {
                mUtils.toast(getString(R.string.hint_no_permission));
            } else {
                startCommand();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            DialogInterface.OnClickListener ok = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int i) {
                    finish();
                }
            };
            mUtils.showChooseAlertDialog(getString(R.string.exit_promt), ok, null);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected abstract void initView();

    protected abstract void initData();

    protected abstract void initListener();

    protected abstract void startCommand();

    public void showCompressingDialog() {
        if (mDialog == null) {
            mDialog = ProgressInfoDialog.getInstance(this);
        }
        if (!mDialog.isShowing()) {
            mDialog.showDialog(R.raw.compress);
            mDialog.changeTitle(getResources().getString(R.string.compress_info));
        }
    }

    public void showDeCompressingDialog() {
        if (mDialog == null) {
            mDialog = ProgressInfoDialog.getInstance(this);
        }
        if (!mDialog.isShowing()) {
            mDialog.showDialog(R.raw.decompress);
            mDialog.changeTitle(getResources().getString(R.string.compress_info));
        }
    }

    public void cancelDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.cancel();
        }
    }
}
