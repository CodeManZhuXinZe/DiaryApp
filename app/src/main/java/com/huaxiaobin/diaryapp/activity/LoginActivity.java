package com.huaxiaobin.diaryapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
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

public class LoginActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private Button login;
    private TextView register;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findView();
        init();
    }

    private void findView() {
        email = (EditText) findViewById(R.id.email_edit);
        password = (EditText) findViewById(R.id.password_edit);
        login = (Button) findViewById(R.id.login_btn);
        register = (TextView) findViewById(R.id.register_btn);
    }

    /**
     * 初始化控件
     */
    private void init() {
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (email.getText().toString().trim().equals("") || password.getText().toString().trim().equals("")) {
                    Toast.makeText(LoginActivity.this, "邮箱和密码不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    login();
                }
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    /**
     * 用户登录
     */
    private void login() {
        String url = Config.SERVER_HOST + "/login";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getString("msg").equals("登录成功")) {
                        SharedPreferences login = getSharedPreferences("login", 0);
                        SharedPreferences.Editor editor = login.edit();
                        editor.putString("email", email.getText().toString());
                        editor.putString("password", MD5.GetMD5(password.getText().toString()));
                        editor.putBoolean("isLogin", true);
                        editor.commit();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        JSONObject data = json.getJSONObject("data");
                        intent.putExtra("id", data.getInt("id"));
                        intent.putExtra("username", data.getString("username"));
                        intent.putExtra("header_img", data.getString("header_img"));
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "账号不存在或密码错误", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LoginActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("email", email.getText().toString().trim());
                map.put("password", MD5.GetMD5(password.getText().toString().trim()));
                return map;
            }
        };
        MyApplication.getQueue().add(stringRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            email.setText(data.getStringExtra("email"));
        }
    }
}
