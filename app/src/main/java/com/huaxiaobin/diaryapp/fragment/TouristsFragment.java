package com.huaxiaobin.diaryapp.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.huaxiaobin.diaryapp.R;
import com.huaxiaobin.diaryapp.activity.MainActivity;


public class TouristsFragment extends Fragment {

    private int id;

    private View view;
    private ViewPager viewPager;
    private Fragment[] fragments;

    public static TouristsFragment newInstance(int id) {
        Bundle args = new Bundle();
        args.putInt("id", id);
        TouristsFragment fragment = new TouristsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_tourists, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findView();
        init();
    }

    private void findView() {
        viewPager = (ViewPager) view.findViewById(R.id.viewPager);
    }

    /**
     * 初始化控件
     */
    private void init() {
        id = getArguments().getInt("id");
        fragments = new Fragment[3];
        fragments[0] = TouristsNewFragment.newInstance(id);
        fragments[1] = TouristsHotFragment.newInstance(id);
        fragments[2] = TouristsMyFragment.newInstance(id);
        TouristsFragmentPagerAdapter touristsFragmentPagerAdapter = new TouristsFragmentPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(touristsFragmentPagerAdapter);
    }

    /**
     * 绑定tabLayout到viewPager上
     *
     * @param tabLayout tabLayout
     */
    public void setViewPager(TabLayout tabLayout) {
        tabLayout.setupWithViewPager(viewPager);
    }

    /**
     * 过客页面FragmentPager适配器
     */
    private class TouristsFragmentPagerAdapter extends FragmentPagerAdapter {

        String[] title = {getString(R.string.tabLayout_new), getString(R.string.tabLayout_hot), getString(R.string.tabLayout_my)};

        public TouristsFragmentPagerAdapter(FragmentManager fm) {
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

        @Override
        public CharSequence getPageTitle(int position) {
            return title[position];
        }
    }

}
