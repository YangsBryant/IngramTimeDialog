package com.bryant.ingramtimedialoglibrary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * 滚动选择器
 */
public class ScrollSelector extends View {

    //滚动监听
    private ScrollClickListener scrollClickListener;

    /**
     * 获取选中项
     */
    public int getSelectedIndex(){
        if((int) (-offsetY + 0.5) < contents.size()) {
            return (int) (-offsetY + 0.5);
        }else{
            return contents.size()-1;
        }
    }

    /**
     * 设置选中项
     */
    public void setSelectedIndex(int pos){
       // if (pos < 0 || pos >= contents.size()) return;
        offsetY = -pos + 1;
    }

    /**
     * 设置项列表的内容
     */
    public void setItemContents(ArrayList<String> list){
        /*当前选中项为原本列表项的最后一项时，如果重新指定的列表项的比原本的列表项的小
          则会让当前的选中项为空，所以需要重新指定选中项*/
        if (getSelectedIndex() >= list.size()){
            setSelectedIndex(list.size());
        }

        contents = list;
        invalidate();
    }

    /**
     * 设置显示的项数
     */
    public void setShowItemNum(int num){
        showItemNum = num;
        offsetY = (showItemNum - 1) / 2;    //设置默认项
    }

    /**
     * 设置分割线的颜色
     */
    public void setDividerColor(int dividerColor) {
        this.dividerColor = dividerColor;
    }

    /**
     * 设置选中状态字体的颜色
     */
    public void setTextSelectorColor(int textSelectorColor) {
        this.textSelectorColor = textSelectorColor;
    }

    /**
     * 设置正常状态字体的颜色
     */
    public void setTextNormalColor(int textNormalColor) {
        this.textNormalColor = textNormalColor;
    }

    private final int DIVIDER_WIDTH = 2;        //分割线的宽度
    private final int DEFAULT_TEXTSIZE = 40;    //默认字体大小
    private final int SLEEP_TIME = 1000 / 60;   //动画的延时时间，每秒大约80帧
    private final int WHAT_INVALIDATE = 0;      //重新绘制

    private int showItemNum = 3;                    //显示的项数
    private int dividerY;                           //绘制分隔线的y坐标
    private int itemHeight;                         //每一项所占的高度
    private int dividerColor = 0xFFF5F5F5;          //分割线的颜色
    private int textSelectorColor = 0xFF333333;     //选中状态文字的颜色
    private int textNormalColor = 0xFFcccccc;       //正常状态文字的颜色
    private float offsetY;                          //项偏移的y坐标
    private boolean isPress;                        //手指是否是按下状态
    private boolean isFirst = true;                 //是否是首次绘制
    private boolean isHoming;                       //是否正在执行归位
    private ArrayList<String> contents;             //项的内容
    private Paint mPaint;                           //画笔
    private GestureDetector mDetector;              //手势
    private Handler mHandler;                       //异步处理
    private RollThread rollThread;                  //滚动线程

