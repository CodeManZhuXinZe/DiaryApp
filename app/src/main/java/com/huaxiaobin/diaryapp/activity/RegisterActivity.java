package com.huaxiaobin.diaryapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
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
import com.huaxiaobin.diaryapp.utils.MD5;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText email;
    private EditText username;
    private EditText password;
    private Button register;
    private TextView exit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        findView();
        init();
    }

    private void findView() {
        email = (EditText) findViewById(R.id.email_edit);
        username = (EditText) findViewById(R.id.username_edit);
        password = (EditText) findViewById(R.id.password_edit);
        register = (Button) findViewById(R.id.register_btn);
        exit = (TextView) findViewById(R.id.exit);
    }

    /**
     * 初始化控件
     */
    private void init() {
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String check = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
                Pattern regex = Pattern.compile(check);
                if (email.getText().toString().trim().equals("") || username.getText().toString().trim().equals("") || password.getText().toString().trim().equals("")) {
                    Toast.makeText(RegisterActivity.this, "邮箱、昵称、密码不能为空", Toast.LENGTH_SHORT).show();
                } else if (!regex.matcher(email.getText().toString().trim()).matches()) {
                    Toast.makeText(RegisterActivity.this, "邮箱格式不正确", Toast.LENGTH_SHORT).show();
                } else if (password.getText().toString().trim().length() < 6 || password.getText().toString().trim().length() > 12) {
                    Toast.makeText(RegisterActivity.this, "密码长度为6～12位", Toast.LENGTH_SHORT).show();
                } else {
                    register();
                }
            }
        });
        exit.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }

    /**
     * 用户注册
     */
    private void register() {
        String url = Config.SERVER_HOST + "/register";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getString("msg").equals("注册成功")) {
                        Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.putExtra("email",email.getText().toString().trim());
                        setResult(RESULT_OK, intent);
                        finish();
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    } else {
                        Toast.makeText(RegisterActivity.this, "该邮箱已经被注册了", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(RegisterActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("email", email.getText().toString().trim());
                map.put("username", username.getText().toString().trim());
                map.put("password", MD5.GetMD5(password.getText().toString().trim()));
                return map;
            }
        };
        MyApplication.getQueue().add(stringRequest);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
