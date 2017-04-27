package com.openthos.compress;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getIntent().getStringExtra("paths") != null) {
            startActivity(new Intent(MainActivity.this, CompressActivity.class).putExtra("paths", getIntent().getStringExtra("paths")));
        }
    }

    public void btClick(View view) {
        switch (view.getId()) {
            case R.id.bt1:
                startActivity(new Intent(MainActivity.this, CompressActivity.class));
                break;
            case R.id.bt2:
                startActivity(new Intent(MainActivity.this, DecompressActivity.class));
                break;
        }
    }
}
