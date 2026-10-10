package com.example.tripboxie.utils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.List;


public class DayPlanFragmentAdapter extends FragmentStateAdapter {
    private List<DayPlan> dayPlans;
    private int planId;

    public DayPlanFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, List<DayPlan> dayPlans, int planId) {
        super(fragmentManager, lifecycle);
        this.dayPlans = dayPlans;
        this.planId = planId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        DayPlan dp = dayPlans.get(position);

        Type type = new TypeToken<List<DayActivity>>(){}.getType();
        List<DayActivity> activities = new Gson().fromJson(dp.getActivitiesJson(), type);

        return DayPlanFragment.newInstance(planId, dp.getDate(), activities);
    }

    @Override
    public int getItemCount() {
        return dayPlans.size();
    }
}