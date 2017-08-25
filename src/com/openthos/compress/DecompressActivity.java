package com.openthos.compress;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.text.TextUtils;

import com.openthos.compress.utils.CompressUtils;

public class DecompressActivity extends BaseActivity {

    private TextView mTvDecompress;
    private Button mBtDestination, mBtDecompress;
    private EditText mEtDestination, mEtPassword;
    private CheckBox mCbPassword, mCbShowPwd;
    private CheckBoxChangeListener mCheckedListener;
    private ButtonClickListener mClickListener;
    private boolean mIsPassword;
    private String mDeFileName;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_decompress);
        mTvDecompress = (TextView) findViewById(R.id.tv_decompress_file);
        mBtDestination = (Button) findViewById(R.id.bt_de_destination);
        mBtDecompress = (Button) findViewById(R.id.bt_decompress);
        mEtDestination = (EditText) findViewById(R.id.et_de_destination);
        mEtPassword = (EditText) findViewById(R.id.et_de_password);
        mCbPassword = (CheckBox) findViewById(R.id.cb_de_password);
        mCbShowPwd = (CheckBox) findViewById(R.id.de_password_visible);
    }

    @Override
    protected void initData() {
        mUtils = new CompressUtils(this);
        mDeFileName = getIntent().getData().getPath();
        if (mDeFileName != null) {
            mTvDecompress.setText(mDeFileName);
            mDefaultDestination = mDeFileName.substring(0, mDeFileName.lastIndexOf("/"));
            mEtDestination.setText(mDefaultDestination);
        }
    }

    @Override
    protected void initListener() {
        mClickListener = new ButtonClickListener();
        mCheckedListener = new CheckBoxChangeListener();
        mBtDestination.setOnClickListener(mClickListener);
        mBtDecompress.setOnClickListener(mClickListener);
        mCbPassword.setOnCheckedChangeListener(mCheckedListener);
        mCbShowPwd.setOnCheckedChangeListener(mCheckedListener);
    }

    @Override
    protected void startCommand() {
        StringBuilder simpleCmd = new StringBuilder("7z t ");
        simpleCmd.append("'" + mDeFileName + "' ");
        if (mIsPassword && !TextUtils.isEmpty(mEtPassword.getText().toString())) {
            simpleCmd.append("'-p" + mEtPassword.getText().toString() + "' ");
        }
        simpleCmd.append("'-o" + mDestination + "' ");
        mUtils.initUtils(new String[]{simpleCmd.toString()});
        mUtils.start();
    }

    private void startFileChooser(int filter, int requestCode) {
        Intent intent = new Intent(DecompressActivity.this, FileChooseActivity.class);
        intent.putExtra(FileChooseActivity.STRING_FILTER, filter);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        String retStr = data.getStringExtra(FileChooseActivity.STRING_RETURN);
        switch (requestCode) {
            case CompressUtils.REQUEST_CODE_DST:
                mEtDestination.requestFocus();
                mEtDestination.setText(retStr);
                mEtDestination.selectAll();
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    class ButtonClickListener implements OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.bt_de_destination:
                    startFileChooser(FileChooseActivity.FILTER_DIR, CompressUtils.REQUEST_CODE_DST);
                    break;
                case R.id.bt_decompress:
                    checkDestination(mEtDestination);
                    break;
                default:
                    break;
            }
        }
    }

    class CheckBoxChangeListener implements OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.cb_de_password:
                    mEtPassword.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                    mEtPassword.requestFocus();
                    mCbShowPwd.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                    mIsPassword = isChecked;
                    break;
                case R.id.de_password_visible:
                    int pwdType = isChecked ? InputType.TYPE_CLASS_TEXT
                            : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
                    mEtPassword.setInputType(pwdType);
                    mEtPassword.selectAll();
                    break;
                default:
                    break;
            }
        }
    }

    public void inputPassword(boolean input) {
        if (!(mDeFileName.endsWith(CompressUtils.SUFFIX_TAR)
                || mDeFileName.endsWith(CompressUtils.SUFFIX_GZ)
                || mDeFileName.endsWith(CompressUtils.SUFFIX_BZ2))) {
            if (input) {
                mCbPassword.setChecked(true);
                mUtils.showSimpleAlertDialog(getString(R.string.hint_input_password));
            } else {
                mUtils.showSimpleAlertDialog(getString(R.string.hint_wrong_password));
            }
        }
    }
}
