package com.example.tripboxie.utils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class DayPlanTabAdapter extends FragmentStateAdapter {
    private int planId;
    private String date;

    public DayPlanTabAdapter(@NonNull FragmentActivity fa, int planId, String date) {
        super(fa);
        this.planId = planId;
        this.date = date;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return WishListFragment.newInstance(planId, date);
        } else {
            return MapListFragment.newInstance(planId, date);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
