package com.example.majordesign_master_v1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.majordesign_master_v1.data.DrawingEntity;
import com.example.majordesign_master_v1.data.DrawingRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeActivity extends Activity {
    private DrawingRepository repository;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private long lastBackPressed = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        repository = new DrawingRepository(this);
        setupButtons();
    }

    private void setupButtons() {
        findViewById(R.id.button_new).setOnClickListener(v -> promptForNameAndCreate());
        findViewById(R.id.button_continue).setOnClickListener(v -> continueLast());
        findViewById(R.id.button_history).setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
    }

    private void promptForNameAndCreate() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_text, null);
        EditText editText = dialogView.findViewById(R.id.dialog_edit_text);
        editText.setText(defaultName());

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_enter_name)
                .setView(dialogView)
                .setNegativeButton(R.string.dialog_cancel, null)
                .setPositiveButton(R.string.dialog_confirm, (dialog, which) -> {
                    String name = editText.getText().toString().trim();
                    if (TextUtils.isEmpty(name)) {
                        name = defaultName();
                    }
                    createDrawing(name);
                })
                .show();
    }

    private void createDrawing(String name) {
        ioExecutor.execute(() -> {
            long id = repository.createDrawing(name);
            runOnUiThread(() -> launchCanvas(id));
        });
    }

    private void continueLast() {
        ioExecutor.execute(() -> {
            DrawingEntity latest = repository.latest();
            runOnUiThread(() -> {
                if (latest == null) {
                    Toast.makeText(this, R.string.home_empty_continue, Toast.LENGTH_SHORT).show();
                } else {
                    launchCanvas(latest.id);
                }
            });
        });
    }

    private void launchCanvas(long drawingId) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("drawing_id", drawingId);
        startActivity(intent);
    }

    private String defaultName() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        return getString(R.string.default_canvas_prefix) + "_" + format.format(new Date());
    }

    @Override
    public void onBackPressed() {
        long now = System.currentTimeMillis();
        if (now - lastBackPressed < 1500) {
            super.onBackPressed();
            return;
        }
        lastBackPressed = now;
        Toast.makeText(this, R.string.home_exit_prompt, Toast.LENGTH_SHORT).show();
    }
}
