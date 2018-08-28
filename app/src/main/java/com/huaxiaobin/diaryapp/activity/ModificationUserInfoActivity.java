package com.huaxiaobin.diaryapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.huaxiaobin.diaryapp.R;
import com.huaxiaobin.diaryapp.application.MyApplication;
import com.huaxiaobin.diaryapp.utils.Config;


import java.util.HashMap;
import java.util.Map;

public class ModificationUserInfoActivity extends AppCompatActivity {

    private int id;
    private ActionBar actionBar;
    private EditText username;
    private Button return_btn;
    private Button save_btn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modification_user_info);
        findView();
        init();
    }

    private void findView() {
        username = (EditText) findViewById(R.id.username);
        actionBar = getSupportActionBar();
    }

    /**
     * 初始化控件
     */
    private void init() {
        id = getIntent().getIntExtra("id", -1);
        username.setText(getIntent().getStringExtra("username"));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.actionbar_modification_user_info);
        return_btn = (Button) actionBar.getCustomView().findViewById(R.id.return_btn);
        return_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
        save_btn = (Button) actionBar.getCustomView().findViewById(R.id.save_btn);
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (username.getText().toString().trim().equals("")) {
                    Toast.makeText(ModificationUserInfoActivity.this, "昵称不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    update_info();
                }
            }
        });
    }

    /**
     * 用户修改昵称
     */
    private void update_info() {
        String url = Config.SERVER_HOST + "/update_info";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(ModificationUserInfoActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.putExtra("username", username.getText().toString().trim());
                setResult(RESULT_OK, intent);
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ModificationUserInfoActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("user_id", String.valueOf(id));
                map.put("username", username.getText().toString().trim());
                return map;
            }
        };
        MyApplication.getQueue().add(stringRequest);
    }
}
