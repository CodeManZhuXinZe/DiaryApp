package com.huaxiaobin.diaryapp.utils;

public class Color {

    public static String getDiaryColor(int position, int temp) {
        String[] colors = {"#7c8691", "#958ec1", "#8ac79f", "#9dacd2", "#c0ce8c", "#89a4b0", "#bbc4c6", "#83adcf", "#87d0c0", "#ae8dc7", "#968b83"};
        return colors[(position + temp) % colors.length];
    }

}