    @SuppressLint("HandlerLeak")
    public ScrollSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);                          //实例化画笔
        mPaint.setTextSize(DEFAULT_TEXTSIZE);                               //设置字体大小
        mPaint.setStrokeWidth(DIVIDER_WIDTH);                               //设置线条的宽度
        mDetector = new GestureDetector(context, new MyGestureListener());  //实例化手势
        contents = new ArrayList<>();                                       //初始化列表项的内容，防止出现空指针错误
        mHandler = new Handler(){                                           //实例化Handler
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    //重新绘制
                    case WHAT_INVALIDATE:
                        invalidate();
                        break;
                }
            }
        };
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //计算每一项的高度
        itemHeight = h / showItemNum;

        //计算分割线的y坐标
        dividerY = itemHeight * ((showItemNum - 1) / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //绘制分割线
        mPaint.setColor(dividerColor);
        canvas.drawLine(0, dividerY, getWidth(), dividerY, mPaint);
        canvas.drawLine(0, dividerY + itemHeight, getWidth(), dividerY + itemHeight, mPaint);

        //边界限制
        borderLimit();

        //绘制项
        for (int i = 0; i < showItemNum + 1; i++){
            //获取要绘制的项的序号
            int index = (int) -offsetY + i - (showItemNum - 1) / 2;
            if (index >= contents.size()) break;
            if (index < 0) continue;

            //获取字符串的宽高
            String item = contents.get(index);
            Rect bound = new Rect();
            mPaint.getTextBounds(item, 0, item.length(), bound);

            //绘制字符串
            int x = bound.width() > getWidth() ? 0 :(getWidth() - bound.width()) / 2;   //绘制文本的x坐标
            int y = (int) (itemHeight * i + (offsetY - (int) offsetY) * itemHeight);    //绘制文本的y坐标
            y += (itemHeight + bound.height()) / 2;                                     //绘制文本的基线偏移量

            if (getSelectedIndex() == index) {
                mPaint.setColor(textSelectorColor); //选中状态的字体颜色
            }else {
                mPaint.setColor(textNormalColor);   //正常状态的字体颜色
            }

            canvas.drawText(item, x, y, mPaint);

            if (isFirst) isFirst = false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //手指按下
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            isPress = true;
        }
        //手指抬起
        if (event.getAction() == MotionEvent.ACTION_UP){
            isPress = false;
            homing();
        }
        //手势判断
        mDetector.onTouchEvent(event);

        return true;
    }

    /**
     * 归位
     */
    private void homing(){
        if (isHoming) return;
        isHoming = true;
        new HomingThread().start();
    }

    /**
     * 滚动
     */
    private void roll(float speed){
        if (rollThread != null && rollThread.isAlive()) return;

        rollThread = new RollThread(speed);
        rollThread.start();
    }

    /**
     * 跳转
     * @param dir true为跳转到顶部，false为跳转到底部
     */
    /*private void skip(boolean dir){
        if (isSkiping) return;
        isSkiping = true;
        Log.e("tag", "skip");
        new SkipThread(dir).start();
    }*/

    /**
     * 边界限制
     * @return -1为在顶部，1为在底部，0为不在边界
     */
    private int borderLimit(){
        if (offsetY >= 0) {                          //顶部边界
            offsetY = 0;
            return -1;
        }
        else if (offsetY <= -contents.size() + 1){   //底部边界
            offsetY = -contents.size() + 1;
            return 1;
        }
        return 0;
    }

    /**
     * 手势事件
     */
    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener{

        /**
         * 滑动
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            offsetY -= distanceY / itemHeight;      //偏移量
            invalidate();

            return false;
        }

        /**
         * 滚动
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float speed = velocityY / itemHeight / 20;
            if (Math.abs(speed) > 0.5) {
                roll(speed);
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    /**
     * 自动返回中间位置的（归位）线程
     */
    private class HomingThread extends Thread {

        private final float MOVE_DISTANCE = 0.05f;   //每一帧的移动距离(itemHeight的比例)

        @Override
        public void run() {
            super.run();

            float dy = 0;
            while(!isPress){    //手指按下就停止归位
                //取小数部分
                float decimal = Math.abs(offsetY - (int) offsetY);
                //大概达到中间位置
                if (decimal > -MOVE_DISTANCE * 1.1 && decimal < MOVE_DISTANCE * 1.1) break;
                //移动量
                dy = decimal < 0.5 ? MOVE_DISTANCE : -MOVE_DISTANCE;
                //防止超过位置
                if ((int) offsetY != (int) (offsetY + dy)) break;

                offsetY += dy;

                try{
                    Thread.sleep(SLEEP_TIME);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                //重新绘制
                mHandler.sendEmptyMessage(WHAT_INVALIDATE);
            }
            //取整
            if (!isPress) {
                offsetY = (int) offsetY;
                if (dy < 0) {   //误差校正
                    offsetY--;
                }
                mHandler.sendEmptyMessage(WHAT_INVALIDATE);
            }
            isHoming = false;
            if(scrollClickListener!=null){
                scrollClickListener.onScrollClick();
            }
        }
    }

    /**
     * 滚动的线程
     */
    private class RollThread extends Thread {

        private final float DAMPING = 0.1f;    //速度的衰减，即每一帧之后的衰减量

        private float speed;        //滚动的速度，即每一帧移动的距离

        public RollThread(float speed){
            this.speed = speed;
        }

        @Override
        public void run() {
            super.run();

            boolean dir = speed > 0;   //滚动方向，true为向上，false为向下
            while (!isPress){
                offsetY += speed;
                //显示越界
                if (borderLimit() != 0) {
                    mHandler.sendEmptyMessage(WHAT_INVALIDATE);
                    break;
                }
                //速度衰减
                speed += (dir ? -DAMPING : DAMPING);
                //速度越界
                if ((dir && speed < 0) || (!dir && speed > 0)) break;

                try {
                    Thread.sleep(SLEEP_TIME);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                //重新绘制
                mHandler.sendEmptyMessage(WHAT_INVALIDATE);
            }
            //滚动完后归位
            if (!isPress) {
                homing();
            }
        }
    }

    public interface ScrollClickListener {
        void onScrollClick();
    }

    public void setScrollListener(ScrollClickListener scrollClickListener) {
        this.scrollClickListener = scrollClickListener;
    }
}
