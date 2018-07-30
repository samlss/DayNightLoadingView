package com.iigo.daynightloadingview;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import com.iigo.library.DayNightLoadingView;

public class MainActivity extends AppCompatActivity {
    private DayNightLoadingView dayNightLoadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    public void onStartSetAttrs(View view) {
        startActivity(new Intent(this, SetAttrsActivity.class));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Manually change the size
        //手动改变大小
        ViewGroup.LayoutParams layoutParams = dayNightLoadingView.getLayoutParams();
        layoutParams.width = 100;
        layoutParams.height = 50;
        dayNightLoadingView.setLayoutParams(layoutParams);
        return super.onKeyDown(keyCode, event);
    }
}
