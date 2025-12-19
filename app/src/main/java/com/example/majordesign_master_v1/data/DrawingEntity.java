package com.example.majordesign_master_v1.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "drawings")
public class DrawingEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public long createdAt;
    public long updatedAt;
    public byte[] bitmapBytes;
    public String undoStack;
    public String redoStack;
    public byte[] thumbnail;

    public DrawingEntity(String name,
                         long createdAt,
                         long updatedAt,
                         byte[] bitmapBytes,
                         String undoStack,
                         String redoStack,
                         byte[] thumbnail) {
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.bitmapBytes = bitmapBytes;
        this.undoStack = undoStack;
        this.redoStack = redoStack;
        this.thumbnail = thumbnail;
    }
}

