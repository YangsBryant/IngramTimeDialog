# IngramTimeDialog
日期选择器

## 引入module
```java
allprojects {
    repositories {
        google()
        jcenter()
        maven { url 'https://www.jitpack.io' }
    }
}
```

```java
implementation 'com.github.YangsBryant:IngramTimeDialog:1.0.1'
```

## 主要代码
```java
public class MainActivity extends AppCompatActivity {

    private IngramDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dialog = new IngramDialog(this);
        dialog.setTipsStr("请选择开始时间")
                .setCancelText("取消")
                .setFixText("确定")
                .build();
        dialog.setTimeListener(new IngramDialog.TimeClickListener() {
            @Override
            public void onTimeClick(String time) {
                Toast.makeText(MainActivity.this,time,Toast.LENGTH_SHORT).show();
            }
        });

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();

            }
        });
    }
}
```
## ScaleProgressView属性大全
方法名 | 属性
--------- | -------------
setTipsStr(String tips) | 设置标题文字
setTipsColor(int resId) | 设置标题文字的颜色
setTipsSize(int size) | 设置标题文字的大小
setCancelText(String text) | 设置取消按钮的文字
setFixText(String text) | 设置确定按钮的文字
setCancelBg(int drawableId) | 设置取消按钮的背景
setFixBg(int drawableId) | 设置确定按钮的背景
build() | 参数设置完毕，一定要build一下

## 联系QQ：961606042
