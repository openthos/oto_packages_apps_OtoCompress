package com.openthos.compress;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.openthos.compress.utils.CompressUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CompressActivity extends Activity {

    private static final int LIMIT_FILES_NUM = 5;
    private static final int LIMIT_FILES_HEIGHT = 300;
    private ListView mListView;
    private EditText mEtDestination, mEtFileName, mEtPassword;
    private Button mBtDestination, mBtCompress;
    private CheckBox mCbPassword, mCbShowPwd;
    private Spinner mSpType;
    private ButtonClickListener mClickListener;
    private CheckBoxChangeListener mCheckedListener;

    private List<String> mCompressList;
    private ArrayAdapter mCompressAdapter;

    private String[] mFileTypes;
    private boolean mIsPassword;
    private boolean mIsSpecialType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compress);

        mListView = (ListView) findViewById(R.id.lv_compress_file);
        mEtDestination = (EditText) findViewById(R.id.et_co_destination);
        mEtFileName = (EditText) findViewById(R.id.et_co_name);
        mEtPassword = (EditText) findViewById(R.id.et_co_passwd);
        mBtDestination = (Button) findViewById(R.id.bt_co_destination);
        mBtCompress = (Button) findViewById(R.id.bt_co_compress);
        mCbPassword = (CheckBox) findViewById(R.id.cb_co_passwd);
        mCbShowPwd = (CheckBox) findViewById(R.id.co_passwd_visible);
        mSpType = (Spinner) findViewById(R.id.sp_co_type);
        mFileTypes = getResources().getStringArray(R.array.array_compress_type);

        mCompressList = new ArrayList<>();
        mCompressList.clear();
        String path = getIntent().getStringExtra(CompressUtils.COMPRESS_FILE_PATH);
        if (path != null) {
            String[] paths = path.split(CompressUtils.EXTRA_DELETE_FILE_HEADER);
            for (int i = 1; i < paths.length; i++) {
                mCompressList.add(paths[i]);
            }
        }
        String dstPath = mCompressList.get(mCompressList.size() - 1);
        mEtDestination.setText(dstPath.substring(0, dstPath.lastIndexOf("/")));
        mCompressAdapter = new ArrayAdapter(this, R.layout.list_item, mCompressList);
        mListView.setAdapter(mCompressAdapter);
        if (mCompressList.size() > LIMIT_FILES_NUM) {
            ViewGroup.LayoutParams params = mListView.getLayoutParams();
            params.height = LIMIT_FILES_HEIGHT;
            mListView.setLayoutParams(params);
        }
        mCompressAdapter.notifyDataSetChanged();

        mClickListener = new ButtonClickListener();
        mCheckedListener = new CheckBoxChangeListener();
        mBtDestination.setOnClickListener(mClickListener);
        mBtCompress.setOnClickListener(mClickListener);
        mCbPassword.setOnCheckedChangeListener(mCheckedListener);
        mCbShowPwd.setOnCheckedChangeListener(mCheckedListener);
    }

    private void startFileChooser(int filter, int requestCode) {
        Intent intent = new Intent(CompressActivity.this, FileChooseActivity.class);
        intent.putExtra(FileChooseActivity.STRING_FILTER, filter);
        startActivityForResult(intent, requestCode);
    }

    private void compressProcess() {
        StringBuilder sbCmd = new StringBuilder("7z a ");
        StringBuilder cmd = new StringBuilder("7z a ");
        if (mSpType.getSelectedItemPosition() == CompressUtils.TAR_GZ_POSITION
            || mSpType.getSelectedItemPosition() == CompressUtils.TAR_BZ2_POSITION) {
            sbCmd.append("-ttar ");
            cmd.append("'" + mEtDestination.getText() + File.separator
                + mEtFileName.getText() + "." + mFileTypes[mSpType.getSelectedItemPosition()]
                + "' " + "'" + mEtDestination.getText() + File.separator
                + mEtFileName.getText() + ".tar" + "' ");
            mIsSpecialType = true;
        } else {
            sbCmd.append("-t" + mFileTypes[mSpType.getSelectedItemPosition()] + " ");
            cmd = null;
            mIsSpecialType = false;
        }
        sbCmd.append("'" + mEtDestination.getText() + File.separator +
                     mEtFileName.getText() + "' ");
        sbCmd.append("'");
        for (int i = 0; i < mCompressList.size() - 1; i++) {
            sbCmd.append(mCompressList.get(i) + "' '");
        }
        sbCmd.append(mCompressList.get(mCompressList.size() - 1) + "' ");
        if (mIsPassword) {
            if (mIsSpecialType) {
                cmd.append("'-p" + mEtPassword.getText().toString() + "' ");
            } else {
                sbCmd.append("'-p" + mEtPassword.getText().toString() + "' ");
            }
        }
        new CompressUtils(CompressActivity.this, sbCmd.toString(),
                          mIsSpecialType ? cmd.toString() : null).start();
        finish();
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
                case R.id.bt_co_destination:
                    startFileChooser(FileChooseActivity.FILTER_DIR, CompressUtils.REQUEST_CODE_DST);
                    break;
                case R.id.bt_co_compress:
                    compressProcess();
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
                case R.id.cb_co_passwd:
                    mEtPassword.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                    mEtPassword.requestFocus();
                    mCbShowPwd.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                    mIsPassword = isChecked;
                    break;
                case R.id.co_passwd_visible:
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
}
