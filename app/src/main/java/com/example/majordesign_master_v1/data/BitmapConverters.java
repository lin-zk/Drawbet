package com.example.majordesign_master_v1.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;

import java.io.ByteArrayOutputStream;

public final class BitmapConverters {
    private BitmapConverters() {}

    public static byte[] bitmapToBytes(Bitmap bitmap) {
        return bitmapToBytes(bitmap, Bitmap.CompressFormat.PNG, 100);
    }

    public static byte[] bitmapToBytes(Bitmap bitmap, Bitmap.CompressFormat format, int quality) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(format, quality, stream);
        return stream.toByteArray();
    }

    public static byte[] bitmapToJpegBytes(Bitmap bitmap, int quality) {
        return bitmapToBytes(bitmap, Bitmap.CompressFormat.JPEG, quality);
    }

    public static Bitmap bytesToBitmap(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public static Bitmap createEmptyBitmap(int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(Math.max(width, 1), Math.max(height, 1), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.TRANSPARENT);
        return bitmap;
    }

    public static Bitmap copyBitmap(Bitmap source) {
        if (source == null) {
            return null;
        }
        return source.copy(Bitmap.Config.ARGB_8888, true);
    }

    public static Bitmap createThumbnail(Bitmap source, int size) {
        if (source == null || size <= 0) {
            return null;
        }
        int dimension = Math.min(source.getWidth(), source.getHeight());
        int xOffset = (source.getWidth() - dimension) / 2;
        int yOffset = (source.getHeight() - dimension) / 2;
        Bitmap squared = Bitmap.createBitmap(source, xOffset, yOffset, dimension, dimension);
        return Bitmap.createScaledBitmap(squared, size, size, true);
    }
}
