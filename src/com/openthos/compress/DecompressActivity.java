package com.openthos.compress;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

import com.openthos.compress.utils.CompressUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class DecompressActivity extends Activity {

    private TextView mTvDecompress;
    private Button mBtDestination, mBtDecompress;
    private EditText mEtDestination, mEtPassword;
    private CheckBox mCbPassword, mCbShowPwd;
    private CheckBoxChangeListener mCheckedListener;
    private ButtonClickListener mClickListener;
    private boolean mIsPassword = false;
    private boolean mIsSpecialType;
    private String mDeFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decompress);

        mTvDecompress = (TextView) findViewById(R.id.tv_decompress_file);
        mBtDestination = (Button) findViewById(R.id.bt_de_destination);
        mBtDecompress = (Button) findViewById(R.id.bt_decompress);
        mEtDestination = (EditText) findViewById(R.id.et_de_destination);
        mEtPassword = (EditText) findViewById(R.id.et_de_password);
        mCbPassword = (CheckBox) findViewById(R.id.cb_de_password);
        mCbShowPwd = (CheckBox) findViewById(R.id.cb_de_password_visib);

        mClickListener = new ButtonClickListener();
        mCheckedListener = new CheckBoxChangeListener();
        mBtDestination.setOnClickListener(mClickListener);
        mBtDecompress.setOnClickListener(mClickListener);
        mCbPassword.setOnCheckedChangeListener(mCheckedListener);
        mCbShowPwd.setOnCheckedChangeListener(mCheckedListener);

        mDeFileName = getIntent().getStringExtra(CompressUtils.COMPRESS_FILE_PATH);
        mEtDestination.setText(mDeFileName.substring(0, mDeFileName.lastIndexOf("/")));
        if (mDeFileName != null) {
            mTvDecompress.setText(mDeFileName);
        }
//        mDeFileName = "storage/emulated/0/Movies/c1.zip";
    }

    private void extractProcess() {
        StringBuilder sbCmd = new StringBuilder("7z x ");
        StringBuilder cmd = new StringBuilder("7z x ");
        sbCmd.append("'" + mDeFileName + "' ");    //7z x 'aaa/bbb.zip'
        if (mDeFileName.endsWith(".tar.gz") || mDeFileName.endsWith("tar.bz2")) {
            mDeFileName = mEtDestination.getText().toString() +
                    mDeFileName.substring(mDeFileName.lastIndexOf("/"), mDeFileName.lastIndexOf("."));
            cmd.append("'" + mDeFileName + "' ");
            mIsSpecialType = true;
        } else {
            cmd = null;
            mIsSpecialType = false;
        }
        if (!isContinue(mDeFileName.substring(0, mDeFileName.lastIndexOf("/")))) {
            return;
        }

        if (mIsPassword) {
            sbCmd.append("'-p" + mEtPassword.getText().toString() + "' ");    //7z x 'a.zip' '-o/out/' '*.txt' -ppwd
        }
        sbCmd.append("'-o" + mEtDestination.getText().toString() + "' ");    //7z x 'a.zip' '-o/out/'
        if (mIsSpecialType) {
            cmd.append("'-o" + mEtDestination.getText().toString() + "' ");    //7z x 'a.zip' '-o/out/'
        }
        sbCmd.append("-aoa ");
        if (mIsSpecialType) {
            cmd.append("-aoa ");
        }
        Log.d("LUNING", "-------------------sbCmd = " + sbCmd.toString() + "------");
        if (cmd != null) {
            Log.d("LUNING", "-------------------cmd = " + cmd.toString() + "------");
        }
        String check = "7z l " + mEtDestination.getText().toString() +
                mDeFileName.substring(mDeFileName.lastIndexOf("/"), mDeFileName.lastIndexOf("."));
        new CompressUtils(DecompressActivity.this, sbCmd.toString(), mIsSpecialType ? cmd.toString() : null, check).start();
        finish();
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
                    extractProcess();
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
                case R.id.cb_de_password_visib:
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

    private Boolean isContinue(String path) {
        final boolean[] isContinue = new boolean[1];
        isContinue[0] = true;
        String[] list = list(path);
        File[] files = new File(path).listFiles();
        for (String s : list) {
            for (File file : files) {
                if (file.getAbsolutePath().endsWith(s)) {
                    new AlertDialog.Builder(this).setMessage("File is already exists, do you want to continue ?")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    isContinue[0] = true;
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    isContinue[0] = false;
                                }
                            })
                            .create();
                }
            }
        }
        return isContinue[0];
    }

    public static String[] list(String file) {
        Runtime runtime = Runtime.getRuntime();
        Process pro;
        BufferedReader in = null;
        boolean isPrint = false;
        boolean isRar = file.toLowerCase().endsWith("rar");
        ArrayList<String> fileList = new ArrayList<>();
        try {
            if (!isRar) {
                pro = runtime.exec(new String[]{"/system/bin/7za", "l", file});
            } else {
                pro = runtime.exec(new String[]{"/system/bin/unrar", "v", file});
            }
            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line;
            int row = 1;
            while ((line = in.readLine()) != null) {
                if (isPrint) {
                    if (line.contains("-----")) {
                        isPrint = false;
                        continue;
                    }
                    if (!isRar) {
                        line = line.substring(CompressUtils.INDEX_7Z_FILENAME);
                        System.out.println(line);
                        if (line.contains("/")) {
                            line = line.replace(line.substring(line.indexOf("/")), "");
                            if (!fileList.contains(line)) {
                                fileList.add(line);
                            }
                        } else {
                            fileList.add(line);
                        }
                    } else if (row % 2 != 0) {
                        fileList.add(line.substring(1));
                    }
                    row++;
                }
                if (line.contains("-----")) {
                    isPrint = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return fileList.toString().substring(1, fileList.toString().length() - 1).split(", ");
    }
}
