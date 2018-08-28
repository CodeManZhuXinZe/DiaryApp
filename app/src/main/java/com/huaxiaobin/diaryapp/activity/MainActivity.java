package com.huaxiaobin.diaryapp.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huaxiaobin.diaryapp.R;
import com.huaxiaobin.diaryapp.fragment.DiaryFragment;
import com.huaxiaobin.diaryapp.fragment.MyFragment;
import com.huaxiaobin.diaryapp.fragment.TouristsFragment;
import com.huaxiaobin.diaryapp.utils.CustomDialog;
import com.huaxiaobin.diaryapp.utils.CustomViewPager;


import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity {

    private int id;
    private String username;
    private String header_img;

    private Dialog bottomDialog;
    private TextView exit_btn;

    private CustomViewPager viewPager;
    private BottomNavigationView navigationView;
    private Fragment[] fragments;
    private ActionBar actionBar;

    private Button calendar_btn;
    private Button edit_btn;

    private TabLayout tabLayout;
    private Button info_btn;
    private CustomDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        init();
    }

    private void findView() {
        navigationView = (BottomNavigationView) findViewById(R.id.navigation);
        viewPager = (CustomViewPager) findViewById(R.id.viewPager);
        actionBar = getSupportActionBar();
    }

    /**
     * 初始化控件
     */
    private void init() {
        id = getIntent().getIntExtra("id", 0);
        username = getIntent().getStringExtra("username");
        header_img = getIntent().getStringExtra("header_img");
        fragments = new Fragment[3];
        fragments[0] = DiaryFragment.newInstance(id);
        fragments[1] = TouristsFragment.newInstance(id);
        fragments[2] = MyFragment.newInstance(id, username, header_img);
        BottomNavigationFragmentPagerAdapter bottomNavigationFragmentPagerAdapter = new BottomNavigationFragmentPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(bottomNavigationFragmentPagerAdapter);
        viewPager.setOffscreenPageLimit(3);
        navigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        initDiaryActionBar();
    }

    /**
     * 初始化日记页actionBar
     */
    private void initDiaryActionBar() {
        viewPager.setCurrentItem(0);
        actionBar.show();
        actionBar.setCustomView(R.layout.actionbar_diary);
        calendar_btn = (Button) actionBar.getCustomView().findViewById(R.id.calendar_btn);
        calendar_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
                intent.putExtra("id", id);
                startActivity(intent);
            }
        });
        edit_btn = (Button) actionBar.getCustomView().findViewById(R.id.edit_btn);
        edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditDiaryActivity.class);
                intent.putExtra("id", id);
                startActivityForResult(intent, 1);
            }
        });
    }

    /**
     * 初始化过客页actionBar
     */
    private void initTouristsActionBar() {
        viewPager.setCurrentItem(1);
        actionBar.show();
        actionBar.setCustomView(R.layout.actionbar_tourists);
        tabLayout = (TabLayout) actionBar.getCustomView().findViewById(R.id.tabLayout);
        ((TouristsFragment) fragments[1]).setViewPager(tabLayout);
        LinearLayout linearLayout = (LinearLayout) tabLayout.getChildAt(0);
        linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        linearLayout.setDividerDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.tablayout_line));
        info_btn = (Button) actionBar.getCustomView().findViewById(R.id.info_btn);
        info_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                info_dialog();
            }
        });
    }

    /**
     * 初始化我的页actionBar
     */
    private void initMyActionBar() {
        viewPager.setCurrentItem(2);
        actionBar.hide();
    }

    /**
     * FragmentPager适配器
     */
    private class BottomNavigationFragmentPagerAdapter extends FragmentPagerAdapter {

        public BottomNavigationFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            ((DiaryFragment) fragments[0]).refreshData();
        }
    }

    /**
     * 底部菜单栏点击事件监听
     */
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            disableActionBarShowHideAnimation(actionBar);
            switch (item.getItemId()) {
                //选择日记页
                case R.id.navigation_diary:
                    initDiaryActionBar();
                    return true;
                //选择过客页
                case R.id.navigation_tourists:
                    initTouristsActionBar();
                    return true;
                //选择我的页
                case R.id.navigation_my:
                    initMyActionBar();
                    return true;
            }
            return false;
        }
    };

    /**
     * 弹出信息Dialog
     */
    private void info_dialog() {
        CustomDialog.Builder builder = new CustomDialog.Builder(MainActivity.this);
        dialog = builder
                .style(R.style.Dialog)
                .heightDimenRes(R.dimen.dialog_info_height)
                .widthDimenRes(R.dimen.dialog_info_width)
                .cancelTouchout(true)
                .view(R.layout.dialog_info)
                .addViewOnclick(R.id.dialog, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                })
                .build();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        showExitDialog();
    }

    private void showExitDialog() {
        bottomDialog = new Dialog(MainActivity.this, R.style.BottomDialog);
        bottomDialog.setCanceledOnTouchOutside(true);
        View contentView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_exit, null);
        exit_btn = (TextView) contentView.findViewById(R.id.sure);
        exit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
        params.width = getResources().getDisplayMetrics().widthPixels - dp2px(MainActivity.this, 16f);
        params.bottomMargin = dp2px(MainActivity.this, 8f);
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
     * 取消actionbar隐藏显示时的动画
     *
     * @param actionBar 要取消动画的actionbar
     */
    public static void disableActionBarShowHideAnimation(ActionBar actionBar) {
        try {
            actionBar.getClass().getDeclaredMethod("setShowHideAnimationEnabled", boolean.class).invoke(actionBar, false);
        } catch (Exception exception) {
            try {
                Field mActionBarField = actionBar.getClass().getSuperclass().getDeclaredField("mActionBar");
                mActionBarField.setAccessible(true);
                Object icsActionBar = mActionBarField.get(actionBar);
                Field mShowHideAnimationEnabledField = icsActionBar.getClass().getDeclaredField("mShowHideAnimationEnabled");
                mShowHideAnimationEnabledField.setAccessible(true);
                mShowHideAnimationEnabledField.set(icsActionBar, false);
                Field mCurrentShowAnimField = icsActionBar.getClass().getDeclaredField("mCurrentShowAnim");
                mCurrentShowAnimField.setAccessible(true);
                mCurrentShowAnimField.set(icsActionBar, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
