package com.huaxiaobin.diaryapp.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.huaxiaobin.diaryapp.R;

public class AboutActivity extends AppCompatActivity {

    private ActionBar actionBar;
    private Button return_btn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        findView();
        init();
    }

    private void findView() {
        actionBar = getSupportActionBar();
    }

    /**
     * 初始化控件
     */
    private void init() {
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.actionbar_about);
        return_btn = (Button) actionBar.getCustomView().findViewById(R.id.return_btn);
        return_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }
}
