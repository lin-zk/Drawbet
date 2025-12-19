package com.example.majordesign_master_v1;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.majordesign_master_v1.data.DrawingState;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Stack;

/**
 * 画板视图
 * @author Administrator
 *
 */
public class WrittingView extends View{
    private Canvas mCanvas;
    private Bitmap mBitmap;
    private Paint paint = null;
    public int draw_type = 0; // 笔标识，控制绘画样式，0=曲线，1=圆形，2=矩形

    public Stack<Bitmap> undoStack; //撤销栈，存储位图
    public Stack<Bitmap> redoStack; //恢复栈，存储位图
    private long lastShakeTime = 0; // 上一次摇晃的时间
    private static final int SHAKE_THRESHOLD = 1000; // 摇晃的阈值，单位为毫秒
    private SensorManager sensorManager; //传感器
    private Vibrator vibrator; // 震动

    private boolean windos_flag = false;

    public interface OnCanvasChangeListener {
        void onCanvasChanged();
    }

    private OnCanvasChangeListener canvasChangeListener;

    public void setOnCanvasChangeListener(OnCanvasChangeListener listener) {
        this.canvasChangeListener = listener;
    }

    private void notifyCanvasChanged() {
        if (canvasChangeListener != null) {
            canvasChangeListener.onCanvasChanged();
        }
    }

    public WrittingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inite();
        // TODO Auto-generated constructor stub

