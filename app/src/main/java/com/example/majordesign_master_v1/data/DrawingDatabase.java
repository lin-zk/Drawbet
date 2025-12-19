package com.example.majordesign_master_v1.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {DrawingEntity.class}, version = 1, exportSchema = false)
public abstract class DrawingDatabase extends RoomDatabase {
    private static volatile DrawingDatabase INSTANCE;

    public abstract DrawingDao drawingDao();

    public static DrawingDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (DrawingDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    DrawingDatabase.class,
                                    "drawing_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

