package com.openthos.compress;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.EditText;

import com.openthos.compress.utils.CompressUtils;

import java.io.File;
import java.io.IOException;

public abstract class BaseActivity extends Activity {
    public boolean mIsUserVersion;
    public String mDestination;
    public String mDefaultDestination;
    public CompressUtils mUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIsUserVersion = "user".equals(Build.TYPE);

        initView();
        initData();
        initListener();
    }

    public void checkDestination(EditText editText) {
        mDestination = editText.getText().toString();
        if (TextUtils.isEmpty(mDestination)) {
            mDestination = mDefaultDestination;
        }
        try {
            File file = new File(mDestination);
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
}
