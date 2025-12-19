package com.example.majordesign_master_v1;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.majordesign_master_v1.data.DrawingEntity;
import com.example.majordesign_master_v1.data.DrawingRepository;
import com.example.majordesign_master_v1.history.HistoryAdapter;
import com.example.majordesign_master_v1.history.HistoryViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryActivity extends AppCompatActivity implements HistoryAdapter.Listener {
    private HistoryViewModel viewModel;
    private HistoryAdapter adapter;
    private EditText searchField;
    private TextView emptyView;
    private Button selectAllButton;
    private Button invertButton;
    private Button deleteButton;
    private Button exportButton;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private int selectedCount = 0;
    private int totalCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        initViews();
        setupRecycler();
        setupViewModel();
        setupSearch();
        setupButtons();
    }

    private void initViews() {
        searchField = findViewById(R.id.history_search);
        emptyView = findViewById(R.id.history_empty);
        selectAllButton = findViewById(R.id.history_select_all);
        invertButton = findViewById(R.id.history_invert);
        deleteButton = findViewById(R.id.history_delete);
        exportButton = findViewById(R.id.history_export);
    }

    private void setupRecycler() {
        RecyclerView list = findViewById(R.id.history_list);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(this);
        list.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        viewModel.getDrawings().observe(this, this::renderHistory);
    }

    private void setupSearch() {
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString();
                viewModel.setQuery(query);
                adapter.setSearchQuery(query);
            }
        });
    }

    private void setupButtons() {
        ImageButton addButton = findViewById(R.id.history_add);
        addButton.setOnClickListener(v -> promptForName(R.string.history_add, defaultName(), this::createDrawingAsync));

        selectAllButton.setOnClickListener(v -> {
            if (totalCount > 0 && selectedCount == totalCount) {
                adapter.clearSelection();
            } else {
                adapter.selectAll();
            }
        });

        invertButton.setOnClickListener(v -> adapter.invertSelection());

        deleteButton.setOnClickListener(v -> {
            List<Long> ids = new ArrayList<>(adapter.getSelectedIds());
            if (ids.isEmpty()) {
                showToast(R.string.history_selection_empty);
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle(R.string.history_delete_title)
                    .setMessage(R.string.history_delete_confirm)
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .setPositiveButton(R.string.dialog_confirm, (dialog, which) -> performDelete(ids))
                    .show();
        });

        exportButton.setOnClickListener(v -> {
            List<Long> ids = new ArrayList<>(adapter.getSelectedIds());
            if (ids.isEmpty()) {
                showToast(R.string.history_export_empty);
                return;
            }
            final int requested = ids.size();
            viewModel.export(ids, new DrawingRepository.ExportCallback() {
                @Override
                public void onExported(List<String> outputPaths) {
                    if (outputPaths.size() == requested) {
                        showToast(R.string.history_export_success);
                    } else if (!outputPaths.isEmpty()) {
                        showToast(R.string.history_export_partial);
                    } else {
                        showToast(R.string.history_export_error);
                    }
                }

                @Override
                public void onError(Exception exception) {
                    showToast(R.string.history_export_error);
                }
            });
        });

        updateSelectionUi();
    }

    private void renderHistory(List<DrawingEntity> entities) {
        adapter.submitList(entities);
        boolean empty = entities == null || entities.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        totalCount = entities == null ? 0 : entities.size();
        if (empty) {
            adapter.clearSelection();
        }
        updateSelectionUi();
    }

    private void createDrawingAsync(String name) {
        ioExecutor.execute(() -> {
            long id = viewModel.create(name);
            mainHandler.post(() -> openDrawing(id));
        });
    }

    private void performDelete(List<Long> ids) {
        viewModel.deleteSelected(ids);
        adapter.clearSelection();
        showToast(R.string.history_delete_done);
    }

    private void openDrawing(long id) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("drawing_id", id);
        startActivity(intent);
    }

    private void promptForName(@StringRes int titleRes, String defaultText, NameCallback callback) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_text, null);
        EditText editText = dialogView.findViewById(R.id.dialog_edit_text);
        editText.setText(defaultText);
        editText.setSelection(editText.getText().length());

        new AlertDialog.Builder(this)
                .setTitle(titleRes)
                .setView(dialogView)
                .setNegativeButton(R.string.dialog_cancel, null)
                .setPositiveButton(R.string.dialog_confirm, (dialog, which) -> {
                    String name = editText.getText().toString().trim();
                    if (name.isEmpty()) {
                        name = defaultText;
                    }
                    callback.onNameReady(name);
                })
                .show();
    }

    private void promptForRename(long id, String currentName) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_text, null);
        EditText editText = dialogView.findViewById(R.id.dialog_edit_text);
        editText.setText(currentName);
        editText.setSelection(editText.getText().length());

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_enter_name)
                .setView(dialogView)
                .setNegativeButton(R.string.dialog_cancel, null)
                .setPositiveButton(R.string.dialog_confirm, (dialog, which) -> {
                    String name = editText.getText().toString().trim();
                    if (TextUtils.isEmpty(name)) {
                        name = currentName;
                    }
                    if (!currentName.equals(name)) {
                        viewModel.rename(id, name);
                    }
                })
                .show();
    }

    private String defaultName() {
        String prefix = getString(R.string.default_canvas_prefix);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        return prefix + "_" + format.format(new Date());
    }

    private void showToast(@StringRes int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onOpen(long id) {
        openDrawing(id);
    }

    @Override
    public void onSelectionChanged(int selectedCount, int totalCount) {
        this.selectedCount = selectedCount;
        this.totalCount = totalCount;
        updateSelectionUi();
    }

    @Override
    public void onRenameRequested(long id, String currentName) {
        promptForRename(id, currentName == null ? "" : currentName);
    }

    private void updateSelectionUi() {
        selectAllButton.setText(selectedCount == totalCount && totalCount > 0
                ? R.string.history_select_none
                : R.string.history_select_all);
        boolean hasSelection = selectedCount > 0;
        deleteButton.setEnabled(hasSelection);
        exportButton.setEnabled(hasSelection);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    interface NameCallback {
        void onNameReady(String name);
    }
}
