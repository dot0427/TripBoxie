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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripboxie.PlanActivity;
import com.example.tripboxie.R;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class WishListFragment extends Fragment {

    private RecyclerView recyclerView;
    private WishAdapter mWishAdapter;
    AppDatabase db;
    Dialog mDialogTime;
    String mName = "", mAddress = "", mCountry = "", mUri = "", mStartTime = "", mEndTime = "";
    Double mLatitude, mLongitude;

    public static WishListFragment newInstance(int planId, String date) {
        WishListFragment fragment = new WishListFragment();
        Bundle args = new Bundle();
        args.putInt("arg_planId", planId);
        args.putString("arg_date", date);
        fragment.setArguments(args);
        return fragment;
    }

    private List<DayActivity> activityList = new ArrayList<>();
    private int mOrderIndex = 0;
    private Gson gson;
    private DayPlan currentDayPlan;
    private int planId = -1;
    String date;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wish_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewWish);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.bottom = spacingInPixels;  // 每個item底部加間距
            }
        });

        db = AppDatabase.getInstance(getContext());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadWishList();
    }

    private void loadWishList() {
        List<Wish> wishList = db.wishDao().getAll();
        mWishAdapter = new WishAdapter(wishList);
        planId = getArguments().getInt("arg_planId");
        date = getArguments().getString("arg_date");

        new Thread(() -> {
            currentDayPlan = db.dayPlanDao().getDayPlanByPlanIdAndDate(planId, date);

            if (currentDayPlan == null) {
                requireActivity().runOnUiThread(() -> {
                    CustomToast.show(requireContext(), "找不到行程資料！");
                    requireActivity().finish();
                });
            }
        }).start();

        mWishAdapter.setOnItemClickListener(new WishAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Wish wish) {
                if (currentDayPlan == null) {
                    CustomToast.show(requireContext(), "行程尚未載入，請稍後再試");
                    return;
                }
                
                int id = wish.getId();
                Wish wishClick = db.wishDao().getWishById(id);
                mName = wishClick.getName();
                mAddress = wishClick.getAddress();
                mCountry = wishClick.getCountry();
                mUri += wishClick.getUri();
                mLatitude = wishClick.getLatitude();
                mLongitude = wishClick.getLongitude();

                mDialogTime = new Dialog(requireContext());
                mDialogTime.setCancelable(false);
                mDialogTime.setContentView(R.layout.time_dlg);

                EditText mEditTextStartTime = mDialogTime.findViewById(R.id.editTextText_startTime);
                EditText mEditTextEndTime = mDialogTime.findViewById(R.id.editTextText_endTime);
                Button mButtonTimeConfirm = mDialogTime.findViewById(R.id.button_confirm_time);
                Button mButtonTimeBack = mDialogTime.findViewById(R.id.button_back_time);

                mEditTextStartTime.setFocusable(false);
                mEditTextEndTime.setFocusable(false);

                gson = new Gson();

                Type listType = new TypeToken<List<DayActivity>>(){}.getType();
                List<DayActivity> initialList = gson.fromJson(currentDayPlan.getActivitiesJson(), listType);
                if (initialList == null) initialList = new ArrayList<>();
                final List<DayActivity> activityList = new ArrayList<>(initialList);

                int ActOrderIndex = initialList.size();

                final int[] startTime = {0};
                final int[] endTime = {0};

                mEditTextStartTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                                .setTimeFormat(TimeFormat.CLOCK_24H)
                                .setHour(9)
                                .setMinute(0)
                                .setTitleText("選擇開始時間")
                                .setTheme(R.style.CustomTimePickerTheme)
                                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                                .build();

                        picker.addOnPositiveButtonClickListener(dialog -> {
                            int hour = picker.getHour();
                            int minute = picker.getMinute();
                            String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                            mEditTextStartTime.setText(formattedTime);
                            startTime[0] = hour * 60 + minute;
                        });

                        picker.show(getParentFragmentManager(), "TIME_PICKER");
                    }
                });

                mEditTextEndTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                                .setTimeFormat(TimeFormat.CLOCK_24H)
                                .setHour(9)
                                .setMinute(0)
                                .setTitleText("選擇結束時間")
                                .setTheme(R.style.CustomTimePickerTheme)
                                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                                .build();

                        picker.addOnPositiveButtonClickListener(dialog -> {
                            int hour = picker.getHour();
                            int minute = picker.getMinute();
                            String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                            mEditTextEndTime.setText(formattedTime);
                            endTime[0] = hour * 60 + minute;
                        });

                        picker.show(getParentFragmentManager(), "TIME_PICKER");
                    }
                });

                mButtonTimeConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mStartTime = mEditTextStartTime.getText().toString();
                        mEndTime = mEditTextEndTime.getText().toString();
                        mOrderIndex = ActOrderIndex;

                        if(endTime[0] < startTime[0]){
                            CustomToast.show(requireContext(), "結束時間不能早於開始時間");
                            return;
                        }

                        DayActivity dayActivity = new DayActivity(mName, mAddress, mCountry, mUri
                                , mLatitude, mLongitude, mStartTime, mEndTime, mOrderIndex);

                        activityList.add(dayActivity);

                        // 更新 JSON 字串回到 DayPlan
                        Collections.sort(activityList, (a, b) -> {
                            int aStart = parseMinutes(a.getStartTime());
                            int bStart = parseMinutes(b.getStartTime());
                            return Integer.compare(aStart, bStart);
                        });
                        String updatedJson = gson.toJson(activityList);
                        currentDayPlan.activitiesJson = updatedJson;

                        db.dayPlanDao().update(currentDayPlan);

                        Intent intent = new Intent(requireContext(), PlanActivity.class);
                        intent.putExtra("plan_id", planId);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                    }
                });
                mButtonTimeBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialogTime.cancel();
                    }
                });
                mDialogTime.show();
            }
        });

        recyclerView.setAdapter(mWishAdapter);
    }
    private int parseMinutes(String time) {
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        return hour * 60 + minute;
    }
}

