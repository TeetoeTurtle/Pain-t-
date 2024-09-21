package com.paint021;

import javafx.scene.control.Tab;
import javafx.scene.image.WritableImage;

import java.util.Stack;

public class TabController {

    private Tab tab;
    private WritableImage image;
    private Stack<WritableImage> undoStack = new Stack<>();
    private Stack<WritableImage> redoStack = new Stack<>();

    public TabController() {
        tab = new Tab();

    }

    public TabController(Tab tab, WritableImage image) {
        this.tab = tab;
        this.image = image;

    }

    public Tab getTab() {
        return tab;
    }

    public void setTab(Tab tab) {
        this.tab = tab;
    }

    public WritableImage getImage() {
        return image;
    }

    public void setImage(WritableImage image) {
        this.image = image;
    }

    public Stack<WritableImage> getUndoStack() {
        return undoStack;
    }

    public void setUndoStack(Stack<WritableImage> undoStack) {
        this.undoStack = undoStack;
    }

    public Stack<WritableImage> getRedoStack() {
        return redoStack;
    }

    public void setRedoStack(Stack<WritableImage> redoStack) {
        this.redoStack = redoStack;
    }
}
