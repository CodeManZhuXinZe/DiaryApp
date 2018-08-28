package com.huaxiaobin.diaryapp.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.huaxiaobin.diaryapp.R;
import com.huaxiaobin.diaryapp.activity.ViewDiaryActivity;
import com.huaxiaobin.diaryapp.application.MyApplication;
import com.huaxiaobin.diaryapp.utils.Config;
import com.huaxiaobin.diaryapp.utils.CustomListView;
import com.huaxiaobin.diaryapp.utils.DateTimeTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;


public class DiaryFragment extends Fragment {

    private int id;

    private View view;
    private CustomListView listView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<Map<String, Object>> data = new ArrayList<>();
    private SimpleAdapter simpleAdapter;
    private int page = 1;

    public static DiaryFragment newInstance(int id) {
        Bundle args = new Bundle();
        args.putInt("id", id);
        DiaryFragment fragment = new DiaryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_diary, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findView();
        init();
        fillData();
    }

    private void findView() {
        listView = (CustomListView) view.findViewById(R.id.listView);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
    }

    /**
     * 初始化控件
     */
    private void init() {
        id = getArguments().getInt("id");
        listView.setDivider(null);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, Object> map = (HashMap<String, Object>) listView.getItemAtPosition(position);
                Intent intent = new Intent(getActivity(), ViewDiaryActivity.class);
                intent.putExtra("id", map.get("id").toString());
                intent.putExtra("diary_date", map.get("diary_date").toString());
                intent.putExtra("diary_week", map.get("diary_week").toString());
                intent.putExtra("diary_time", map.get("diary_time").toString());
                intent.putExtra("diary_content", map.get("diary_content").toString());
                startActivityForResult(intent, 1);
            }
        });
        listView.setOnPullToRefreshListener(new CustomListView.OnPullToRefreshListener() {
            @Override
            public void onBottom() {
                fetchDiaryData();
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 1;
                data.clear();
                fetchDiaryData();
            }
        });
    }

    /**
     * 填充listView中的数据
     */
    private void fillData() {
        String[] from = {"diary_week", "diary_date", "diary_content", "diary_time"};
        int[] to = {R.id.diary_week, R.id.diary_date, R.id.diary_content, R.id.diary_time};
        simpleAdapter = new SimpleAdapter(getActivity(), data, R.layout.item_diary, from, to);
        listView.setAdapter(simpleAdapter);
        fetchDiaryData();
    }

    /**
     * 获取日记数据
     */
    private void fetchDiaryData() {
        String url = Config.SERVER_HOST + "/fetch_diary";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseJsonObjectByDiary(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), "网络异常", Toast.LENGTH_SHORT).show();
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("user_id", String.valueOf(id));
                map.put("page", String.valueOf(page));
                map.put("fetch_type", String.valueOf(2));
                return map;
            }
        };
        MyApplication.getQueue().add(stringRequest);
    }

    /**
     * 解析json格式的日记数据并填充
     *
     * @param response json数据
     */
    private void parseJsonObjectByDiary(String response) {
        ArrayList<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> map;
        try {
            JSONObject json = new JSONObject(response);
            JSONArray all_data = json.getJSONArray("data");
            for (int i = 0; i < all_data.length(); i++) {
                JSONArray row = (JSONArray) all_data.get(i);
                long timestamp = Long.parseLong(row.get(4).toString());
                map = new HashMap<>();
                map.put("id", Integer.valueOf(row.get(0).toString()));
                map.put("diary_week", DateTimeTools.getWeek(timestamp));
                map.put("diary_date", DateTimeTools.getYearMonthDay(timestamp));
                map.put("diary_content", row.get(2).toString());
                map.put("diary_time", DateTimeTools.getTime(timestamp));
                result.add(map);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        data.addAll(result);
        simpleAdapter.notifyDataSetChanged();
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
        page++;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            refreshData();
        }
    }

    /**
     * 刷新当前数据
     */
    public void refreshData() {
        page = 1;
        this.data.clear();
        fetchDiaryData();
    }
}
