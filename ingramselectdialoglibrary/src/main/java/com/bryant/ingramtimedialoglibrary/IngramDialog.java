package com.bryant.ingramtimedialoglibrary;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Calendar;

public class IngramDialog extends Dialog {

    private Context context;
    private TextView cancel,fix;
    private ArrayList<String> list,list02,list03;
    private String time;
    private TimeClickListener timeClickListener;
    private TextView text;
    private ScrollSelector selectorView,selectorView2,selectorView3;
    private String tipsStr = "";
    private String cancelText = "取消",fixText = "确定";
    private int currentYear = 0,currentMonth = 0;
    
    public IngramDialog(Context context) {
        super(context, R.style.ShowImageDialog);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_select_layout, null);
        setContentView(view);
        this.context = context;

        cancel = findViewById(R.id.cancel);
        fix = findViewById(R.id.fix);
        text = findViewById(R.id.text);
    }

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void init(Context context){
        Calendar calendar = Calendar.getInstance();

        list = getDefaultdateStartDate();
        list02 = getDefaultdateStartDate2();
        list03 = getDefaultdateStartDate3();

        selectorView = findViewById(R.id.selectorView);
        selectorView.setItemContents(list);
        selectorView.setSelectedIndex(calendar.get(Calendar.YEAR) - 1999 + 1);
        selectorView.setScrollListener(new ScrollSelector.ScrollClickListener() {
            @Override
            public void onScrollClick() {
                currentYear = Integer.parseInt(list.get(selectorView.getSelectedIndex()).replace("年",""));
            }
        });
        selectorView2 = findViewById(R.id.selectorView2);
        selectorView2.setItemContents(list02);
        selectorView2.setSelectedIndex(calendar.get(Calendar.MONTH)+1);
        selectorView2.setScrollListener(new ScrollSelector.ScrollClickListener() {
            @Override
            public void onScrollClick() {
                currentMonth = Integer.parseInt(list02.get(selectorView2.getSelectedIndex()).replace("月",""));
                list03.clear();
                list03 = getDefaultdateStartDate3();
                selectorView3.setItemContents(list03);
            }
        });

        selectorView3 = findViewById(R.id.selectorView3);
        selectorView3.setItemContents(list03);
        selectorView3.setSelectedIndex(calendar.get(Calendar.DATE));

        cancel.setText(cancelText);
        fix.setText(fixText);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        fix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               time = list.get(selectorView.getSelectedIndex())+list02.get(selectorView2.getSelectedIndex())+list03.get(selectorView3.getSelectedIndex());
                if (timeClickListener != null) {
                    timeClickListener.onTimeClick(time);
                }
                dismiss();
            }
        });

        text.setText(tipsStr);
    }

    ArrayList<String> getDefaultdateStartDate() {
        ArrayList<String> list = new ArrayList<>();
        for (int i=1999;i<=2100;i++){
            list.add(i+"年");
        }
        return list;
    }

    @SuppressLint("DefaultLocale")
    ArrayList<String> getDefaultdateStartDate2() {
        ArrayList<String> list = new ArrayList<>();
        for (int i=1;i<=12;i++){
            list.add(String.format("%02d",i)+"月");
        }
        return list;
    }

    @SuppressLint("DefaultLocale")
    ArrayList<String> getDefaultdateStartDate3() {
        int maxDate = 31;
        Calendar a = Calendar.getInstance();
        if(currentMonth!=0) {
            if(currentYear!=0){
                a.set(Calendar.YEAR, currentYear);
            }
            a.set(Calendar.MONTH, currentMonth-1);
            a.set(Calendar.DATE, 1);
            a.roll(Calendar.DATE, -1);
            maxDate = a.get(Calendar.DATE);
        }else{
            a.set(Calendar.DATE, 1);
            a.roll(Calendar.DATE, -1);
            maxDate = a.get(Calendar.DATE);
        }
        ArrayList<String> list = new ArrayList<>();
        for (int i=1;i<=maxDate;i++){
            list.add(String.format("%02d",i)+"日");
        }
        return list;
    }

    //设置提示文字
    public IngramDialog setTipsStr(String tips){
        this.tipsStr = tips;
        return this;
    }

    //设置提示文字的颜色
    public IngramDialog setTipsColor(int resId){
        text.setTextColor(context.getResources().getColor(resId));
        return this;
    }

    //设置提示文字的大小
    public IngramDialog setTipsSize(int size){
        text.setTextSize(size);
        return this;
    }

    //设置取消按钮的文字
    public IngramDialog setCancelText(String text){
        this.cancelText = text;
        return this;
    }

    //设置确定按钮的文字
    public IngramDialog setFixText(String text){
        this.fixText = text;
        return this;
    }

    //设置取消按钮的背景
    public IngramDialog setCancelBg(int drawableId){
        cancel.setBackground(context.getDrawable(drawableId));
        return this;
    }

    //设置确定按钮的背景
    public IngramDialog setFixBg(int drawableId){
        fix.setBackground(context.getDrawable(drawableId));
        return this;
    }

    //参数设置完毕，一定要build一下
    public void build(){
        init(context);
    }

    public interface TimeClickListener {
        void onTimeClick(String time);
    }

    public void setTimeListener(TimeClickListener timeClickListener) {
        this.timeClickListener = timeClickListener;
    }
}
