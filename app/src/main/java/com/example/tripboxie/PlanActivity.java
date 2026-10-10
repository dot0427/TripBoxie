package com.example.tripboxie;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tripboxie.utils.AppDatabase;
import com.example.tripboxie.utils.DayPlanFragmentAdapter;
import com.example.tripboxie.utils.DayPlan;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlanActivity extends AppCompatActivity {

    ViewPager2 mViewPager;
    TabLayout mTabLayout;
    AppDatabase db;
    SimpleDateFormat noYearFormat = new SimpleDateFormat("MM/dd", Locale.US);

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_plan);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        int planId = intent.getIntExtra("id", -1);

        mViewPager = findViewById(R.id.ViewPager);
        mTabLayout = findViewById(R.id.TabLayout);

        db = AppDatabase.getInstance(getApplicationContext());
        List<DayPlan> dayPlans = db.dayPlanDao().getDaysByPlanId(planId);
        DayPlanFragmentAdapter adapter = new DayPlanFragmentAdapter(getSupportFragmentManager(), getLifecycle(), dayPlans, planId);
        mViewPager.setAdapter(adapter);

        new TabLayoutMediator(mTabLayout, mViewPager, (tab, position) -> {
            String dateStr = dayPlans.get(position).getDate();
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
                Date date = inputFormat.parse(dateStr);
                tab.setText(noYearFormat.format(date));
            } catch (ParseException e) {
                tab.setText(dayPlans.get(position).getDate());
                e.printStackTrace();
            }
        }).attach();

    }

    @Override
    protected void onResume() {
        super.onResume();
        int planId = getIntent().getIntExtra("id", -1);

        List<DayPlan> updatedDayPlans = db.dayPlanDao().getDaysByPlanId(planId);
        DayPlanFragmentAdapter adapter = new DayPlanFragmentAdapter(
                getSupportFragmentManager(), getLifecycle(), updatedDayPlans, planId);
        mViewPager.setAdapter(adapter);

        new TabLayoutMediator(mTabLayout, mViewPager, (tab, position) -> {
            String dateStr = updatedDayPlans.get(position).getDate();
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
                Date date = inputFormat.parse(dateStr);
                tab.setText(noYearFormat.format(date));
            } catch (ParseException e) {
                tab.setText(updatedDayPlans.get(position).getDate());
                e.printStackTrace();
            }
        }).attach();
    }
}
