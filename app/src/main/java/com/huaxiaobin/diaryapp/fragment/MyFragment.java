package com.huaxiaobin.diaryapp.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.huaxiaobin.diaryapp.R;
import com.huaxiaobin.diaryapp.activity.AboutActivity;
import com.huaxiaobin.diaryapp.activity.LoginActivity;
import com.huaxiaobin.diaryapp.activity.ModificationUserInfoActivity;
import com.huaxiaobin.diaryapp.application.MyApplication;
import com.huaxiaobin.diaryapp.utils.BitmapToBase64;
import com.huaxiaobin.diaryapp.utils.Config;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.huaxiaobin.diaryapp.utils.BitmapToBase64.bitmapToBase64;


public class MyFragment extends Fragment {

    private int user_id;
    private Bitmap headerBitmap;
    private String bitmapBase64;

    private View view;
    private ImageView header_img;
    private ListView listView;
    private Button quit_btn;
    private Dialog bottomDialog;
    private TextView select_pic;
    private TextView exit_btn;
    private TextView username_text;

    public static MyFragment newInstance(int id, String username, String header_img) {
        Bundle args = new Bundle();
        args.putInt("id", id);
        args.putString("username", username);
        args.putString("header_img", header_img);
        MyFragment fragment = new MyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findView();
        init();
        setMenu();
    }

    private void findView() {
        header_img = (ImageView) view.findViewById(R.id.header_img);
        quit_btn = (Button) view.findViewById(R.id.quit_btn);
        listView = (ListView) view.findViewById(R.id.listView);
        username_text = (TextView) view.findViewById(R.id.username);
    }

    /**
     * 初始化控件
     */
    private void init() {
        Bitmap bitmap = id2Bitmap(R.mipmap.default_header);
        setHeaderImage(bitmap);
        header_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHeaderImageDialog();
            }
        });
        username_text.setText(getArguments().getString("username"));
        quit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showExitDialog();
            }
        });
        user_id = getArguments().getInt("id");
        bitmapBase64 = getArguments().getString("header_img");
        if (!bitmapBase64.equals("")) {
            setHeaderImage(BitmapToBase64.base64ToBitmap(bitmapBase64));
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (id == 0) {
                    Intent intent = new Intent(getActivity(), ModificationUserInfoActivity.class);
                    intent.putExtra("id", user_id);
                    intent.putExtra("username", username_text.getText().toString());
                    startActivityForResult(intent, 1);
                } else if (id == 1) {
                    startActivity(new Intent(getActivity(), AboutActivity.class));
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            username_text.setText(data.getStringExtra("username"));
        }
        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            /*
             * 当调用相册选择一张图片后，将图片信息通过data传过来
             * data.getData()获取到图片URI
             */
            beginCrop(data.getData());
        }
        if (requestCode == Crop.REQUEST_CROP) {
            //调用Crop.start后来到这
            handleCrop(resultCode, data);
        }
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getActivity().getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(getActivity(), MyFragment.this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            //如果裁剪正常，resultCode == RESULT_OK则到这里裁剪完成
            try {
                headerBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Crop.getOutput(result));
            } catch (IOException e) {
                e.printStackTrace();
            }
            uploadHeaderImage();
            setHeaderImage(headerBitmap);
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(getActivity(), "裁截错误", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadHeaderImage() {
        String url = Config.SERVER_HOST + "/upload_image";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                String image = bitmapToBase64(headerBitmap);
                Map<String, String> map = new Hashtable<>();
                map.put("user_id", String.valueOf(user_id));
                map.put("image", image);
                return map;
            }
        };
        MyApplication.getQueue().add(stringRequest);
    }

    /**
     * 设置listView中的菜单
     */
    private void setMenu() {
        ArrayList<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("icon", R.drawable.ic_user);
        map.put("title", "资料修改");
        data.add(map);
        map = new HashMap<>();
        map.put("icon", R.drawable.ic_feedback);
        map.put("title", "关于印象");
        data.add(map);
        String[] from = {"icon", "title"};
        int[] to = {R.id.icon, R.id.title};
        SimpleAdapter adapter = new SimpleAdapter(getActivity(), data, R.layout.item_my_menu, from, to);
        listView.setAdapter(adapter);
    }

    private void showHeaderImageDialog() {
        bottomDialog = new Dialog(getActivity(), R.style.BottomDialog);
        bottomDialog.setCanceledOnTouchOutside(true);
        View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_header_img, null);
        select_pic = (TextView) contentView.findViewById(R.id.select);
        select_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomDialog.dismiss();
                Crop.pickImage(getActivity(), MyFragment.this);
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
        params.width = getResources().getDisplayMetrics().widthPixels - dp2px(getActivity(), 16f);
        params.bottomMargin = dp2px(getActivity(), 8f);
        contentView.setLayoutParams(params);
        bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        bottomDialog.show();
    }

    private void showExitDialog() {
        bottomDialog = new Dialog(getActivity(), R.style.BottomDialog);
        bottomDialog.setCanceledOnTouchOutside(true);
        View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_logout, null);
        exit_btn = (TextView) contentView.findViewById(R.id.sure);
        exit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
                SharedPreferences login = getActivity().getSharedPreferences("login", 0);
                SharedPreferences.Editor editor = login.edit();
                editor.putBoolean("isLogin", false);
                editor.commit();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
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
        params.width = getResources().getDisplayMetrics().widthPixels - dp2px(getActivity(), 16f);
        params.bottomMargin = dp2px(getActivity(), 8f);
        contentView.setLayoutParams(params);
        bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        bottomDialog.show();
    }

    private int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal,
                context.getResources().getDisplayMetrics());
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
