package com.huaxiaobin.diaryapp.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class ViewDiaryActivity extends AppCompatActivity {

    private int id;
    private boolean isModification = false;
    private TextView diary_date;
    private TextView diary_time;
    private TextView diary_week;
    private TextView diary_content;
    private String content;
    private ActionBar actionBar;

    private Button return_btn;
    private Button delete_btn;
    private Button edit_btn;

    private Dialog bottomDialog;
    private TextView delete_sure_btn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_diary);
        findView();
        init();
    }

    private void findView() {
        diary_date = (TextView) findViewById(R.id.diary_date);
        diary_time = (TextView) findViewById(R.id.diary_time);
        diary_week = (TextView) findViewById(R.id.diary_week);
        diary_content = (TextView) findViewById(R.id.content);
        actionBar = getSupportActionBar();
    }

    /**
     * 初始化控件
     */
    private void init() {
        id = Integer.parseInt(getIntent().getStringExtra("id"));
        diary_date.setText(getIntent().getStringExtra("diary_date"));
        diary_time.setText(getIntent().getStringExtra("diary_time"));
        diary_week.setText(getIntent().getStringExtra("diary_week"));
        content = getIntent().getStringExtra("diary_content");
        diary_content.setText(content);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.actionbar_view_diary);
        return_btn = (Button) actionBar.getCustomView().findViewById(R.id.return_btn);
        delete_btn = (Button) actionBar.getCustomView().findViewById(R.id.delete_btn);
        edit_btn = (Button) actionBar.getCustomView().findViewById(R.id.edit_btn);
        return_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isModification) {
                    setResult(RESULT_OK);
                }
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog();
            }
        });
        edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewDiaryActivity.this, EditDiaryActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("content", content);
                intent.putExtra("state", 1);
                startActivityForResult(intent, 1);
            }
        });
    }

    private void showDeleteDialog() {
        bottomDialog = new Dialog(ViewDiaryActivity.this, R.style.BottomDialog);
        bottomDialog.setCanceledOnTouchOutside(true);
        View contentView = LayoutInflater.from(ViewDiaryActivity.this).inflate(R.layout.dialog_delete_diary, null);
        delete_sure_btn = (TextView) contentView.findViewById(R.id.sure);
        delete_sure_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDiary();
            }
        });
        TextView cancel = (TextView) contentView.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomDialog.dismiss();
            }
        });
        bottomDialog.setContentView(contentView);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) contentView.getLayoutParams();
        params.width = getResources().getDisplayMetrics().widthPixels - dp2px(ViewDiaryActivity.this, 16f);
        params.bottomMargin = dp2px(ViewDiaryActivity.this, 8f);
        contentView.setLayoutParams(params);
        bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        bottomDialog.show();
    }

    private int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal,
                context.getResources().getDisplayMetrics());
    }

    @Override
    public void onBackPressed() {
        if (isModification) {
            setResult(RESULT_OK);
        }
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            diary_content.setText(data.getStringExtra("content"));
            isModification = true;
        }
    }

    /**
     * 删除日记
     */
    private void deleteDiary() {
        String url = Config.SERVER_HOST + "/delete_diary";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(ViewDiaryActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ViewDiaryActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
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

}
