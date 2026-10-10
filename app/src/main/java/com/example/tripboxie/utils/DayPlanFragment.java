package com.example.tripboxie.utils;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripboxie.DayPlanActivity;
import com.example.tripboxie.R;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DayPlanFragment extends Fragment {
    private static final String ARG_DATE = "arg_date";
    private static final String ARG_ACTIVITIES = "arg_activities";

    private String date;
    private List<DayActivity> activityList;
    Integer planId;

    public static DayPlanFragment newInstance(int planId, String date, List<DayActivity> activities) {
        DayPlanFragment fragment = new DayPlanFragment();
        Bundle args = new Bundle();
        args.putInt("arg_planId", planId);
        args.putString("arg_date", date);
        args.putSerializable("arg_activities", (Serializable) activities);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            date = getArguments().getString(ARG_DATE);
            activityList = (List<DayActivity>) getArguments().getSerializable(ARG_ACTIVITIES);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_day_act, container, false);
        planId = getArguments().getInt("arg_planId");

        Button mButtonNew = view.findViewById(R.id.button_dayactivity_new);
        Button mButtonBack = view.findViewById(R.id.button_dayactivity_back);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_day_plan);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.bottom = spacingInPixels;  // 每個item底部加間距
            }
        });

        DayActivityAdapter adapter = new DayActivityAdapter(activityList);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new DayActivityAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(DayActivity dayActivity) {
                Dialog mDialogMore = new Dialog(requireContext());
                mDialogMore.setCancelable(false);
                mDialogMore.setContentView(R.layout.day_plan_edit_dlg);

                RecyclerView todoRecycler = mDialogMore.findViewById(R.id.recyclerView_todo_list);
                int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing);
                todoRecycler.addItemDecoration(new RecyclerView.ItemDecoration() {
                    @Override
                    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                        outRect.bottom = spacingInPixels;  // 每個item底部加間距
                    }
                });

                EditText todoInput = mDialogMore.findViewById(R.id.editText_new_todo);
                Button todoAdd = mDialogMore.findViewById(R.id.button_add_todo);
                Button todoDel = mDialogMore.findViewById(R.id.button_day_act_del);
                Button todoBack = mDialogMore.findViewById(R.id.button_day_act_back);

                Gson gson = new Gson();
                AppDatabase db = AppDatabase.getInstance(requireContext());
                DayPlanDao dao = db.dayPlanDao();
                DayPlan dayPlan = dao.getDayPlanByPlanIdAndDate(planId, date);

                List<ToDoItem> tempTodos = dayActivity.getTodoList();
                if (tempTodos == null) {
                    tempTodos = new ArrayList<>();
                }
                final List<ToDoItem> todos = tempTodos;

                TodoListAdapter todoAdapter = new TodoListAdapter(todos, () -> {
                    new Thread(() -> {
                        if (dayPlan != null) {
                            String newJson = gson.toJson(activityList);
                            dayPlan.activitiesJson = newJson;
                            dao.update(dayPlan);
                        }
                    }).start();
                });

                todoRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
                todoRecycler.setAdapter(todoAdapter);

                todoAdd.setOnClickListener(v -> {
                    String newTodo = todoInput.getText().toString().trim();
                    if (!newTodo.isEmpty()) {
                        todos.add(new ToDoItem(newTodo, false));
                        todoAdapter.notifyItemInserted(todos.size() - 1);
                        todoInput.setText("");

                        new Thread(() -> {
                            if (dayPlan != null) {
                                String newJson = gson.toJson(activityList);
                                dayPlan.activitiesJson = newJson;
                                dao.update(dayPlan);
                            }
                        }).start();
                    }
                });

                todoDel.setOnClickListener(v -> {
                    activityList.remove(dayActivity);
                    adapter.notifyDataSetChanged();

                    new Thread(() -> {
                        if (dayPlan != null) {
                            String newJson = gson.toJson(activityList, new com.google.gson.reflect.TypeToken<List<DayActivity>>(){}.getType());
                            dayPlan.activitiesJson = newJson;
                            dao.update(dayPlan);
                        }
                    }).start();

                    mDialogMore.dismiss();
                });

                todoBack.setOnClickListener(v -> mDialogMore.dismiss());

                mDialogMore.show();
            }
        });

        mButtonNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), DayPlanActivity.class);
                intent.putExtra("plan_id", planId);
                intent.putExtra("date", date);
                startActivity(intent);
            }
        });

        mButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().finish();
            }
        });

        return view;
    }
}
