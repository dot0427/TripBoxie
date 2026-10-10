package com.example.tripboxie;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tripboxie.utils.DayPlanTabAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class DayPlanActivity extends AppCompatActivity {

    ViewPager2 mViewPager;
    TabLayout mTabLayout;
    Button mButtonNew, mButtonBack;
    Integer planId;
    String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_day_plan);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        planId = intent.getIntExtra("plan_id", -1);
        date = intent.getStringExtra("date");

        mViewPager = findViewById(R.id.ViewPager2);
        mTabLayout = findViewById(R.id.TabLayout2);
        mButtonNew = findViewById(R.id.button_new_day_act);
        mButtonBack = findViewById(R.id.button_back_day_act);

        mButtonNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DayPlanActivity.this, NewDayPlanActivity.class);
                intent.putExtra("plan_id", planId);
                intent.putExtra("date", date);
                startActivity(intent);
            }
        });

        mButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        DayPlanTabAdapter adapter = new DayPlanTabAdapter(this, planId, date);
        mViewPager.setAdapter(adapter);

        new TabLayoutMediator(mTabLayout, mViewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("願望清單");
                    } else {
                        tab.setText("附近景點地圖");
                    }
                }
        ).attach();
    }
}