        //注册一下加速度传感器和震动马达
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        SensorEventListener accelerometerListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                // 计算动态加速度（减去重力加速度）
                float acceleration = event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2];
                acceleration = (float) Math.sqrt(acceleration) - SensorManager.GRAVITY_EARTH;

                //记录时间
                long currentTime = System.currentTimeMillis();

                if (acceleration > 4.9f) { // 根据需要调整摇晃的灵敏度
                    if (currentTime - lastShakeTime >= SHAKE_THRESHOLD) {
                        // 摇晃时间大于1s才触发
                        if(!undoStack.isEmpty() || !redoStack.isEmpty()) {
                            // 撤销栈和恢复栈都为空，即现在是清空状态不再触发清空
                            if (!windos_flag) {
                                // 防止还没有选择是否清空又触发弹窗，即防止多次摇晃产生多重弹窗
                                windos_flag = true;
                                if (vibrator.hasVibrator()) {
                                    //震动
                                    // 震动持续时间（毫秒）
                                    long duration = 200;

                                    // 执行震动
                                    vibrator.vibrate(duration);
                                }
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle("提示");
                                builder.setMessage("确定要清空画布?");
                                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // 按下确定后取消清屏
                                        windos_flag = false;
                                    }
                                });
                                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // 按下确定后执行清屏
                                        clearall();
                                        windos_flag = false;
                                    }
                                });
                                builder.show();//弹出提示框
                                lastShakeTime = currentTime;// 上次摇晃时间赋成现在
                            }
                        }
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // 加速度计的精度变化时的回调方法，不需要处理
            }
        };
        sensorManager.registerListener(accelerometerListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);//绑定回调函数
    }

    /**
     * 初始化
     */
    private void inite(){
        paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);// 抗锯齿
        paint.setColor(Color.BLACK); //初始笔颜色
        paint.setStrokeWidth(4);// 初始画笔大小
        mCanvas = new Canvas();
        mCanvas.drawColor(Color.TRANSPARENT,Mode.CLEAR); // 清屏幕

        undoStack = new Stack<>(); //初始化两个栈
        redoStack = new Stack<>(); //用双栈结构实现撤销与恢复

    }


    /**
     * 设置颜色
     * @param color 颜色
     */
    public void setcolor(int color){
        if(paint!=null){
            paint.setColor(color);
        }
    }
    /**
     * 设置笔刷大小
     * @param size 笔刷大小值
     */
    public void setPenSize(int size){
        if(paint!=null){
            paint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, size, getResources().getDisplayMetrics()));
            //px转dp，更加准确
        }
    }
    /**
     * 清屏
     */
    public void clearall(){
        if(mCanvas!=null){
            mCanvas.drawColor(Color.TRANSPARENT,Mode.CLEAR); // 清屏幕
            invalidate();
            undoStack.clear();// 清空后不能再撤回
            redoStack.clear();// 清空后不能再恢复
            notifyCanvasChanged();
        }
    }

    public void applyState(DrawingState state) {
        if (state == null) {
            return;
        }
        if (getWidth() == 0 || getHeight() == 0) {
            post(() -> applyState(state));
            return;
        }
        Bitmap base = state.getCurrentBitmap();
        Bitmap target = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(target);
        canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
        if (base != null) {
            Rect src = new Rect(0, 0, base.getWidth(), base.getHeight());
            Rect dst = new Rect(0, 0, target.getWidth(), target.getHeight());
            canvas.drawBitmap(base, src, dst, null);
        }
        mBitmap = target;
        mCanvas.setBitmap(mBitmap);
        undoStack = cloneStack(state.getUndoStack());
        redoStack = cloneStack(state.getRedoStack());
        invalidate();
    }

    public DrawingState snapshotState() {
        return new DrawingState(cloneBitmap(mBitmap), cloneStack(undoStack), cloneStack(redoStack));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // TODO Auto-generated method stub
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas.setBitmap(mBitmap);
        super.onSizeChanged(w, h, oldw, oldh); //旋转屏幕时会调用，考虑在平板上使用场景
    }

    /**
     * 撤销上一步操作
     */
    public void undo() {
        if (!undoStack.isEmpty()) {
            // 弹出最后一次绘图操作的状态
            Bitmap previousBitmap = undoStack.pop();// 弹出前一次操作
            redoStack.push(mBitmap); // 将当前状态保存到redoStack
            mBitmap = previousBitmap; // 恢复到上一次绘图的状态
            mCanvas.setBitmap(mBitmap); // 把位图重新加载到画布
            invalidate(); // 刷新画布
            notifyCanvasChanged();
        }
    }

    /**
     * 恢复撤销的操作
     */
    public void redo() {
        if (!redoStack.isEmpty()) {
            // 弹出最后一次撤销操作的状态
            Bitmap nextBitmap = redoStack.pop();// 弹出最新被撤销的操作
            undoStack.push(mBitmap); // 将当前状态保存到undoStack
            mBitmap = nextBitmap; // 恢复到上一次撤销的状态
            mCanvas.setBitmap(mBitmap);// 把位图重新加载到画布
            invalidate(); // 刷新画布
            notifyCanvasChanged();
        }
    }

    //绘制起点，L为线，R为矩形或圆
    private float LstartX = 0;
    private float RstartX = 0;
    private float LstartY = 0;
    private float RstartY = 0;

    // 用于预览的变量
    private boolean isDrawingPreview = false;
    private float previewStopX = 0;
    private float previewStopY = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // 操作前先把当前位图入栈
                Bitmap currentBitmap = Bitmap.createBitmap(mBitmap);
                undoStack.push(currentBitmap);
                redoStack.clear(); // 开始新的绘制时，清空恢复栈

                if (draw_type == 0) { // 曲线
                    LstartX = x;
                    LstartY = y;
                } else { // 矩形或圆形
                    isDrawingPreview = true;
                    RstartX = x;
                    RstartY = y;
                    previewStopX = x;
                    previewStopY = y;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (draw_type == 0) { // 曲线
                    float lstopX = x;
                    float lstopY = y;
                    mCanvas.drawLine(LstartX, LstartY, lstopX, lstopY, paint);
                    LstartX = lstopX;
                    LstartY = lstopY;
                    invalidate();
                } else { // 矩形或圆形预览
                    previewStopX = x;
                    previewStopY = y;
                    invalidate(); // 触发onDraw来绘制预览
                }
                break;

            case MotionEvent.ACTION_UP:
                if (isDrawingPreview) {
                    isDrawingPreview = false;
                    drawShape(mCanvas, RstartX, RstartY, x, y, draw_type); // 在mBitmap上绘制最终形状
                }
                invalidate();
                notifyCanvasChanged();
                break;

            default:
                break;
        }
        return true;
    }

    public void captureCanvas() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 创建一个与画布相同尺寸的位图
                    Bitmap capturedBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);

                    // 将mBitmap绘制到新的位图上
                    Canvas canvas = new Canvas(capturedBitmap);
                    canvas.drawColor(Color.WHITE);
                    canvas.drawBitmap(mBitmap, 0, 0, null);

                    // 创建一个文件来保存截取的图片
                    File imagePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "captured_image.png");

                    // 将位图保存为图片文件
                    FileOutputStream fos = new FileOutputStream(imagePath);
                    capturedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);//JPG保存
                    fos.close();

                    // 通知系统相册扫描新的图片文件
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri contentUri = Uri.fromFile(imagePath);
                    mediaScanIntent.setData(contentUri);
                    getContext().sendBroadcast(mediaScanIntent);

                    // Toast需要在UI线程中显示
                    if (getContext() instanceof android.app.Activity) {
                        ((android.app.Activity) getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "图片保存至相册", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public Bitmap renderBitmapWithBackground(int backgroundColor) {
        if (mBitmap == null) {
            return null;
        }
        Bitmap output = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawColor(backgroundColor);
        canvas.drawBitmap(mBitmap, 0, 0, null);
        return output;
    }

    private Stack<Bitmap> cloneStack(Stack<Bitmap> source) {
        Stack<Bitmap> clone = new Stack<>();
        if (source != null) {
            for (Bitmap bmp : source) {
                clone.push(cloneBitmap(bmp));
            }
        }
        return clone;
    }

    private Bitmap cloneBitmap(Bitmap source) {
        if (source == null) {
            return null;
        }
        return source.copy(Bitmap.Config.ARGB_8888, true);
    }

    private void drawShape(Canvas canvas, float startX, float startY, float stopX, float stopY, int type) {
        paint.setStyle(Paint.Style.STROKE);

        float left = Math.min(startX, stopX);
        float top = Math.min(startY, stopY);
        float right = Math.max(startX, stopX);
        float bottom = Math.max(startY, stopY);

        if (type == 1) { // 圆形或椭圆 (外接)
            float centerX = (startX + stopX) / 2;
            float centerY = (startY + stopY) / 2;
            float width = Math.abs(startX - stopX);
            float height = Math.abs(startY - stopY);

            // 计算外接椭圆的半径
            // 乘以 sqrt(2) 可以确保对角线上的点(即起点和终点)落在椭圆上
            float rx = (float) (width / 2 * Math.sqrt(2));
            float ry = (float) (height / 2 * Math.sqrt(2));

            // 定义椭圆的边界并绘制
            canvas.drawOval(centerX - rx, centerY - ry, centerX + rx, centerY + ry, paint);

        } else if (type == 2) { // 矩形
            canvas.drawRect(left, top, right, bottom, paint);
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制背景边框
        Drawable roundedRectangleDrawable = getResources().getDrawable(R.drawable.gray_border);
        roundedRectangleDrawable.setBounds(0, 0, getWidth(), getHeight());
        roundedRectangleDrawable.draw(canvas);

        // 绘制 mBitmap (已有的内容)
        int margin = 12;
        Rect dstRect = new Rect(margin, margin, getWidth() - margin, getHeight() - margin);
        canvas.drawBitmap(mBitmap, null, dstRect, null);

        // 如果正在绘制预览，则在顶层绘制预览形状
        if (isDrawingPreview) {
            drawShape(canvas, RstartX, RstartY, previewStopX, previewStopY, draw_type);
        }
    }
}
