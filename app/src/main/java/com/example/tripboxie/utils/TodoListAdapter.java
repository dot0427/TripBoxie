package com.example.tripboxie.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripboxie.R;

import java.util.List;

public class TodoListAdapter extends RecyclerView.Adapter<TodoListAdapter.TodoViewHolder> {
    private List<ToDoItem> todoItems;
    private OnItemChangedListener listener;

    public interface OnItemChangedListener {
        void onItemChanged();
    }

    public TodoListAdapter(List<ToDoItem> todoItems, OnItemChangedListener listener) {
        this.todoItems = todoItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_todo, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        ToDoItem item = todoItems.get(position);
        holder.checkBox.setText(item.getContent());
        holder.checkBox.setChecked(item.isDone());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setDone(isChecked);
            listener.onItemChanged();
        });

        holder.deleteButton.setOnClickListener(v -> {
            todoItems.remove(position);
            notifyItemRemoved(position);
            listener.onItemChanged();
        });
    }

    @Override
    public int getItemCount() {
        return todoItems.size();
    }

    static class TodoViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        Button deleteButton;

        TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox_todo);
            deleteButton = itemView.findViewById(R.id.button_delete_todo);
        }
    }
}
