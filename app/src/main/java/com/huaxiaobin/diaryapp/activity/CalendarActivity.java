package com.huaxiaobin.diaryapp.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.huaxiaobin.diaryapp.R;
import com.huaxiaobin.diaryapp.application.MyApplication;
import com.huaxiaobin.diaryapp.utils.Config;
import com.huaxiaobin.diaryapp.utils.CustomCalendar;

public class CalendarActivity extends AppCompatActivity {

    private ActionBar actionBar;
    private Button return_btn;

    private CustomCalendar customCalendar;
    private int month;
    private List<DayFinish> list = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        findView();
        init();
        customCalendar = (CustomCalendar) findViewById(R.id.cal);
        fetchAllDiaryDate();

        customCalendar.setOnClickListener(new CustomCalendar.onClickListener() {
            @Override
            public void onLeftRowClick() {
                customCalendar.monthChange(-1);
                month = month - 1;
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            CalendarActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    customCalendar.setTask(month, list);
                                }
                            });
                        } catch (Exception e) {
                        }
                    }
                }.start();
            }

            @Override
            public void onRightRowClick() {
                customCalendar.monthChange(1);
                month = month + 1;
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            CalendarActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    customCalendar.setTask(month, list);
                                }
                            });
                        } catch (Exception e) {
                        }
                    }
                }.start();
            }
        });
    }

    private void findView() {
        actionBar = getSupportActionBar();
    }

    /**
     * 初始化控件
     */
    private void init() {
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.actionbar_calendar);
        return_btn = (Button) actionBar.getCustomView().findViewById(R.id.return_btn);
        return_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


    private void fetchAllDiaryDate() {
        String url = Config.SERVER_HOST + "/fetch_all_diary_date";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                SimpleDateFormat format = new SimpleDateFormat("MM-dd");
                JSONArray jsonArray = null;
                try {
                    jsonArray = (new JSONObject(response)).getJSONArray("data");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String[] date = new String[jsonArray.length()];
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        JSONArray jsonArray1 = jsonArray.getJSONArray(i);
                        date[i] = format.format(Long.parseLong(jsonArray1.getString(0)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                for (int i = 0; i < date.length; i++) {
                    int m = Integer.parseInt(date[i].substring(0, 2));
                    int d = Integer.parseInt(date[i].substring(3, 5));
                    list.add(new DayFinish(m, d, 1));
                }

                Calendar calendar = Calendar.getInstance();
                int nowYear = calendar.get(Calendar.YEAR);
                int nowMonth = calendar.get(Calendar.MONTH) + 1;
                customCalendar.setTask(String.valueOf(nowYear) + "年" + String.valueOf(nowMonth) + "月", nowMonth, list);
                month = nowMonth;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new Hashtable<>();
                map.put("user_id", String.valueOf(getIntent().getIntExtra("id", 0)));
                return map;
            }
        };
        MyApplication.getQueue().add(stringRequest);
    }

    public class DayFinish {
        public int month;
        public int day;
        public int finish;

        public DayFinish(int month, int day, int finish) {
            this.month = month;
            this.day = day;
            this.finish = finish;
        }
    }
}
