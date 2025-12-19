package com.example.majordesign_master_v1.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;

import androidx.lifecycle.LiveData;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DrawingRepository {
    public interface ExportCallback {
        void onExported(List<String> outputPaths);
        void onError(Exception exception);
    }

    private final Context appContext;
    private final DrawingDao drawingDao;
    private final Executor ioExecutor = Executors.newSingleThreadExecutor();

    public DrawingRepository(Context context) {
        appContext = context.getApplicationContext();
        drawingDao = DrawingDatabase.getInstance(appContext).drawingDao();
    }

    public long createDrawing(String name) {
        long now = System.currentTimeMillis();
        DrawingEntity entity = new DrawingEntity(name, now, now, null, "[]", "[]", null);
        return drawingDao.insert(entity);
    }

    public DrawingEntity getById(long id) {
        return drawingDao.findByIdSync(id);
    }

    public void updateDrawing(DrawingEntity entity) {
        entity.updatedAt = System.currentTimeMillis();
        drawingDao.update(entity);
    }

    public DrawingEntity latest() {
        return drawingDao.latestSync();
    }

    public LiveData<List<DrawingEntity>> observeAll() {
        return drawingDao.observeAll();
    }

    public LiveData<List<DrawingEntity>> observeSearch(String queryText) {
        String normalized = queryText == null ? "" : queryText.trim();
        return normalized.isEmpty() ? drawingDao.observeAll() : drawingDao.search(normalized);
    }

    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        ioExecutor.execute(() -> drawingDao.deleteByIds(ids));
    }

    public void rename(long id, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            return;
        }
        ioExecutor.execute(() -> {
            DrawingEntity entity = drawingDao.findByIdSync(id);
            if (entity == null) {
                return;
            }
            entity.name = newName.trim();
            entity.updatedAt = System.currentTimeMillis();
            drawingDao.update(entity);
        });
    }

    public void export(List<Long> ids, ExportCallback callback) {
        if (ids == null || ids.isEmpty()) {
            if (callback != null) {
                callback.onError(new IllegalArgumentException("No drawings selected"));
            }
            return;
        }
        ioExecutor.execute(() -> {
            List<String> output = new ArrayList<>();
            try {
                List<DrawingEntity> entities = drawingDao.findByIdsSync(ids);
                ContentResolver resolver = appContext.getContentResolver();
                for (DrawingEntity entity : entities) {
                    if (entity == null || entity.bitmapBytes == null) {
                        continue;
                    }
                    String displayName = entity.name == null || entity.name.isEmpty()
                            ? "drawing_" + entity.id
                            : entity.name;
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DISPLAY_NAME, displayName + ".jpg");
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(MediaStore.Images.Media.IS_PENDING, 1);
                    Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    if (uri == null) {
                        continue;
                    }
                    try (OutputStream os = resolver.openOutputStream(uri)) {
                        if (os != null) {
                            os.write(entity.bitmapBytes);
                        }
                    }
                    values.clear();
                    values.put(MediaStore.Images.Media.IS_PENDING, 0);
                    resolver.update(uri, values, null, null);
                    output.add(uri.toString());
                }
                if (callback != null) {
                    callback.onExported(output);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public static byte[] bitmapToJpegBytes(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream);
        return stream.toByteArray();
    }

    public static Bitmap createEmptyBitmap(int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.TRANSPARENT);
        return bitmap;
    }

    public static byte[] serializeStack(List<Bitmap> stack) {
        if (stack == null || stack.isEmpty()) {
            return "[]".getBytes();
        }
        List<String> encoded = new ArrayList<>(stack.size());
        for (Bitmap bmp : stack) {
            if (bmp == null) {
                continue;
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            encoded.add(Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP));
        }
        return ("[" + String.join(",", encoded) + "]").getBytes();
    }

    public static List<Bitmap> deserializeStack(byte[] data) {
        List<Bitmap> stack = new ArrayList<>();
        if (data == null || data.length == 0) {
            return stack;
        }
        String json = new String(data);
        if (json.equals("[]")) {
            return stack;
        }
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                String encoded = array.optString(i, null);
                if (encoded != null) {
                    byte[] bytes = Base64.decode(encoded, Base64.NO_WRAP);
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if (bmp != null) {
                        stack.add(bmp);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return stack;
    }

    public DrawingState toState(DrawingEntity entity, int width, int height) {
        if (entity == null) {
            return new DrawingState(createEmptyBitmap(width, height), new Stack<>(), new Stack<>());
        }
        Bitmap base = entity.bitmapBytes != null
                ? BitmapFactory.decodeByteArray(entity.bitmapBytes, 0, entity.bitmapBytes.length)
                : createEmptyBitmap(width, height);
        Stack<Bitmap> undoStack = toStack(entity.undoStack);
        Stack<Bitmap> redoStack = toStack(entity.redoStack);
        return new DrawingState(base, undoStack, redoStack);
    }

    private Stack<Bitmap> toStack(String serialized) {
        Stack<Bitmap> stack = new Stack<>();
        if (serialized == null || serialized.isEmpty()) {
            return stack;
        }
        try {
            JSONArray array = new JSONArray(serialized);
            for (int i = 0; i < array.length(); i++) {
                String encoded = array.optString(i, null);
                if (encoded == null) {
                    continue;
                }
                byte[] bytes = Base64.decode(encoded, Base64.NO_WRAP);
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bmp != null) {
                    stack.add(bmp);
                }
            }
        } catch (JSONException e) {
            // no-op; return current stack
        }
        return stack;
    }

    public void persistState(long id, DrawingState state) {
        if (state == null) {
            return;
        }
        ioExecutor.execute(() -> {
            DrawingEntity entity = drawingDao.findByIdSync(id);
            if (entity == null) {
                return;
            }
            Bitmap currentBitmap = state.getCurrentBitmap();
            Bitmap flattened = flattenWithBackground(currentBitmap, Color.WHITE);
            entity.bitmapBytes = bitmapToJpegBytes(flattened);
            entity.undoStack = stackToJson(state.getUndoStack());
            entity.redoStack = stackToJson(state.getRedoStack());
            entity.thumbnail = generateThumbnail(flattened);
            updateDrawing(entity);
        });
    }

    public byte[] generateThumbnail(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        Bitmap thumb = BitmapConverters.createThumbnail(bitmap, 128);
        return BitmapConverters.bitmapToJpegBytes(thumb, 85);
    }

    public Bitmap flattenWithBackground(Bitmap source, int backgroundColor) {
        if (source == null) {
            return null;
        }
        Bitmap target = Bitmap.createBitmap(Math.max(1, source.getWidth()), Math.max(1, source.getHeight()), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(target);
        canvas.drawColor(backgroundColor);
        canvas.drawBitmap(source, 0, 0, null);
        return target;
    }

    private String stackToJson(Stack<Bitmap> stack) {
        JSONArray array = new JSONArray();
        if (stack != null) {
            for (Bitmap bmp : stack) {
                if (bmp == null) {
                    continue;
                }
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                String encoded = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);
                array.put(encoded);
            }
        }
        return array.toString();
    }
}
