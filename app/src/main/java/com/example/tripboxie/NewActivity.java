package com.example.tripboxie;

import android.app.Dialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.view.View;

// Google Maps 相關
import com.example.tripboxie.utils.AppDatabase;
import com.example.tripboxie.utils.CustomToast;
import com.example.tripboxie.utils.GeocodingHelper;
import com.example.tripboxie.utils.Wish;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

public class NewActivity extends AppCompatActivity implements OnMapReadyCallback {

    Button mButtonNewAdd, mButtonNewGo, mButtonNewBack;
    Dialog mDialogNew;
    EditText mEditTextKey;
    String  newName = "", newAddress = "", newCountry = "", newUri = "";
    double newLatitude, newLongitude;
    private com.google.android.gms.maps.GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync((OnMapReadyCallback) this);

        mEditTextKey = findViewById(R.id.editTextText_key);
        mButtonNewGo = findViewById(R.id.button_new_go);
        mButtonNewAdd = findViewById(R.id.button_new_add);
        mButtonNewBack = findViewById(R.id.button_new_back);

        mButtonNewGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newName = mEditTextKey.getText().toString();
                if (!newName.isEmpty()) {
                    goCodeAddress(newName); // 這裡會自動查座標＋加進資料庫
                } else {
                    CustomToast.show(NewActivity.this, "請輸入地點關鍵字！");
                }
            }
        });

        mButtonNewAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newName = "";
                newAddress = "";
                newCountry = "";
                newUri = "";
                newLatitude = 0;
                newLongitude = 0;

                showAddDialog();
            }
        });

        mButtonNewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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

                newLatitude = lat;
                newLongitude = lng;
                newName = keyword;
                newCountry = "";

                // 反查地址
                Geocoder geocoder = new Geocoder(NewActivity.this, Locale.getDefault());
                try {
                    List<Address> addressList = geocoder.getFromLocation(lat, lng, 1);
                    if (!addressList.isEmpty()) {
                        Address addr = addressList.get(0);
                        newAddress = addr.getAddressLine(0);
                        newCountry = addr.getCountryName();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // 自動帶入 dialog
                showAddDialog();
            }

            @Override
            public void onError(String message) {
                CustomToast.show(NewActivity.this, "搜尋失敗：" + message);
            }
        });
    }
    private void showAddDialog() {
        mDialogNew = new Dialog(NewActivity.this);
        mDialogNew.setCancelable(false);
        mDialogNew.setContentView(R.layout.new_add_dlg);

        EditText mEditTextName = mDialogNew.findViewById(R.id.editTextText_name);
        EditText mEditTextAddress = mDialogNew.findViewById(R.id.editTextText_address_new);
        EditText mEditTextCountry = mDialogNew.findViewById(R.id.editTextText_country);
        EditText mEditTextUri = mDialogNew.findViewById(R.id.editTextText_uri);
        Button mButtonConfirm = mDialogNew.findViewById(R.id.button_confirm);
        Button mButtonBack = mDialogNew.findViewById(R.id.button_back);

        mEditTextName.setText(newName); // 自動填入關鍵字
        mEditTextAddress.setText(newAddress);
        mEditTextCountry.setText(newCountry);

        mButtonConfirm.setOnClickListener(v -> {
            newName = mEditTextName.getText().toString();
            newAddress = mEditTextAddress.getText().toString();
            newCountry = mEditTextCountry.getText().toString();
            newUri = mEditTextUri.getText().toString();

            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            db.wishDao().insert(new Wish(newName, newAddress, newCountry, newUri, newLatitude, newLongitude));

            mDialogNew.cancel();
            finish();
        });

        mButtonBack.setOnClickListener(v -> mDialogNew.cancel());

        mDialogNew.show();
        Window window = mDialogNew.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}