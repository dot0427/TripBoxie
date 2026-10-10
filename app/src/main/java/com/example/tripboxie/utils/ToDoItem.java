package com.example.tripboxie.utils;

import java.io.Serializable;

public class ToDoItem implements Serializable {
    private String content;
    private boolean done;

    public ToDoItem(String content, boolean done) {
        this.content = content;
        this.done = done;
    }

    public String getContent() { return content; }
    public boolean isDone() { return done; }

    public void setContent(String content) { this.content = content; }
    public void setDone(boolean done) { this.done = done; }
}
