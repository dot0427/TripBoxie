package com.example.tripboxie;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripboxie.utils.AppDatabase;
import com.example.tripboxie.utils.CustomToast;
import com.example.tripboxie.utils.DayPlan;
import com.example.tripboxie.utils.Plan;
import com.example.tripboxie.utils.PlanAdapter;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    PlanAdapter mplanAdapter;
    AppDatabase db;
    Button mButtonAdd, mButtonPlan;
    Dialog mDialogNewPlan;
    String newLocation = "", newStartDate = "", newEndDate = "", CalenderFormat = "yyyy/MM/dd";
    Calendar startCalendar = Calendar.getInstance();
    Calendar endCalendar = Calendar.getInstance();
    SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
    SimpleDateFormat noYearFormat = new SimpleDateFormat("MM/dd", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mButtonAdd = findViewById(R.id.button_add);
        mButtonPlan = findViewById(R.id.button_plan);

        db = AppDatabase.getInstance(getApplicationContext());

        mRecyclerView = findViewById(R.id.recyclerView_plan);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing);
        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.bottom = spacingInPixels;  // 每個item底部加間距
            }
        });

        loadPlanList();

        mButtonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WishActivity.class);
                startActivity(intent);
            }
        });

        mButtonPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogNewPlan = new Dialog(MainActivity.this);
                mDialogNewPlan.setCancelable(false);
                mDialogNewPlan.setContentView(R.layout.new_plan_dlg);

                EditText mEditTextLocation = mDialogNewPlan.findViewById(R.id.editTextText_location);
                EditText mEditTextStartDate = mDialogNewPlan.findViewById(R.id.editTextText_start_date);
                EditText mEditTextEndDate = mDialogNewPlan.findViewById(R.id.editTextText_end_date);
                Button mButtonConfirm = mDialogNewPlan.findViewById(R.id.button_confirm_new_plan);
                Button mButtonBack = mDialogNewPlan.findViewById(R.id.button_back_plan);

                mEditTextStartDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("選擇出發日期")
                                .setTheme(R.style.CustomDatePickerTheme).build();
                        datePicker.show(getSupportFragmentManager(), "START_PICKER");

                        datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                            @Override
                            public void onPositiveButtonClick(Long selection) {
                                Calendar selectedDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                                selectedDate.setTimeInMillis(selection);

                                startCalendar = selectedDate;
                                mEditTextStartDate.setText(fullFormat.format(startCalendar.getTime()));
                            }
                        });
                    }
                });

                mEditTextEndDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("選擇結束日期")
                                .setTheme(R.style.CustomDatePickerTheme).build();
                        datePicker.show(getSupportFragmentManager(), "END_PICKER");

                        datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                            @Override
                            public void onPositiveButtonClick(Long selection) {
                                Calendar selectedDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                                selectedDate.setTimeInMillis(selection);

                                endCalendar = selectedDate;
                                mEditTextEndDate.setText(fullFormat.format(endCalendar.getTime()));
                            }
                        });
                    }
                });

                mButtonConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (endCalendar.before(startCalendar)) {
                            CustomToast.show(MainActivity.this, "結束日期不能早於開始日期><");
                            return;
                        }
                        if(mEditTextStartDate.getText().toString().isEmpty()||mEditTextEndDate.getText().toString().isEmpty()){
                            CustomToast.show(MainActivity.this, "尚未選完兩個日期><");
                            return;
                        }

                        newLocation = mEditTextLocation.getText().toString();
                        newStartDate = mEditTextStartDate.getText().toString();
                        newEndDate = mEditTextEndDate.getText().toString();

                        long diffInMillis = endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();
                        long days = diffInMillis / (24 * 60 * 60 * 1000) + 1; //加1表示當日也算

                        // 插入 Plan
                        Plan newPlan = new Plan(newLocation, newStartDate, newEndDate);
                        db.planDao().insert(newPlan);
                        // 拿回它的 id
                        Plan insertedPlan = db.planDao().getAll().get(db.planDao().getAll().size() - 1);
                        int planId = insertedPlan.getId();
                        List<DayPlan> dayPlans = new ArrayList<>();
                        // 產生 DayPlan
                        Calendar cal = Calendar.getInstance();
                        try {
                            cal.setTime(fullFormat.parse(newStartDate));
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                        for (int i = 0; i < days; i++) {
                            String date = fullFormat.format(cal.getTime());
                            DayPlan Dp = new DayPlan(planId, date, "[]");
                            dayPlans.add(Dp);
                            cal.add(Calendar.DATE, 1);
                        }
                        for (DayPlan newDp:dayPlans){
                            db.dayPlanDao().insert(newDp);
                        }

                        Intent intent = new Intent(MainActivity.this, PlanActivity.class);
                        intent.putExtra("id", planId);
                        startActivity(intent);

                        mDialogNewPlan.cancel();
                    }
                });

                mButtonBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialogNewPlan.cancel();
                    }
                });

                mDialogNewPlan.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlanList();
    }

    private void loadPlanList() {
        List<Plan> planList = db.planDao().getAll();
        mplanAdapter = new PlanAdapter(planList);

        mplanAdapter.setOnItemClickListener(new PlanAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Plan plan) {
                int id = plan.getId();
                Dialog mDialogPlanEdit = new Dialog(MainActivity.this);
                mDialogPlanEdit.setCancelable(false);
                mDialogPlanEdit.setContentView(R.layout.plan_edit_dlg);

                TextView mTextViewLocation = mDialogPlanEdit.findViewById(R.id.textView_location);
                TextView mTextViewDate = mDialogPlanEdit.findViewById(R.id.textView_date);
                Button mButtonEdit = mDialogPlanEdit.findViewById(R.id.button_edit_plan);
                Button mButtonDel = mDialogPlanEdit.findViewById(R.id.button_del_plan);
                Button mButtonBack = mDialogPlanEdit.findViewById(R.id.button_back_plan);

                Plan planClick = db.planDao().getPlanById(id);
                mTextViewLocation.setText(planClick.getLocation());
                try {
                    Date startDate = fullFormat.parse(planClick.getStartDate());
                    Date endDate = fullFormat.parse(planClick.getEndDate());
                    String displayText = fullFormat.format(startDate) + " ~ " + noYearFormat.format(endDate);
                    mTextViewDate.setText(displayText);
                } catch (ParseException e) {
                    e.printStackTrace();
                    mTextViewDate.setText(planClick.getStartDate() + " ~ " + planClick.getEndDate());
                }

                mButtonEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, PlanActivity.class);
                        intent.putExtra("id", id);
                        startActivity(intent);
                        mDialogPlanEdit.cancel();
                    }
                });

                mButtonDel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        db.planDao().deleteById(id);
                        loadPlanList();
                        mDialogPlanEdit.cancel();
                    }
                });

                mButtonBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialogPlanEdit.cancel();
                    }
                });

                mDialogPlanEdit.show();
            }
        });

        mRecyclerView.setAdapter(mplanAdapter);
    }
}