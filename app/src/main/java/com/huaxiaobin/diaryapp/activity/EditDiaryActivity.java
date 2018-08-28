package com.huaxiaobin.diaryapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class EditDiaryActivity extends AppCompatActivity {

    private int ADD_STATE = 0;
    private int EDIT_STATE = 1;
    private int STATE = ADD_STATE;

    private int id;
    private TextView wordCount;
    private EditText content;
    private ActionBar actionBar;
    private Button return_btn;
    private Button save_btn;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_diary);
        findView();
        init();
    }

    private void findView() {
        wordCount = (TextView) findViewById(R.id.wordCount);
        content = (EditText) findViewById(R.id.content);
        actionBar = getSupportActionBar();
    }

    /**
     * 初始化控件
     */
    private void init() {
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.actionbar_edit_diary);
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
                if (content.getText().toString().trim().equals("")) {
                    Toast.makeText(EditDiaryActivity.this, "内容不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    if (STATE == ADD_STATE) {
                        addDiary();
                    } else {
                        updateDiary();
                    }
                }
            }
        });
        STATE = getIntent().getIntExtra("state", ADD_STATE);
        id = getIntent().getIntExtra("id", -1);
        if (STATE == EDIT_STATE) {
            content.setText(getIntent().getStringExtra("content"));
        }
        wordCount.setText(String.valueOf(content.getText().length()));
        content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                wordCount.setText(String.valueOf(content.getText().length()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


    /**
     * 添加日记
     */
    private void addDiary() {
        String url = Config.SERVER_HOST + "/add_diary";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(EditDiaryActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(EditDiaryActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("user_id", String.valueOf(id));
                map.put("content", content.getText().toString());
                map.put("datetime", String.valueOf(System.currentTimeMillis()));
                return map;
            }
        };
        MyApplication.getQueue().add(stringRequest);
    }

    /**
     * 修改日记
     */
    private void updateDiary() {
        String url = Config.SERVER_HOST + "/update_diary";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(EditDiaryActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.putExtra("content", content.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(EditDiaryActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("diary_id", String.valueOf(id));
                map.put("content", content.getText().toString());
                return map;
            }
        };
        MyApplication.getQueue().add(stringRequest);
    }
}
