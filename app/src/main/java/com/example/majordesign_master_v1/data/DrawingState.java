package com.example.majordesign_master_v1.data;

import android.graphics.Bitmap;

import java.util.Stack;

/**
 * Represents the full in-memory state of a drawing, including the visible bitmap
 * plus undo/redo stacks used by the canvas widget.
 */
public class DrawingState {
    private final Bitmap currentBitmap;
    private final Stack<Bitmap> undoStack;
    private final Stack<Bitmap> redoStack;

    public DrawingState(Bitmap currentBitmap, Stack<Bitmap> undoStack, Stack<Bitmap> redoStack) {
        this.currentBitmap = currentBitmap;
        this.undoStack = undoStack;
        this.redoStack = redoStack;
    }

    public Bitmap getCurrentBitmap() {
        return currentBitmap;
    }

    public Stack<Bitmap> getUndoStack() {
        return undoStack;
    }

    public Stack<Bitmap> getRedoStack() {
        return redoStack;
    }
}

