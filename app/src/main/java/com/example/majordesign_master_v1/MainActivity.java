package com.example.majordesign_master_v1;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.majordesign_master_v1.data.DrawingEntity;
import com.example.majordesign_master_v1.data.DrawingRepository;
import com.example.majordesign_master_v1.data.DrawingState;

import java.io.OutputStream;
import android.net.Uri;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//主函数
public class MainActivity extends Activity {
    private static final long AUTOSAVE_INTERVAL_MS = 30_000L;
    private static final String EXTRA_DRAWING_ID = "drawing_id";

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
    private TextView drawingTitle;
    private DrawingEntity currentEntity;
    private int color = Color.argb(255, 0, 0, 0);//全局当前颜色
    private boolean eraser_flag = false;//橡皮模式标志

    private DrawingRepository repository;
    private long drawingId = -1L;
    private boolean stateLoaded = false;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private Handler autosaveHandler;
    private final Runnable autosaveRunnable = new Runnable() {
        @Override
        public void run() {
            saveDrawing(false);
            scheduleAutosave();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);//关联布局
        repository = new DrawingRepository(this);
        autosaveHandler = new Handler(Looper.getMainLooper());
        drawingId = getIntent().getLongExtra(EXTRA_DRAWING_ID, -1L);
        inite();
        prepareDrawingSession();
    }

    private void prepareDrawingSession() {
        if (drawingId > 0) {
            awaitCanvasThenLoad();
        } else {
            ioExecutor.execute(() -> {
                drawingId = repository.createDrawing(defaultName());
                runOnUiThread(this::awaitCanvasThenLoad);
            });
        }
    }

    private void awaitCanvasThenLoad() {
        if (writtingView.getWidth() == 0 || writtingView.getHeight() == 0) {
            writtingView.post(this::loadDrawingState);
        } else {
            loadDrawingState();
        }
    }

    private void loadDrawingState() {
        ioExecutor.execute(() -> {
            DrawingEntity entity = repository.getById(drawingId);
            if (entity == null) {
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.main_missing_drawing, Toast.LENGTH_SHORT).show();
                    stateLoaded = true;
                });
                return;
            }
            DrawingState state = repository.toState(entity,
                    Math.max(writtingView.getWidth(), 1),
                    Math.max(writtingView.getHeight(), 1));
            currentEntity = entity;
            runOnUiThread(() -> {
                drawingTitle.setText(entity.name);
                writtingView.applyState(state);
                stateLoaded = true;
                scheduleAutosave();
            });
        });
    }

    private void scheduleAutosave() {
        if (!stateLoaded || drawingId <= 0) {
            return;
        }
        autosaveHandler.removeCallbacks(autosaveRunnable);
        autosaveHandler.postDelayed(autosaveRunnable, AUTOSAVE_INTERVAL_MS);
    }

    private void stopAutosave() {
        autosaveHandler.removeCallbacks(autosaveRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        scheduleAutosave();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutosave();
        saveDrawing(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutosave();
        ioExecutor.shutdownNow();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void saveDrawing() {
        saveDrawing(false);
    }

    private void saveDrawing(boolean showToast) {
        if (!stateLoaded || drawingId <= 0) {
            return;
        }
        DrawingState snapshot = writtingView.snapshotState();
        repository.persistState(drawingId, snapshot);
        if (showToast) {
            Toast.makeText(this, R.string.main_save_success, Toast.LENGTH_SHORT).show();
        }
    }

    private void exportCurrentDrawing() {
        if (currentEntity == null) {
            Toast.makeText(this, R.string.main_save_error, Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap snapshot = writtingView.snapshotState().getCurrentBitmap();
        if (snapshot == null) {
            Toast.makeText(this, R.string.main_save_error, Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap flattened = repository.flattenWithBackground(snapshot, Color.WHITE);
        ioExecutor.execute(() -> {
            String name = currentEntity.name == null || currentEntity.name.isEmpty()
                    ? defaultName()
                    : currentEntity.name;
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, name + ".jpg");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
            ContentResolver resolver = getContentResolver();
            Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                runOnUiThread(() -> Toast.makeText(this, R.string.main_save_error, Toast.LENGTH_SHORT).show());
                return;
            }
            try (OutputStream os = resolver.openOutputStream(uri)) {
                if (os != null) {
                    flattened.compress(Bitmap.CompressFormat.JPEG, 95, os);
                }
            } catch (Exception e) {
                resolver.delete(uri, null, null);
                runOnUiThread(() -> Toast.makeText(this, R.string.main_save_error, Toast.LENGTH_SHORT).show());
                return;
            }
            ContentValues finalizeValues = new ContentValues();
            finalizeValues.put(MediaStore.Images.Media.IS_PENDING, 0);
            resolver.update(uri, finalizeValues, null, null);
            runOnUiThread(() -> Toast.makeText(this, R.string.main_save_success, Toast.LENGTH_SHORT).show());
        });
    }

    private void openHistory() {
        startActivity(new Intent(this, HistoryActivity.class));
    }

    private String defaultName() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        return getString(R.string.default_canvas_prefix) + "_" + format.format(new Date());
    }

    /**
     * 初始化
     */
    private void inite(){
        writtingView = (WrittingView)findViewById(R.id.writting);
        drawingTitle = findViewById(R.id.drawing_title);
        drawingTitle.setOnClickListener(v -> {
            if (currentEntity != null) {
                promptForRename(currentEntity.id, currentEntity.name);
            }
        });
        writtingView.setOnCanvasChangeListener(this::saveDrawing);

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
                    builder.setPositiveButton("确定", (dialogInterface, i) -> {
                        // 按下确定后执行清屏
                        writtingView.clearall();
                    });
                    builder.show();//弹出提示框
                }
            }


        });

        targetView = findViewById(R.id.clear); // 长按按钮，和保存按钮是同一个，但是功能不同就分开写

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
                exportCurrentDrawing();
            }
        });
        save.setOnLongClickListener(v -> {
            openHistory();
            return true;
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

    private void promptForRename(long id, String currentName) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_text, null);
        EditText editText = dialogView.findViewById(R.id.dialog_edit_text);
        String titleText = currentName == null ? "" : currentName;
        editText.setText(titleText);
        editText.setSelection(editText.getText().length());

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_enter_name)
                .setView(dialogView)
                .setNegativeButton(R.string.dialog_cancel, null)
                .setPositiveButton(R.string.dialog_confirm, (dialog, which) -> {
                    String name = editText.getText().toString().trim();
                    if (TextUtils.isEmpty(name)) {
                        name = titleText;
                    }
                    if (!name.equals(currentName)) {
                        if (currentEntity != null) {
                            currentEntity.name = name;
                        }
                        drawingTitle.setText(name);
                        repository.rename(id, name);
                    }
                })
                .show();
    }
}
