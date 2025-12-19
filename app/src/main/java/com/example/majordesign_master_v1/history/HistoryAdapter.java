package com.example.majordesign_master_v1.history;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.majordesign_master_v1.R;
import com.example.majordesign_master_v1.data.DrawingEntity;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.Holder> {
    public interface Listener {
        void onOpen(long id);
        void onSelectionChanged(int selectedCount, int totalCount);
        void onRenameRequested(long id, String currentName);
    }

    private final Listener listener;
    private final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
    private final Set<Long> selectedIds = new HashSet<>();
    private final List<DrawingEntity> items = new ArrayList<>();
    private String searchQuery = "";

    public HistoryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<DrawingEntity> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        notifyDataSetChanged();
        notifySelection();
    }

    public void toggleSelection(long id) {
        if (selectedIds.contains(id)) {
            selectedIds.remove(id);
        } else {
            selectedIds.add(id);
        }
        notifySelection();
        notifyDataSetChanged();
    }

    public void selectAll() {
        selectedIds.clear();
        for (DrawingEntity entity : items) {
            selectedIds.add(entity.id);
        }
        notifySelection();
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedIds.clear();
        notifySelection();
        notifyDataSetChanged();
    }

    public void invertSelection() {
        Set<Long> newSelection = new HashSet<>();
        for (DrawingEntity entity : items) {
            if (!selectedIds.contains(entity.id)) {
                newSelection.add(entity.id);
            }
        }
        selectedIds.clear();
        selectedIds.addAll(newSelection);
        notifySelection();
        notifyDataSetChanged();
    }

    public Set<Long> getSelectedIds() {
        return new HashSet<>(selectedIds);
    }

    public void setSearchQuery(String query) {
        String normalized = query == null ? "" : query.trim();
        if (!normalized.equals(searchQuery)) {
            searchQuery = normalized;
            notifyDataSetChanged();
        }
    }

    private void notifySelection() {
        if (listener != null) {
            listener.onSelectionChanged(selectedIds.size(), items.size());
        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_entry, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        DrawingEntity entity = items.get(position);
        holder.bind(entity);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        private final ImageView thumbnail;
        private final TextView title;
        private final TextView timestamp;
        private final CheckBox checkbox;
        private final int defaultTitleColor;

        Holder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.item_thumbnail);
            title = itemView.findViewById(R.id.item_title);
            timestamp = itemView.findViewById(R.id.item_timestamp);
            checkbox = itemView.findViewById(R.id.item_checkbox);
            defaultTitleColor = title.getCurrentTextColor();
        }

        void bind(DrawingEntity entity) {
            title.setText(getHighlightedTitle(entity.name));
            title.setOnClickListener(v -> listener.onRenameRequested(entity.id, entity.name));
            timestamp.setText(dateFormat.format(new Date(Math.max(entity.updatedAt, entity.createdAt))));
            checkbox.setOnCheckedChangeListener(null);
            checkbox.setChecked(selectedIds.contains(entity.id));
            checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> toggleSelection(entity.id));
            itemView.setOnClickListener(v -> listener.onOpen(entity.id));
            itemView.setOnLongClickListener(v -> {
                toggleSelection(entity.id);
                return true;
            });
            if (entity.thumbnail != null) {
                Bitmap bmp = BitmapFactory.decodeByteArray(entity.thumbnail, 0, entity.thumbnail.length);
                thumbnail.setImageBitmap(bmp);
            } else if (entity.bitmapBytes != null) {
                Bitmap bmp = BitmapFactory.decodeByteArray(entity.bitmapBytes, 0, entity.bitmapBytes.length);
                thumbnail.setImageBitmap(bmp);
            } else {
                thumbnail.setImageResource(android.R.color.darker_gray);
            }
        }

        private CharSequence getHighlightedTitle(String rawName) {
            String text = rawName == null ? "" : rawName;
            title.setTextColor(defaultTitleColor);
            if (searchQuery.isEmpty()) {
                return text;
            }
            String lowerText = text.toLowerCase(Locale.getDefault());
            String lowerQuery = searchQuery.toLowerCase(Locale.getDefault());
            if (lowerQuery.isEmpty() || !lowerText.contains(lowerQuery)) {
                return text;
            }
            SpannableString spannable = new SpannableString(text);
            int start = 0;
            int highlightColor = ContextCompat.getColor(itemView.getContext(), R.color.search_highlight);
            while (start < lowerText.length()) {
                start = lowerText.indexOf(lowerQuery, start);
                if (start == -1) {
                    break;
                }
                int end = start + lowerQuery.length();
                spannable.setSpan(new ForegroundColorSpan(highlightColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                start = end;
            }
            return spannable;
        }
    }
}
