package com.example.majordesign_master_v1.history;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.majordesign_master_v1.data.DrawingEntity;
import com.example.majordesign_master_v1.data.DrawingRepository;

import java.util.List;

public class HistoryViewModel extends AndroidViewModel {
    private final DrawingRepository repository;
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final LiveData<List<DrawingEntity>> drawings;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        repository = new DrawingRepository(application);
        drawings = Transformations.switchMap(query, repository::observeSearch);
    }

    public LiveData<List<DrawingEntity>> getDrawings() {
        return drawings;
    }

    public void setQuery(String text) {
        query.setValue(text == null ? "" : text.trim());
    }

    public void deleteSelected(List<Long> ids) {
        repository.delete(ids);
    }

    public void export(List<Long> ids, DrawingRepository.ExportCallback callback) {
        if (callback == null) {
            repository.export(ids, null);
            return;
        }
        repository.export(ids, new DrawingRepository.ExportCallback() {
            @Override
            public void onExported(List<String> outputPaths) {
                mainHandler.post(() -> callback.onExported(outputPaths));
            }

            @Override
            public void onError(Exception exception) {
                mainHandler.post(() -> callback.onError(exception));
            }
        });
    }

    public long create(String name) {
        return repository.createDrawing(name);
    }

    public void rename(long id, String newName) {
        repository.rename(id, newName);
    }
}

