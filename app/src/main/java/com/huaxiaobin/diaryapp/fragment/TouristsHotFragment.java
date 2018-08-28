package com.huaxiaobin.diaryapp.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.huaxiaobin.diaryapp.R;
import com.huaxiaobin.diaryapp.activity.TouristsDetailActivity;
import com.huaxiaobin.diaryapp.application.MyApplication;
import com.huaxiaobin.diaryapp.utils.BitmapToBase64;
import com.huaxiaobin.diaryapp.utils.Config;
import com.huaxiaobin.diaryapp.utils.CustomListView;
import com.huaxiaobin.diaryapp.utils.DateTimeTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class TouristsHotFragment extends Fragment {

    private int id;

    private View view;
    private CustomListView listView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SimpleAdapter simpleAdapter;
    private ArrayList<Map<String, Object>> data = new ArrayList<>();
    private int page = 1;

    public static TouristsHotFragment newInstance(int id) {
        Bundle args = new Bundle();
        args.putInt("id", id);
        TouristsHotFragment fragment = new TouristsHotFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_tourists_hot, container, false);
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
                Intent intent = new Intent(getActivity(), TouristsDetailActivity.class);
                intent.putExtra("id", map.get("id").toString());
                intent.putExtra("icon", map.get("icon").toString());
                intent.putExtra("username", map.get("username").toString());
                intent.putExtra("timestamp", map.get("timestamp").toString());
                intent.putExtra("content", map.get("content").toString());
                intent.putExtra("self", Integer.valueOf(map.get("user_id").toString()) == getArguments().getInt("id") ? true : false);
                startActivity(intent);
            }
        });
        listView.setOnPullToRefreshListener(new CustomListView.OnPullToRefreshListener() {
            @Override
            public void onBottom() {
                fetchNewDiaryData();
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 1;
                data.clear();
                fetchNewDiaryData();
            }
        });
    }

    /**
     * 填充listView中的数据
     */
    private void fillData() {
        String[] from = {"icon", "username", "content", "time"};
        int[] to = {R.id.icon, R.id.username, R.id.content, R.id.time};
        simpleAdapter = new SimpleAdapter(getActivity(), data, R.layout.item_tourists_diary, from, to) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v;
                v = super.getView(position, convertView, parent);
                if (v != null) {
                    LinearLayout linearLayout = (LinearLayout) ((LinearLayout) v).getChildAt(0);
                    GradientDrawable drawable = (GradientDrawable) linearLayout.getBackground();
                    drawable.setColor(Color.parseColor(com.huaxiaobin.diaryapp.utils.Color.getDiaryColor(position, 4)));
                    linearLayout.setBackgroundDrawable(drawable);

                    String base64 = (String) data.get(position).get("icon");
                    LinearLayout topLinearLayout = (LinearLayout) linearLayout.getChildAt(0);
                    ImageView imageView = (ImageView) topLinearLayout.getChildAt(0);
                    if (base64.equals("")) {
                        imageView.setImageResource(R.mipmap.default_header);
                    } else {
                        imageView.setImageBitmap(BitmapToBase64.base64ToBitmap(base64));
                    }
                }
                return v;
            }
        };
        listView.setAdapter(simpleAdapter);
        fetchNewDiaryData();
    }

    /**
     * 获取最热的日记数据
     */
    private void fetchNewDiaryData() {
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
                map.put("fetch_type", String.valueOf(1));
                return map;
            }
        };
        MyApplication.getQueue().add(stringRequest);
    }

    /**
     * 解析json格式的最新日记数据
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
                map.put("icon", row.get(9).toString());
                map.put("username", row.get(7).toString());
                map.put("content", row.get(2).toString());
                map.put("time", DateTimeTools.getTimeInterval(timestamp));
                map.put("timestamp", timestamp);
                map.put("user_id", row.get(1).toString());
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

}
