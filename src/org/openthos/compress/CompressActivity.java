package org.openthos.compress;

import android.content.Intent;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import org.openthos.compress.bean.CommandLineBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CompressActivity extends BaseActivity {

    private static final int LIMIT_FILES_NUM = 5;
    private static final int LIMIT_FILES_HEIGHT = 250;
    private ListView mListView;
    private EditText mEtDestination, mEtFileName, mEtPassword;
    private Button mBtDestination, mBtCompress;
    private CheckBox mCbPassword, mCbShowPwd;
    private Spinner mSpType;
    private ButtonClickListener mClickListener;
    private CheckBoxChangeListener mCheckedListener;
    private TypeSelectedListener mSelectedListener;
    private List<String> mCompressList;
    private ArrayAdapter mCompressAdapter;
    private String[] mFileTypes;
    private boolean mIsPassword;
    private ArrayAdapter mTypeAdapter;
    private CommandLineBean mCmdObj;

    @Override
    protected void initView() {
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
    }

    @Override
    protected void initData() {
        mUtils = new CompressUtils(this);
        mCompressList = new ArrayList<>();
        String path = getIntent().getStringExtra(CompressUtils.COMPRESS_FILE_PATH);
        if (path != null) {
            String[] paths = path.split(CompressUtils.EXTRA_DELETE_FILE_HEADER);
            for (int i = 1; i < paths.length; i++) {
                mCompressList.add(paths[i]);
            }
            mDefaultDestination = mCompressList.get(mCompressList.size() - 1);
            mDefaultDestination = mDefaultDestination.
                    substring(0, mDefaultDestination.lastIndexOf("/"));
            mEtDestination.setText(mDefaultDestination);
            mCompressAdapter = new ArrayAdapter(this, R.layout.list_item, mCompressList);
            mListView.setAdapter(mCompressAdapter);
            ViewGroup.LayoutParams params = mListView.getLayoutParams();
            if (mCompressList.size() > LIMIT_FILES_NUM) {
                params.height = LIMIT_FILES_HEIGHT;
            } else {
                params.height = (int) (mListView.getDividerHeight() * (mCompressList.size() - 1) +
                        getResources().getDimension(R.dimen.item_height) * mCompressList.size());
            }
            mListView.setLayoutParams(params);
            mCompressAdapter.notifyDataSetChanged();

            if (mCompressList.size() == 1
                    && mCompressList.get(0).endsWith(CompressUtils.SUFFIX_TAR)) {
                mTypeAdapter = ArrayAdapter.createFromResource(this, R.array.complex_compress_type,
                        android.R.layout.simple_spinner_item);
                mFileTypes = getResources().getStringArray(R.array.complex_compress_type);
            } else {
                mTypeAdapter = ArrayAdapter.createFromResource(this, R.array.simple_compress_type,
                        android.R.layout.simple_spinner_item);
                mFileTypes = getResources().getStringArray(R.array.simple_compress_type);
            }
            mTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpType.setAdapter(mTypeAdapter);
        }
    }

    @Override
    protected void initListener() {
        mClickListener = new ButtonClickListener();
        mCheckedListener = new CheckBoxChangeListener();
        mSelectedListener = new TypeSelectedListener();
        mBtDestination.setOnClickListener(mClickListener);
        mBtCompress.setOnClickListener(mClickListener);
        mCbPassword.setOnCheckedChangeListener(mCheckedListener);
        mCbShowPwd.setOnCheckedChangeListener(mCheckedListener);
        mSpType.setOnItemSelectedListener(mSelectedListener);
    }

    @Override
    protected void startCommand() {
        String fileName = mEtFileName.getText().toString();
        String fileType = mFileTypes[mSpType.getSelectedItemPosition()];
        mCmdObj = new CommandLineBean(mDestPath, fileName, fileType, mCompressList);
        mCmdObj.setOperation(CommandLineBean.OPERATION_COMPRESS);
        mCmdObj.setDestTotalName(mDestPath + File.separator + fileName + fileType);
        if (mIsPassword && !TextUtils.isEmpty(mEtPassword.getText().toString())) {
            mCmdObj.setPassword(mEtPassword.getText().toString());
        }
        mUtils.initUtils(mCmdObj.toString().split(","));
        mUtils.checkFileName(mCmdObj.getDestTotalName(), fileName);
    }

    private void startFileChooser(int filter, int requestCode) {
        Intent intent = new Intent(CompressActivity.this, FileChooseActivity.class);
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
                case R.id.bt_co_destination:
                    startFileChooser(FileChooseActivity.FILTER_DIR, CompressUtils.REQUEST_CODE_DST);
                    break;
                case R.id.bt_co_compress:
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

    private class TypeSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
            if (CompressUtils.SUFFIX_TAR.equals(mTypeAdapter.getItem(pos))
                    || mFileTypes[0].startsWith(CompressUtils.SUFFIX_TAR)) {
                mCbPassword.setVisibility(View.GONE);
                mCbPassword.setChecked(false);
                mCbShowPwd.setChecked(false);
                mEtPassword.setText(null);
            } else {
                mCbPassword.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapter) {

        }
    }
}
