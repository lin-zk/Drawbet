package com.example.majordesign;



import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import androidx.appcompat.app.AlertDialog;

//主函数
public class MainActivity extends Activity {
    private WrittingView writtingView = null;//画板视图
    private RadioGroup pengroup = null;//画笔组
    private SeekBar pensizebar = null;//滚动条，用于设置笔刷大小
    private SeekBar redbar = null;//滚动条，用于设置红色
    private SeekBar greenbar = null;//滚动条，用于设置绿色
    private SeekBar bluebar = null;//滚动条，用于设置蓝色
    private Button clearButton = null;//清除按钮
    private Button targetView = null;//长按关于页面按钮
    private Button undo = null;//撤销按钮
    private Button redo = null;//恢复按钮
    private Button save = null;//保存按钮
    private View PenSize = null;//笔大小预览
    private View colorView = null;//颜色预览
    private int color = Color.argb(255, 0, 0, 0);//全局当前颜色
    private boolean eraser_flag = false;//橡皮模式标志
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);//关联布局
        inite();
    }

    /**
     * 初始化
     */
    private void inite(){
        writtingView = (WrittingView)findViewById(R.id.writting);

        RadioButton pen = (RadioButton)findViewById(R.id.pen);
        pen.setChecked(true);//开始时选择画笔

        pengroup = (RadioGroup)findViewById(R.id.toolbar);
        //监听笔样式选择
        pengroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.pen) {
                    // 选择了笔功能
                    writtingView.setcolor(color);//切换笔时重新获取一次颜色
                    writtingView.draw_type = 0;//传入笔标识，划线
                    eraser_flag = false;//非橡皮
                } else if (checkedId == R.id.eraser) {
                    // 选择了橡皮擦功能
                    eraser_flag = true;//橡皮模式，禁用颜色更改
                    writtingView.draw_type = 0;//传入笔标识，划线
                    writtingView.setcolor(Color.WHITE);//固定白色
                } else if (checkedId == R.id.round) {
                    // 选择了画圆功能
                    writtingView.setcolor(color);//切换笔时重新获取一次颜色
                    writtingView.draw_type = 1;//传入笔标识，圆形
                    eraser_flag = false;//非橡皮
                } else if (checkedId == R.id.rectangle) {
                    // 选择了画矩形功能
                    writtingView.setcolor(color);//切换笔时重新获取一次颜色
                    writtingView.draw_type = 2;//传入笔标识，矩形
                    eraser_flag = false;//非橡皮
                }
            };
        });

        //选择笔的大小
        pensizebar = (SeekBar)findViewById(R.id.size);
        //设置滑动条监听
        pensizebar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                writtingView.setPenSize(progress);//当滚动条滑动时，调用

                //设置预览笔大小的View大小，达到预览效果
                PenSize = (View)findViewById(R.id.pensize);

                ViewGroup.LayoutParams layoutParams = PenSize.getLayoutParams();
                layoutParams.width = progress;
                layoutParams.height = progress;
                PenSize.setLayoutParams(layoutParams);//对应变化view的大小

            }

        });

        redbar = (SeekBar) findViewById(R.id.redbar);
        greenbar = (SeekBar) findViewById(R.id.greenbar);
        bluebar = (SeekBar) findViewById(R.id.bluebar);
        colorView = findViewById(R.id.colorview);

        redbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int red = progress; // 计算红色通道的值
                int green = greenbar.getProgress(); // 获取绿色通道的值
                int blue = bluebar.getProgress(); // 获取蓝色通道的值
                color = Color.argb(255, red, green, blue); // 合并颜色通道
                if(!eraser_flag) {
                    //橡皮模式则不设置，即禁用颜色更改
                    writtingView.setcolor(color); // 设置颜色
                }
                //设置颜色预览View的Tint背景颜色，达到实时预览调整颜色的效果
                colorView.setBackgroundTintList(ColorStateList.valueOf(color));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });



        greenbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int red = redbar.getProgress(); // 获取红色通道的值
                int green = progress; // 计算绿色通道的值
                int blue = bluebar.getProgress(); // 获取蓝色通道的值
                color = Color.argb(255, red, green, blue); // 合并颜色通道
                if(!eraser_flag) {
                    //橡皮模式则不设置，即禁用颜色更改
                    writtingView.setcolor(color); // 设置颜色
                }
                //设置颜色预览View的Tint背景颜色，达到实时预览调整颜色的效果
                colorView.setBackgroundTintList(ColorStateList.valueOf(color));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        bluebar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int red = redbar.getProgress(); // 获取红色通道的值
                int green = greenbar.getProgress(); // 获取绿色通道的值
                int blue = progress; // 计算蓝色通道的值
                color = Color.argb(255, red, green, blue); // 合并颜色通道
                if(!eraser_flag) {
                    //橡皮模式则不设置，即禁用颜色更改
                    writtingView.setcolor(color); // 设置颜色
                }
                //设置颜色预览View的Tint背景颜色，达到实时预览调整颜色的效果
                colorView.setBackgroundTintList(ColorStateList.valueOf(color));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //清除按钮
        clearButton = (Button)findViewById(R.id.clear);
        //设置按钮监听
        clearButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // 按下清空按钮后跳出提示框
                // 撤销栈和恢复栈都为空时不能触发清空
                if(!writtingView.undoStack.isEmpty() || !writtingView.redoStack.isEmpty()) {
                    // 撤销栈和恢复栈都为空，即现在是清空状态不再触发清空
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("提示");
                    builder.setMessage("确定要清空画布?");
                    builder.setNegativeButton("取消", null);
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // 按下确定后执行清屏
                            writtingView.clearall();
                        }
                    });
                    builder.show();//弹出提示框
                }
            }


        });

        targetView = findViewById(R.id.save); // 长按按钮，和保存按钮是同一个，但是功能不同就分开写

        targetView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 在这里处理长按事件
                // 长按后跳转到关于页面
                Intent intent = new Intent(MainActivity.this, AboutPage.class);
                startActivity(intent);
                return true; // 返回true表示已消耗该事件，不会触发单击事件
            }
        });

        save = findViewById(R.id.save); // 保存

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // 调用WrittingView实例的captureCanvas()方法，截取位图部分以JPG保存
                writtingView.captureCanvas();
            }
        });

        undo = findViewById(R.id.undo); // 撤销

        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // 调用WrittingView实例的undo()方法，返回上一步
                writtingView.undo();
            }
        });

        redo = findViewById(R.id.redo); // 恢复

        redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // 调用WrittingView实例的redo()方法，实现恢复
                writtingView.redo();
            }
        });

    }
}
