package com.example.tripboxie;

import android.app.Dialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.example.tripboxie.utils.DayPlan;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tripboxie.utils.AppDatabase;
import com.example.tripboxie.utils.CustomToast;
import com.example.tripboxie.utils.DayActivity;
import com.example.tripboxie.utils.GeocodingHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class NewDayPlanActivity extends AppCompatActivity implements OnMapReadyCallback {
    Button mButtonDayAdd, mButtonDayGo, mButtonDayBack;
    Dialog mDialogDay, mDialogTime;
    EditText mEditTextKey;
    String  ActName = "", ActAddress = "", ActCountry = "", ActUri = "", ActStartTime = "", ActEndTime = "";
    double ActLatitude, ActLongitude;
    private GoogleMap mMap;
    Gson gson;
    Integer planId;
    String date;
    AppDatabase db;
    DayPlan currentDayPlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_day_plan);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync((OnMapReadyCallback) this);

        Intent intent = getIntent();
        planId = intent.getIntExtra("plan_id", -1);
        date = intent.getStringExtra("date");

        mEditTextKey = findViewById(R.id.editTextText_key_day);
        mButtonDayGo = findViewById(R.id.button_day_go);
        mButtonDayAdd = findViewById(R.id.button_day_add);
        mButtonDayBack = findViewById(R.id.button_day_back);

        mButtonDayGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActName = mEditTextKey.getText().toString();
                if (!ActName.isEmpty()) {
                    goCodeAddress(ActName);
                } else {
                    CustomToast.show(NewDayPlanActivity.this, "請輸入地點關鍵字！");
                }
            }
        });

        mButtonDayAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActName = "";
                ActAddress = "";
                ActCountry = "";
                ActUri = "";
                ActLatitude = 0;
                ActLongitude = 0;

                showAddDialog("");
            }
        });

        mButtonDayBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        db = AppDatabase.getInstance(getApplicationContext());

        new Thread(() -> {
            currentDayPlan = db.dayPlanDao().getDayPlanByPlanIdAndDate(planId, date);

            if (currentDayPlan == null) {
                runOnUiThread(() -> {
                    CustomToast.show(NewDayPlanActivity.this, "找不到行程資料！");
                    finish();
                });
            }
        }).start();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void goCodeAddress(String keyword) {
        GeocodingHelper.getCoordinatesFromAddress(keyword, new GeocodingHelper.OnGeocodingResult() {
            @Override
            public void onSuccess(double lat, double lng) {
                LatLng latLng = new LatLng(lat, lng);
                mMap.addMarker(new MarkerOptions().position(latLng).title(keyword));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                ActLatitude = lat;
                ActLongitude = lng;
                ActName = keyword;
                ActCountry = "";

                // 反查地址
                Geocoder geocoder = new Geocoder(NewDayPlanActivity.this, Locale.getDefault());
                try {
                    List<Address> addressList = geocoder.getFromLocation(lat, lng, 1);
                    if (!addressList.isEmpty()) {
                        Address addr = addressList.get(0);
                        ActAddress = addr.getAddressLine(0);
                        ActCountry = addr.getCountryName();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // 自動帶入 dialog
                showAddDialog(keyword);
            }

            @Override
            public void onError(String message) {
                CustomToast.show(NewDayPlanActivity.this, "搜尋失敗：" + message);
            }
        });
    }
    private void showAddDialog(String defaultName) {
        mDialogDay = new Dialog(NewDayPlanActivity.this);
        mDialogDay.setCancelable(false);
        mDialogDay.setContentView(R.layout.new_add_dlg);

        EditText mEditTextName = mDialogDay.findViewById(R.id.editTextText_name);
        EditText mEditTextAddress = mDialogDay.findViewById(R.id.editTextText_address_new);
        EditText mEditTextCountry = mDialogDay.findViewById(R.id.editTextText_country);
        EditText mEditTextUri = mDialogDay.findViewById(R.id.editTextText_uri);
        Button mButtonConfirm = mDialogDay.findViewById(R.id.button_confirm);
        Button mButtonBack = mDialogDay.findViewById(R.id.button_back);

        mEditTextName.setText(ActName);
        mEditTextAddress.setText(ActAddress);
        mEditTextCountry.setText(ActCountry);

        gson = new Gson();
        Type listType = new TypeToken<List<DayActivity>>(){}.getType();
        List<DayActivity> initialList = gson.fromJson(currentDayPlan.getActivitiesJson(), listType);
        if (initialList == null) initialList = new ArrayList<>(); // 防止 null
        final List<DayActivity> activityList = new ArrayList<>(initialList); // 加上 final

        int ActOrderIndex = initialList.size();

        mButtonConfirm.setOnClickListener(v -> {
            ActName = mEditTextName.getText().toString();
            ActAddress = mEditTextAddress.getText().toString();
            ActCountry = mEditTextCountry.getText().toString();
            ActUri = mEditTextUri.getText().toString();

            mDialogTime = new Dialog(NewDayPlanActivity.this);
            mDialogTime.setCancelable(false);
            mDialogTime.setContentView(R.layout.time_dlg);

            EditText mEditTextStartTime = mDialogTime.findViewById(R.id.editTextText_startTime);
            EditText mEditTextEndTime = mDialogTime.findViewById(R.id.editTextText_endTime);
            Button mButtonTimeConfirm = mDialogTime.findViewById(R.id.button_confirm_time);
            Button mButtonTimeBack = mDialogTime.findViewById(R.id.button_back_time);

            mEditTextStartTime.setFocusable(false);
            mEditTextEndTime.setFocusable(false);

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

                    picker.show(getSupportFragmentManager(), "TIME_PICKER");
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

                    picker.show(getSupportFragmentManager(), "TIME_PICKER");
                }
            });
            mButtonTimeConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActStartTime = mEditTextStartTime.getText().toString();
                    ActEndTime = mEditTextEndTime.getText().toString();

                    if(endTime[0] < startTime[0]){
                        CustomToast.show(NewDayPlanActivity.this, "結束時間不能早於開始時間");
                    }

                    DayActivity dayActivity = new DayActivity(ActName, ActAddress, ActCountry, ActUri
                            , ActLatitude, ActLongitude, ActStartTime, ActEndTime, ActOrderIndex);
                    activityList.add(dayActivity);
                    // 更新 JSON 字串回到 DayPlan（ 依照開始時間排序 ）
                    Collections.sort(activityList, (a, b) -> {
                        int aStart = parseMinutes(a.getStartTime());
                        int bStart = parseMinutes(b.getStartTime());
                        return Integer.compare(aStart, bStart);
                    });
                    String updatedJson = gson.toJson(activityList);
                    currentDayPlan.activitiesJson = updatedJson;
                    // 更新到 Room
                    AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                    db.dayPlanDao().update(currentDayPlan);

                    mDialogDay.cancel();

                    Intent intent = new Intent(NewDayPlanActivity.this, PlanActivity.class);
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
        });

        mButtonBack.setOnClickListener(v -> mDialogDay.cancel());

        mDialogDay.show();
        Window window = mDialogDay.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
    private int parseMinutes(String time) {
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        return hour * 60 + minute;
    }
}