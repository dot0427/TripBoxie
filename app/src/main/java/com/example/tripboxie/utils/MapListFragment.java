package com.example.tripboxie.utils;

import android.app.Dialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.tripboxie.PlanActivity;
import com.example.tripboxie.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapListFragment extends Fragment implements OnMapReadyCallback {
    AppDatabase db;
    int planId, mOrderIndex;
    String date, mStartTime, mEndTime;
    double lastLat, lastLng;
    GoogleMap mMap;
    Dialog mDialogMarker, mDialogTime;
    private final Map<Marker, Wish> markerWishMap = new HashMap<>();
    private Gson gson;
    DayPlan dayPlan;

    public static MapListFragment newInstance(int planId, String date) {
        MapListFragment fragment = new MapListFragment();
        Bundle args = new Bundle();
        args.putInt("arg_planId", planId);
        args.putString("arg_date", date);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_list, container, false);

        planId = getArguments().getInt("arg_planId");
        date = getArguments().getString("arg_date");
        db = AppDatabase.getInstance(requireContext());

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map3);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        new Thread(() -> {
            dayPlan = db.dayPlanDao().getDayPlanByPlanIdAndDate(planId, date);
            if (dayPlan == null) {
                requireActivity().runOnUiThread(() -> {
                    CustomToast.show(requireContext(), "找不到行程資料！");
                    requireActivity().finish();
                });
                return;
            }

            gson = new Gson();
            Type listType = new TypeToken<List<DayActivity>>() {}.getType();
            List<DayActivity> activityList = gson.fromJson(dayPlan.getActivitiesJson(), listType);
            if (activityList == null || activityList.isEmpty()){
                return;}

            DayActivity lastActivity = activityList.get(activityList.size() - 1);
            lastLat = lastActivity.getLatitude();
            lastLng = lastActivity.getLongitude();

            List<Wish> wishList = db.wishDao().getAll();
            List<Wish> nearbyWishes = new ArrayList<>();
            for (Wish wish : wishList) {
                if (wish.getLatitude() == 0.0 && wish.getLongitude() == 0.0) continue;
                float[] result = new float[1];
                Location.distanceBetween(
                        lastLat, lastLng,
                        wish.getLatitude(), wish.getLongitude(),
                        result
                );
                float distanceInMeters = result[0];
                if (distanceInMeters <= 10000) {
                    nearbyWishes.add(wish);
                }
            }

            requireActivity().runOnUiThread(() -> {
                LatLng lastLatLng = new LatLng(lastLat, lastLng);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng, 13f));

                for (Wish wish : nearbyWishes) {
                    LatLng wishLatLng = new LatLng(wish.getLatitude(), wish.getLongitude());
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(wishLatLng)
                            .title(wish.getName())
                            .snippet("距離約 " + String.format("%.2f",
                                    distanceInKm(lastLat, lastLng, wish.getLatitude(), wish.getLongitude())) + " 公里"));
                    markerWishMap.put(marker, wish);
                }

                mMap.setOnMarkerClickListener(marker -> {
                    Wish wish = markerWishMap.get(marker);
                    if (wish != null) {
                        showAddWishDialog(wish);
                    }
                    return true;
                });
            });
        }).start();

        new Thread(() -> {
            dayPlan = db.dayPlanDao().getDayPlanByPlanIdAndDate(planId, date);

            if (dayPlan == null) {
                requireActivity().runOnUiThread(() -> {
                    CustomToast.show(requireContext(), "找不到行程資料！");
                    requireActivity().finish();
                });
            }
        }).start();

    }
    private void showAddWishDialog(Wish wish) {

        if (dayPlan == null) {
            CustomToast.show(requireContext(), "請稍候，資料尚未載入");
            return;
        }

        mDialogMarker = new Dialog(requireContext());
        mDialogMarker.setCancelable(false);
        mDialogMarker.setContentView(R.layout.map_add_dlg);
        TextView mName = mDialogMarker.findViewById(R.id.Text_map_name);
        TextView mAddress = mDialogMarker.findViewById(R.id.Text_map_address);
        TextView mCountry = mDialogMarker.findViewById(R.id.Text_map_country);
        TextView mUri = mDialogMarker.findViewById(R.id.Text_map_uri);
        Button mConfirm = mDialogMarker.findViewById(R.id.button_map_confirm);
        Button mBack = mDialogMarker.findViewById(R.id.button_map_back);

        mName.setText(wish.getName());
        mAddress.setText(wish.getAddress());
        mCountry.setText(wish.getCountry());
        mUri.setText(wish.getUri());

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogTime = new Dialog(requireContext());
                mDialogTime.setCancelable(false);
                mDialogTime.setContentView(R.layout.time_dlg);

                EditText mEditTextStartTime = mDialogTime.findViewById(R.id.editTextText_startTime);
                EditText mEditTextEndTime = mDialogTime.findViewById(R.id.editTextText_endTime);
                Button mButtonTimeConfirm = mDialogTime.findViewById(R.id.button_confirm_time);
                Button mButtonTimeBack = mDialogTime.findViewById(R.id.button_back_time);

                mEditTextStartTime.setFocusable(false);
                mEditTextEndTime.setFocusable(false);

                Type listType = new com.google.gson.reflect.TypeToken<List<DayActivity>>(){}.getType();
                List<DayActivity> initialList = gson.fromJson(dayPlan.getActivitiesJson(), listType);
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

                        DayActivity dayActivity = new DayActivity(wish.getName(), wish.getAddress(), wish.getCountry(), wish.getUri()
                                , wish.getLatitude(), wish.getLongitude(), mStartTime, mEndTime, mOrderIndex);

                        activityList.add(dayActivity);

                        // 更新 JSON 字串回到 DayPlan
                        Collections.sort(activityList, (a, b) -> {
                            int aStart = parseMinutes(a.getStartTime());
                            int bStart = parseMinutes(b.getStartTime());
                            return Integer.compare(aStart, bStart);
                        });
                        String updatedJson = gson.toJson(activityList);
                        dayPlan.activitiesJson = updatedJson;

                        db.dayPlanDao().update(dayPlan);

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
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogMarker.cancel();
            }
        });

        mDialogMarker.show();
    }

    public static double distanceInKm(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371.0; // 公里
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }
    private int parseMinutes(String time) {
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        return hour * 60 + minute;
    }
}
