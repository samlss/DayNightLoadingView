package com.iigo.daynightloadingview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.iigo.library.DayNightLoadingView;

/**
 * @author SamLeung
 * @e-mail 729717222@qq.com
 * @github https://github.com/samlss
 * @csdn https://blog.csdn.net/Samlss
 * @description Use the colors of the loading view.
 */
public class SetAttrsActivity extends AppCompatActivity {
    private DayNightLoadingView dayNightLoadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_attr);

        dayNightLoadingView = findViewById(R.id.dn_loading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dayNightLoadingView.stop();
    }

    public void onResume(View view) {
        dayNightLoadingView.resume();
    }

    public void onPause(View view) {
        dayNightLoadingView.pause();
    }
}
