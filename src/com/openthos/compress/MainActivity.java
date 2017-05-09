package com.openthos.compress;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    private Button mCompress, mDecompress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCompress = (Button) findViewById(R.id.compress);
        mDecompress = (Button) findViewById(R.id.decompress);

        ButtonClickListener listener = new ButtonClickListener();
        mCompress.setOnClickListener(listener);
        mDecompress.setOnClickListener(listener);
    }

    class ButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.compress:
                    startActivity(new Intent(MainActivity.this, CompressActivity.class));
                    break;
                case R.id.decompress:
                    startActivity(new Intent(MainActivity.this, DecompressActivity.class));
                    break;
            }
        }
    }
}
