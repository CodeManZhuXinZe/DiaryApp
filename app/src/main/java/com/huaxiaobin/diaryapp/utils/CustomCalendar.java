package com.huaxiaobin.diaryapp.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.huaxiaobin.diaryapp.R;
import com.huaxiaobin.diaryapp.activity.CalendarActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomCalendar extends View {
    private int mBgColorDate, mTextColorDate, mTextColorFinish, mSelectWeekTextColor, mSelectTextColor, mSelectBg, mCurrentBg;
    private int mMonthRowL, mMonthRowR;
    private float mMonthRowSpac, mMonthSpac, mLineSpac, mTextSpac, mTextSizeMonth, mTextSizeWeek, mTextSizeDay, titleHeight, weekHeight, dayHeight, oneHeight, mSelectRadius, mCurrentBgStrokeWidth;
    private float[] mCurrentBgDashPath;
    private int currentDay, selectDay, lastSelectDay;
    private int dayOfMonth, firstIndex, todayWeekIndex;
    private int firstLineNum, lastLineNum, lineNum;
    private int columnWidth;
    private Paint mPaint, bgPaint;
    private Date month;
    private boolean isCurrentMonth;
    private String[] WEEK_STR = new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    public CustomCalendar(Context context) {
        this(context, null);
    }

    public CustomCalendar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomCalendar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CustomCalendar, defStyleAttr, 0);
        mBgColorDate = typedArray.getColor(R.styleable.CustomCalendar_mBgColorDate, Color.TRANSPARENT);
        mTextColorDate = typedArray.getColor(R.styleable.CustomCalendar_mTextColorDate, Color.BLACK);
        mTextColorFinish = typedArray.getColor(R.styleable.CustomCalendar_mTextColorFinish, Color.BLUE);
        mSelectWeekTextColor = typedArray.getColor(R.styleable.CustomCalendar_mSelectWeekTextColor, Color.BLACK);
        mSelectTextColor = typedArray.getColor(R.styleable.CustomCalendar_mSelectTextColor, Color.YELLOW);
        mSelectBg = typedArray.getColor(R.styleable.CustomCalendar_mSelectBg, Color.YELLOW);
        mCurrentBg = typedArray.getColor(R.styleable.CustomCalendar_mCurrentBg, Color.GRAY);
        mMonthRowL = typedArray.getResourceId(R.styleable.CustomCalendar_mMonthRowL, R.drawable.custom_calendar_row_left);
        mMonthRowR = typedArray.getResourceId(R.styleable.CustomCalendar_mMonthRowR, R.drawable.custom_calendar_row_right);
        mMonthRowSpac = typedArray.getDimension(R.styleable.CustomCalendar_mMonthRowSpac, 20);
        mMonthSpac = typedArray.getDimension(R.styleable.CustomCalendar_mMonthSpac, 20);
        mLineSpac = typedArray.getDimension(R.styleable.CustomCalendar_mLineSpac, 20);
        mTextSpac = typedArray.getDimension(R.styleable.CustomCalendar_mTextSpac, 20);
        mTextSizeMonth = typedArray.getDimension(R.styleable.CustomCalendar_mTextSizeMonth, 100);
        mTextSizeWeek = typedArray.getDimension(R.styleable.CustomCalendar_mTextSizeWeek, 70);
        mTextSizeDay = typedArray.getDimension(R.styleable.CustomCalendar_mTextSizeDay, 70);
        mSelectRadius = typedArray.getDimension(R.styleable.CustomCalendar_mSelectRadius, 20);
        mCurrentBgStrokeWidth = typedArray.getDimension(R.styleable.CustomCalendar_mCurrentBgStrokeWidth, 5);
        try {
            int dashPathId = typedArray.getResourceId(R.styleable.CustomCalendar_mCurrentBgDashPath, R.array.customCalendar_currentDay_bg_DashPath);
            int[] array = getResources().getIntArray(dashPathId);
            mCurrentBgDashPath = new float[array.length];
            for (int i = 0; i < array.length; i++) {
                mCurrentBgDashPath[i] = array[i];
            }
        } catch (Exception e) {
            e.printStackTrace();
            mCurrentBgDashPath = new float[]{2, 3, 2, 3};
        }

        typedArray.recycle();
        init();
    }

    private void init() {
        mPaint = new Paint();
        bgPaint = new Paint();
        mPaint.setAntiAlias(true);
        bgPaint.setAntiAlias(true);
        map = new HashMap<>();
        mPaint.setTextSize(mTextSizeMonth);
        titleHeight = FontUtil.getFontHeight(mPaint) + 2 * mMonthSpac;
        mPaint.setTextSize(mTextSizeWeek);
        weekHeight = FontUtil.getFontHeight(mPaint);
        mPaint.setTextSize(mTextSizeDay);
        dayHeight = FontUtil.getFontHeight(mPaint);
//        每行高度 = 行间距 + 日期字体高度 + 字间距 + 次数字体高度
        oneHeight = mLineSpac + dayHeight + mTextSpac;
//        默认当前月份
        String currentDate = getMonthStr(new Date());
        setMonth(currentDate);
    }

    private void setMonth(String Month) {
        month = strDate(Month);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        todayWeekIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        Date currentMonth = strDate(getMonthStr(new Date()));

        if (currentMonth.getTime() == month.getTime()) {
            isCurrentMonth = true;
            selectDay = currentDay;
        } else {
            isCurrentMonth = false;
            selectDay = 0;
        }

        calendar.setTime(month);
        dayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
//        第一行1号显示在什么位置（星期几）
        firstIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        lineNum = 1;
//        第一行能展示的天数
        firstLineNum = 7 - firstIndex;
        lastLineNum = 0;
        int remainder = dayOfMonth - firstLineNum;
        while (remainder > 7) {
            lineNum++;
            remainder -= 7;
        }
        if (remainder > 0) {
            lineNum++;
            lastLineNum = remainder;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        columnWidth = widthSize / 7;
//        高度 = 标题高度 + 星期高度 + (日期行数 * 每行高度)
        float height = titleHeight + weekHeight + (lineNum * oneHeight);
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), (int) height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawMonth(canvas);
        drawWeek(canvas);
        drawDayAndPre(canvas);
    }

    private int rowLStart, rowRStart, rowWidth;

    private void drawMonth(Canvas canvas) {
        bgPaint.setColor(mBgColorDate);
        RectF rect = new RectF(0, 0, getWidth(), titleHeight);
        canvas.drawRect(rect, bgPaint);
        mPaint.setTextSize(mTextSizeMonth);
        mPaint.setColor(mTextColorDate);
        float textLen = FontUtil.getFontlength(mPaint, getMonthStr(month));
        float textStart = (getWidth() - textLen) / 2;
        canvas.drawText(getMonthStr(month), textStart, mMonthSpac + FontUtil.getFontLeading(mPaint), mPaint);
//        绘制左右箭头
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), mMonthRowL);
        int h = bitmap.getHeight();
        rowWidth = bitmap.getWidth();
        rowLStart = (int) (textStart - 2 * mMonthRowSpac - rowWidth);
        canvas.drawBitmap(bitmap, rowLStart + mMonthRowSpac, (titleHeight - h) / 2, new Paint());
        bitmap = BitmapFactory.decodeResource(getResources(), mMonthRowR);
        rowRStart = (int) (textStart + textLen);
        canvas.drawBitmap(bitmap, rowRStart + mMonthRowSpac, (titleHeight - h) / 2, new Paint());
    }

    private void drawWeek(Canvas canvas) {
        bgPaint.setColor(mBgColorDate);
        RectF rect = new RectF(0, titleHeight, getWidth(), titleHeight + weekHeight);
        canvas.drawRect(rect, bgPaint);
        mPaint.setTextSize(mTextSizeWeek);

        for (int i = 0; i < WEEK_STR.length; i++) {
            if (todayWeekIndex == i && isCurrentMonth) {
                mPaint.setColor(mSelectWeekTextColor);
            } else {
                mPaint.setColor(mTextColorDate);
            }
            int len = (int) FontUtil.getFontlength(mPaint, WEEK_STR[i]);
            int x = i * columnWidth + (columnWidth - len) / 2;
            canvas.drawText(WEEK_STR[i], x, titleHeight + FontUtil.getFontLeading(mPaint), mPaint);
        }
    }

    private void drawDayAndPre(Canvas canvas) {
//        某行开始绘制的Y坐标，第一行开始的坐标为标题高度 + 星期部分高度
        float top = titleHeight + weekHeight;
//        行
        for (int line = 0; line < lineNum; line++) {
            if (line == 0) {
//                第一行
                drawDayAndPre(canvas, top, firstLineNum, 0, firstIndex);
            } else if (line == lineNum - 1) {
//                最后一行
                top += oneHeight;
                drawDayAndPre(canvas, top, lastLineNum, firstLineNum + (line - 1) * 7, 0);
            } else {
//                满行
                top += oneHeight;
                drawDayAndPre(canvas, top, 7, firstLineNum + (line - 1) * 7, 0);
            }
        }
    }

    private void drawDayAndPre(Canvas canvas, float top, int count, int overDay, int startIndex) {
//        背景
        float topPre = top + mLineSpac + dayHeight;
        bgPaint.setColor(mBgColorDate);
        RectF rect = new RectF(0, top, getWidth(), topPre);
        canvas.drawRect(rect, bgPaint);
        mPaint.setTextSize(mTextSizeDay);
        float dayTextLeading = FontUtil.getFontLeading(mPaint);

        for (int i = 0; i < count; i++) {
            int left = (startIndex + i) * columnWidth;
            int day = (overDay + i + 1);
            mPaint.setTextSize(mTextSizeDay);
//            如果是当前月，当天日期需要做处理
            if (isCurrentMonth && currentDay == day) {
                mPaint.setColor(mTextColorDate);
                bgPaint.setColor(mCurrentBg);
                bgPaint.setStyle(Paint.Style.STROKE);  //空心
                PathEffect effect = new DashPathEffect(mCurrentBgDashPath, 1);
                bgPaint.setPathEffect(effect);   //设置画笔曲线间隔
                bgPaint.setStrokeWidth(mCurrentBgStrokeWidth);       //画笔宽度
//                绘制空心圆背景
                canvas.drawCircle(left + columnWidth / 2, top + mLineSpac + dayHeight / 2, mSelectRadius - mCurrentBgStrokeWidth, bgPaint);
            }
//            绘制完后将画笔还原，避免脏笔
            bgPaint.setPathEffect(null);
            bgPaint.setStrokeWidth(0);
            bgPaint.setStyle(Paint.Style.FILL);
            //绘制次数
            CalendarActivity.DayFinish finish = map.get(day);
            if (isCurrentMonth) {
                if (day > currentDay) {
                    mPaint.setColor(mTextColorDate);
                } else if (finish != null) {
                    if (finish.finish > 0) {
                        mPaint.setColor(mSelectTextColor);
                        bgPaint.setColor(mTextColorFinish);
                        canvas.drawCircle(left + columnWidth / 2, top + mLineSpac + dayHeight / 2, mSelectRadius, bgPaint);
                    } else {
                        mPaint.setColor(mTextColorDate);
                    }
                } else {
                    mPaint.setColor(mTextColorDate);
                }
            } else {
                if (finish != null) {
                    if (finish.finish > 0) {
                        mPaint.setColor(mSelectTextColor);
                        bgPaint.setColor(mTextColorFinish);
                        canvas.drawCircle(left + columnWidth / 2, top + mLineSpac + dayHeight / 2, mSelectRadius, bgPaint);
                    } else {
                        mPaint.setColor(mTextColorDate);
                    }
                } else {
                    mPaint.setColor(mTextColorDate);
                }
            }
//            选中的日期，如果是本月，选中日期正好是当天日期，下面的背景会覆盖上面绘制的虚线背景
            if (selectDay == day) {
                //选中的日期字体白色，橙色背景
                mPaint.setColor(mSelectTextColor);
                bgPaint.setColor(mSelectBg);
//            绘制橙色圆背景，参数一是中心点的x轴，参数二是中心点的y轴，参数三是半径，参数四是paint对象；
                canvas.drawCircle(left + columnWidth / 2, top + mLineSpac + dayHeight / 2, mSelectRadius, bgPaint);
            } else {
                mPaint.setColor(mTextColorDate);
            }

            int len = (int) FontUtil.getFontlength(mPaint, day + "");
            int x = left + (columnWidth - len) / 2;
            canvas.drawText(day + "", x, top + mLineSpac + dayTextLeading, mPaint);
        }
    }

    /**
     * 获取月份标题
     */
    private String getMonthStr(Date month) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月");
        return df.format(month);
    }

    private Date strDate(String str) {
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月");
            return df.parse(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //焦点坐标
    private PointF focusPoint = new PointF();

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                focusPoint.set(event.getX(), event.getY());
                touchFocusMove(focusPoint, false);
                break;
            case MotionEvent.ACTION_MOVE:
                focusPoint.set(event.getX(), event.getY());
                touchFocusMove(focusPoint, false);
                break;
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                focusPoint.set(event.getX(), event.getY());
                touchFocusMove(focusPoint, true);
                break;
        }
        return true;
    }

    /**
     * 焦点滑动
     */
    public void touchFocusMove(final PointF point, boolean eventEnd) {
        /**标题和星期只有在事件结束后才响应*/
        if (point.y <= titleHeight) {
            //事件在标题上
            if (eventEnd && listener != null) {
                if (point.x >= rowLStart && point.x < (rowLStart + 2 * mMonthRowSpac + rowWidth)) {
                    listener.onLeftRowClick();
                } else if (point.x > rowRStart && point.x < (rowRStart + 2 * mMonthRowSpac + rowWidth)) {
                    listener.onRightRowClick();
                }
            }
        } else if (point.y <= (titleHeight + weekHeight)) {
            //事件在星期部分
            if (eventEnd && listener != null) {
                //根据X坐标找到具体的焦点日期
                int xIndex = (int) point.x / columnWidth;
                if ((point.x / columnWidth - xIndex) > 0) {
                    xIndex += 1;
                }
            }
        } else {
            /**日期部分按下和滑动时重绘，只有在事件结束后才响应*/
            touchDay(point, eventEnd);
        }
    }

    //控制事件是否响应
    private boolean responseWhenEnd = false;

    /**
     * 事件点在 日期区域 范围内
     */
    private void touchDay(final PointF point, boolean eventEnd) {
        //根据Y坐标找到焦点行
        boolean availability = false;  //事件是否有效
        //日期部分
        float top = titleHeight + weekHeight + oneHeight;
        int foucsLine = 1;
        while (foucsLine <= lineNum) {
            if (top >= point.y) {
                availability = true;
                break;
            }
            top += oneHeight;
            foucsLine++;
        }
        if (availability) {
            //根据X坐标找到具体的焦点日期
            int xIndex = (int) point.x / columnWidth;
            if ((point.x / columnWidth - xIndex) > 0) {
                xIndex += 1;
            }
            if (xIndex <= 0)
                xIndex = 1;   //避免调到上一行最后一个日期
            if (xIndex > 7)
                xIndex = 7;   //避免调到下一行第一个日期
            if (foucsLine == 1) {
                //第一行
                if (xIndex <= firstIndex) {
                    setSelectedDay(selectDay, true);
                } else {
                    setSelectedDay(xIndex - firstIndex, eventEnd);
                }
            } else if (foucsLine == lineNum) {
                //最后一行
                if (xIndex > lastLineNum) {
                    setSelectedDay(selectDay, true);
                } else {
                    setSelectedDay(firstLineNum + (foucsLine - 2) * 7 + xIndex, eventEnd);
                }
            } else {
                setSelectedDay(firstLineNum + (foucsLine - 2) * 7 + xIndex, eventEnd);
            }
        } else {
            //超出日期区域后，视为事件结束，响应最后一个选择日期的回调
            setSelectedDay(selectDay, true);
        }
    }

    /**
     * 设置选中的日期
     */
    private void setSelectedDay(int day, boolean eventEnd) {
        selectDay = day;
        invalidate();
        if (listener != null && eventEnd && responseWhenEnd && lastSelectDay != selectDay) {
            lastSelectDay = selectDay;
        }
        responseWhenEnd = !eventEnd;
    }

    @Override
    public void invalidate() {
        requestLayout();
        super.invalidate();
    }

    private Map<Integer, CalendarActivity.DayFinish> map;

    public void setTask(String month, int Month, List<CalendarActivity.DayFinish> postList) {
        setMonth(month);
        List<CalendarActivity.DayFinish> list = new ArrayList<>();

        for (int i = 0; i < postList.size(); i++) {
            if (postList.get(i).month == Month) {
                list.add(postList.get(i));
            }
        }

        if (list != null && list.size() > 0) {
            map.clear();
            for (CalendarActivity.DayFinish finish : list) {
                map.put(finish.day, finish);
            }
        }
        invalidate();
    }

    public void setTask(int Month, List<CalendarActivity.DayFinish> postList) {
        List<CalendarActivity.DayFinish> list = new ArrayList<>();

        for (int i = 0; i < postList.size(); i++) {
            if (postList.get(i).month == Month) {
                list.add(postList.get(i));
            }
        }

        if (list != null && list.size() > 0) {
            map.clear();
            for (CalendarActivity.DayFinish finish : list) {
                map.put(finish.day, finish);
            }
        }
        invalidate();
    }

    public void monthChange(int change) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(month);
        calendar.add(Calendar.MONTH, change);
        setMonth(getMonthStr(calendar.getTime()));
        map.clear();
        invalidate();
    }

    private onClickListener listener;

    public void setOnClickListener(onClickListener listener) {
        this.listener = listener;
    }

    public interface onClickListener {
        public abstract void onLeftRowClick();

        public abstract void onRightRowClick();
    }
}