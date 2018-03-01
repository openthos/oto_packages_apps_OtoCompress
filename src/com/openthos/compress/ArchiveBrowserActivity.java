package com.openthos.compress;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hu.p7zip.ZipUtils;
import com.openthos.compress.bean.CommandLineBean;
import com.openthos.compress.common.SingleLineInputDialog;

import java.io.File;
import java.util.ArrayList;

public class ArchiveBrowserActivity extends BaseActivity
        implements ArchiveBrowserContract.IView, View.OnClickListener {

    private ImageView mImgBack, mImgForward, mImgHome;
    private Button mBtExtractAll, mBtExtract, mBtAdd, mImgSearch;
    private TextView mTvFilesCount, mTvDirsCount, mTvFile, mTvFolder;
    private EditText mEtCurrentPath;
    private ListView mListView;
    private CommandLineBean mCmdBean;
    private ArchiveBrowserContract.IPresenter mIPresenter;
    private String mCurrentPath = "/";
    private String mSrcFilePath, mSrcFileName;
    public static final String SPECIFIC_FILE_NAME = "specific_file_name";
    public static final String PASSWORD = "password";
    private SingleLineInputDialog mPwDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void initView() {
        setContentView(R.layout.activity_file_browser);
        mImgBack = (ImageView) findViewById(R.id.img_back);
        mImgForward = (ImageView) findViewById(R.id.img_forward);
        mImgHome = (ImageView) findViewById(R.id.img_home);
        mImgSearch = (Button) findViewById(R.id.img_search);
        mBtExtract = (Button) findViewById(R.id.bt_extract);
        mBtExtractAll = (Button) findViewById(R.id.bt_extract_all);
        mBtAdd = (Button) findViewById(R.id.bt_add);
        mEtCurrentPath = (EditText) findViewById(R.id.et_current_dir);
        mTvFilesCount = (TextView) findViewById(R.id.tv_files_count);
        mTvDirsCount = (TextView) findViewById(R.id.tv_dirs_count);
        mTvFile = (TextView) findViewById(R.id.tv_file);
        mTvFolder = (TextView) findViewById(R.id.tv_folder);
        mListView = (ListView) findViewById(R.id.list_view);
    }

    protected void initListener() {
        mImgBack.setOnClickListener(this);
        mImgForward.setOnClickListener(this);
        mImgHome.setOnClickListener(this);
        mBtExtractAll.setOnClickListener(this);
        mBtExtract.setOnClickListener(this);
        mBtAdd.setOnClickListener(this);
        mImgSearch.setOnClickListener(this);
        mEtCurrentPath.setOnFocusChangeListener(getEtFocusChangeListener());
        mEtCurrentPath.setOnKeyListener(getEtOnKeyListener());
    }

    protected void initData() {
        mIPresenter = new ArchiveBrowserPresenter(this, this);
        mCmdBean = new CommandLineBean();
        mCmdBean.setOperation(CommandLineBean.OPERATION_SHOW_LIST);
        String srcFileTotalName = getIntent().getData().getPath();
        int index = srcFileTotalName.lastIndexOf("/") + 1;
        mSrcFilePath = srcFileTotalName.substring(0, index);
        mCmdBean.setSrcPath(mSrcFilePath);
        mSrcFileName = srcFileTotalName.substring(index);
        mCmdBean.setFileName(mSrcFileName);
        startCommand();
    }

    @Override
    public void startCommand() {
        if (CommandLineBean.OPERATION_SHOW_LIST.equals(mCmdBean.getOperation())) {
            showDeCompressingDialog();
        } else if (CommandLineBean.OPERATION_COMPRESS.equals(mCmdBean.getOperation())) {
            showCompressingDialog();
        }
        new AsyncTask<Void, Void, Void>() {
            String result;

            @Override
            protected Void doInBackground(Void... voids) {
                result = ZipUtils.executeCommandGetStream(mCmdBean.toString());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (CommandLineBean.OPERATION_SHOW_LIST.equals(mCmdBean.getOperation())) {
                    mIPresenter.handleResult(result);
                } else if (CommandLineBean.OPERATION_COMPRESS.equals(mCmdBean.getOperation())) {
                    mIPresenter.removeTempFile();
                    mIPresenter.clearData();
                    mCmdBean.setOperation(CommandLineBean.OPERATION_SHOW_LIST);
                    startCommand();
                }
            }
        }.execute();
    }

    @Override
    public void toInputPassword(boolean hasInputPassword) {
        if (mPwDialog == null) {
            initPwDialog();
        }
        if (hasInputPassword) {
            String msg = getResources().getString(R.string.hint_wrong_password);
            mPwDialog.setMessage(msg);
        } else {
            String msg = getResources().getString(R.string.hint_input_password);
            mPwDialog.setMessage(msg);
        }
        mPwDialog.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                mIPresenter.onBackClick();
                break;
            case R.id.img_forward:
                mIPresenter.onForwardClick();
                break;
            case R.id.img_home:
                mIPresenter.goHome();
                break;
            case R.id.bt_extract_all:
                doExtract(getIntent().getData(), "");
                this.finish();
                break;
            case R.id.bt_extract:
                String selectedFileName = mIPresenter.getSelectedFileName();
                if (TextUtils.isEmpty(selectedFileName)) {
                    return;
                }
                String selectedDir = mCurrentPath;
                if (mCurrentPath.startsWith("/")) {
                    selectedDir = selectedDir.substring(1);
                }
                if (mCurrentPath.endsWith("/")) {
                    selectedDir += mIPresenter.getSelectedFileName();
                } else {
                    selectedDir += "/" + mIPresenter.getSelectedFileName();
                }
                doExtract(getIntent().getData(), selectedDir);
                this.finish();
                break;
            case R.id.bt_add:
                startFileChooser(FileChooseActivity.FILTER_FILE, CompressUtils.REQUEST_CODE_DST);
                break;
            case R.id.img_search:
                doSearch();
                break;
        }
    }

    @Override
    public void doExtract(Uri srcUri, String specificFile) {
        Intent intent = new Intent(this, DecompressActivity.class);
        intent.setDataAndType(srcUri, "");
        if (!TextUtils.isEmpty(mCmdBean.getPassword())) {
            intent.putExtra(PASSWORD, mCmdBean.getPassword());
        }
        intent.putExtra(SPECIFIC_FILE_NAME, specificFile);
        startActivity(intent);
    }

    private void doSearch() {
        if (mCurrentPath.equals(mEtCurrentPath.getText().toString().trim())) {
            return;
        }
        mCurrentPath = mEtCurrentPath.getText().toString().trim();
        int pathLength = mCurrentPath.length();
        if (mCurrentPath.endsWith("/")) {
            mCurrentPath = mCurrentPath.substring(0, pathLength - 1);
        }
        if (mCurrentPath.length() == 0) {
            mCurrentPath = "/";
        }
        mIPresenter.searchByDir(mCurrentPath);
    }

    private void startFileChooser(int filter, int requestCode) {
        Intent intent = new Intent(this, FileChooseActivity.class);
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
                String filePath;
                if ("/".equals(mCurrentPath)) {
                    filePath = retStr;
                } else {
                    String tempPath = new File(Environment.getExternalStorageDirectory(),
                            "ArchiveBrowserTemp").getAbsolutePath();
                    filePath = mIPresenter.createTempFile(retStr, tempPath, mCurrentPath);
                }
                mCmdBean.setOperation(CommandLineBean.OPERATION_COMPRESS);
                mCmdBean.setDestPath(mSrcFilePath);
                mCmdBean.setFileName(mSrcFileName);
                ArrayList<String> list = new ArrayList<>();
                list.add(filePath);
                mCmdBean.setSrcFilePaths(list);
                startCommand();
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ESCAPE) {
            return mIPresenter.onEscClick();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void updateUIData(String path, int dirsCount, int filesCount) {
        mCurrentPath = path;
        if (!mCurrentPath.startsWith("/")) {
            mCurrentPath = "/" + mCurrentPath;
        }
        if (path.endsWith("/")) {
            mEtCurrentPath.setText(mCurrentPath);
        } else {
            mEtCurrentPath.setText(mCurrentPath + "/");
        }
        String filesCountStr = filesCount + getResources().getString(R.string.one_space);
        mTvFilesCount.setText(filesCountStr);
        if (filesCount < 2) {
            mTvFile.setText(getString(R.string.file));
        } else {
            mTvFile.setText(getString(R.string.files));
        }
        String dirsCountStr = dirsCount + getResources().getString(R.string.one_space);
        mTvDirsCount.setText(dirsCountStr);
        if (dirsCount < 2) {
            mTvFolder.setText(getString(R.string.folder));
        } else {
            mTvFolder.setText(getString(R.string.folders));
        }
    }

    @Override
    public void initPwDialog() {
        if (mPwDialog == null) {
            mPwDialog = new SingleLineInputDialog(this);
            mPwDialog.setCancelListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mPwDialog.cancel();
                    finish();
                }
            });
            mPwDialog.setConfirmListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String pw = mPwDialog.getContent();
                    mCmdBean.setPassword(pw);
                    startCommand();
                }
            });
            mPwDialog.setCanceledOnTouchOutside(false);
        }
    }

    @Override
    public void cancelInputDialog() {
        if (mPwDialog != null) {
            mPwDialog.cancel();
        }
    }

    @Override
    public void cancelProgressDialog() {
        cancelDialog();
    }

    @Override
    public ListView getListView() {
        return mListView;
    }

    @Override
    public void clearFocus() {
        if (mEtCurrentPath.hasFocus()) {
            mListView.requestFocus();
        }
    }

    private View.OnKeyListener getEtOnKeyListener() {
        return new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (mEtCurrentPath.isCursorVisible()
                        && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER ||
                        keyEvent.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_ENTER)
                        && (keyEvent.getAction() == KeyEvent.ACTION_DOWN)) {
                    doSearch();
                    return true;
                }
                return false;
            }
        };
    }

    private View.OnFocusChangeListener getEtFocusChangeListener() {
        return new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focusChanged) {
                if (focusChanged) {
                    mEtCurrentPath.setSelection(mEtCurrentPath.length());
                }
            }
        };
    }

}
