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
    private static final int LIMIT_FILES_HEIGHT = 250;
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

        ArrayAdapter adapter;
        if (mCompressList.size() == 1 && mCompressList.get(0).endsWith(".tar")) {
            adapter = ArrayAdapter.createFromResource(this, R.array.complex_compress_type,
                                                      android.R.layout.simple_spinner_item);
            mFileTypes = getResources().getStringArray(R.array.complex_compress_type);
        } else {
            adapter = ArrayAdapter.createFromResource(this, R.array.simple_compress_type,
                                                      android.R.layout.simple_spinner_item);
            mFileTypes = getResources().getStringArray(R.array.simple_compress_type);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpType.setAdapter(adapter);

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
        CompressUtils utils = new CompressUtils();
        StringBuilder simpleCmd = new StringBuilder("7z a ");
        simpleCmd.append("'" + mEtDestination.getText().toString() + File.separator +
                      mEtFileName.getText() + mFileTypes[mSpType.getSelectedItemPosition()] + "' ");
        simpleCmd.append("'");
        for (int i = 0; i < mCompressList.size() - 1; i++) {
            simpleCmd.append(mCompressList.get(i) + "' '");
        }
        simpleCmd.append(mCompressList.get(mCompressList.size() - 1) + "' ");
        if (mIsPassword) {
            simpleCmd.append("'-p" + mEtPassword.getText().toString() + "' ");
        }
        utils.initUtils(this, simpleCmd.toString());
        utils.checkFileName(mEtDestination.getText().toString() + File.separator +
              mEtFileName.getText()+ mFileTypes[mSpType.getSelectedItemPosition()],
              mEtFileName.getText().toString());
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
