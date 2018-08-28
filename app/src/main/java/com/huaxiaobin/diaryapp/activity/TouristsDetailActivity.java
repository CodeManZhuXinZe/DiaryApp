package com.huaxiaobin.diaryapp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.huaxiaobin.diaryapp.R;
import com.huaxiaobin.diaryapp.application.MyApplication;
import com.huaxiaobin.diaryapp.utils.BitmapToBase64;
import com.huaxiaobin.diaryapp.utils.Config;
import com.huaxiaobin.diaryapp.utils.DateTimeTools;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TouristsDetailActivity extends AppCompatActivity {

    private int id;
    private ImageView header_img;
    private TextView username;
    private TextView time;
    private TextView content;
    private TextView back_btn;
    private TextView browse;
    private Boolean self;
    private long timestamp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tourists_detail);
        findView();
        init();
        fillData();
    }

    /**
     * 填充数据
     */
    private void fillData() {
        Intent intent = getIntent();
        String base64 = intent.getStringExtra("icon");
        if (base64.equals("")) {
            setHeaderImage(id2Bitmap(R.mipmap.default_header));
        } else {
            setHeaderImage(BitmapToBase64.base64ToBitmap(base64));
        }
        id = Integer.valueOf(intent.getStringExtra("id"));
        self = intent.getBooleanExtra("self", false);
        username.setText(intent.getStringExtra("username"));
        timestamp = Long.parseLong(intent.getStringExtra("timestamp"));
        time.setText(DateTimeTools.getDateTime(timestamp));
        content.setText(intent.getStringExtra("content"));
        fetchBrowseNum();
    }

    private void findView() {
        header_img = (ImageView) findViewById(R.id.header_img);
        username = (TextView) findViewById(R.id.username);
        time = (TextView) findViewById(R.id.time);
        content = (TextView) findViewById(R.id.content);
        back_btn = (TextView) findViewById(R.id.back);
        browse = (TextView) findViewById(R.id.browse);
    }

    /**
     * 初始化控件
     */
    private void init() {
        content.setMovementMethod(ScrollingMovementMethod.getInstance());
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }

    /**
     * 增加一个当前日记的浏览量
     */
    private void addBrowseNum() {
        String url = Config.SERVER_HOST + "/add_page_view";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(TouristsDetailActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("diary_id", String.valueOf(id));
                return map;
            }
        };
        MyApplication.getQueue().add(stringRequest);
    }

    /**
     * 获取日记的浏览量
     */
    private void fetchBrowseNum() {
        String url = Config.SERVER_HOST + "/fetch_page_view";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    browse.setText(json.getString("data"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (!self) {
                    addBrowseNum();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(TouristsDetailActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("diary_id", String.valueOf(id));
                return map;
            }
        };
        MyApplication.getQueue().add(stringRequest);
    }

    /**
     * 点击手机上的回退按钮事件绑定
     */
    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    /**
     * 设置头像图片
     */
    private void setHeaderImage(Bitmap bitmap) {
        RoundedBitmapDrawable circleDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
        circleDrawable.setCircular(true);
        header_img.setImageDrawable(circleDrawable);
    }

    private Bitmap id2Bitmap(int id) {
        return BitmapFactory.decodeResource(getResources(), id);
    }
}
