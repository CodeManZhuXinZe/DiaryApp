package com.huaxiaobin.diaryapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.huaxiaobin.diaryapp.R;
import com.huaxiaobin.diaryapp.application.MyApplication;
import com.huaxiaobin.diaryapp.utils.Config;
import com.huaxiaobin.diaryapp.utils.MD5;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class StartupPageActivity extends AppCompatActivity {

    private ImageView imageView;
    private Boolean loginSuccessed = false;
    private Intent intent;
    private AnimationSet animationSet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        animation();
        login();
    }

    private void animation() {
        imageView = (ImageView) findViewById(R.id.imageView);
        animationSet = new AnimationSet(true);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
        ScaleAnimation scaleAnimation = new ScaleAnimation(0.25f, 1, 0.25f, 1,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);
        animationSet.setStartOffset(3000);
        animationSet.setDuration(200);
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imageView.setVisibility(View.INVISIBLE);
                if (loginSuccessed) {
                    startActivity(intent);
                } else {
                    startActivity(new Intent(StartupPageActivity.this, LoginActivity.class));
                }
                overridePendingTransition(0, 0);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 用户登录
     */
    private void login() {
        SharedPreferences login_info = getSharedPreferences("login", 0);
        if (login_info.getBoolean("isLogin", false)) {
            String url = Config.SERVER_HOST + "/login";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getString("msg").equals("登录成功")) {
                            intent = new Intent(StartupPageActivity.this, MainActivity.class);
                            JSONObject data = json.getJSONObject("data");
                            intent.putExtra("id", data.getInt("id"));
                            intent.putExtra("username", data.getString("username"));
                            intent.putExtra("header_img", data.getString("header_img"));
                            loginSuccessed = true;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    imageView.startAnimation(animationSet);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(StartupPageActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                    imageView.startAnimation(animationSet);
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    SharedPreferences login_info = getSharedPreferences("login", 0);
                    String email = login_info.getString("email", "");
                    String password = login_info.getString("password", "");
                    HashMap<String, String> map = new HashMap<>();
                    map.put("email", email);
                    map.put("password", password);
                    return map;
                }
            };
            MyApplication.getQueue().add(stringRequest);
        } else {
            imageView.startAnimation(animationSet);
        }
    }
}
