package com.example.tripboxie;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
import com.example.tripboxie.utils.GeocodingHelper;
import com.example.tripboxie.utils.Wish;
import com.example.tripboxie.utils.WishAdapter;

import java.util.List;

public class WishActivity extends AppCompatActivity {

    RecyclerView mRecyclerViewWish;
    WishAdapter mwishAdapter;
    Button mButtonNew, mButtonBack;
    AppDatabase db;
    Dialog mDialogEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wish);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mButtonNew = findViewById(R.id.button_new);
        mButtonBack = findViewById(R.id.button_add_back);

        db = AppDatabase.getInstance(getApplicationContext());

        mRecyclerViewWish = findViewById(R.id.recyclerView_wish);
        mRecyclerViewWish.setLayoutManager(new LinearLayoutManager(WishActivity.this));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.recycler_item_spacing);
        mRecyclerViewWish.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.bottom = spacingInPixels;  // 每個item底部加間距
            }
        });

        loadWishList();

        mButtonNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WishActivity.this, NewActivity.class);
                startActivity(intent);
            }
        });

        mButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadWishList();
    }

    private void loadWishList() {
        List<Wish> wishList = db.wishDao().getAll();
        mwishAdapter = new WishAdapter(wishList);

        mwishAdapter.setOnItemClickListener(new WishAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Wish wish) {
                int id = wish.getId();
                mDialogEdit = new Dialog(WishActivity.this);
                mDialogEdit.setCancelable(false);
                mDialogEdit.setContentView(R.layout.edit_add_dlg);

                EditText mEditTextName = mDialogEdit.findViewById(R.id.editTextText_name_edit);
                EditText mEditTextAddress = mDialogEdit.findViewById(R.id.editTextText_address_edit);
                EditText mEditTextCountry = mDialogEdit.findViewById(R.id.editTextText_country_edit);
                EditText mEditTextUri = mDialogEdit.findViewById(R.id.editTextText_uri_edit);
                Button mButtonEdit = mDialogEdit.findViewById(R.id.button_edit);
                Button mButtonDel = mDialogEdit.findViewById(R.id.button_del);
                Button mButtonMore = mDialogEdit.findViewById(R.id.button_more);
                Button mButtonBack = mDialogEdit.findViewById(R.id.button_back);

                Wish wishClick = db.wishDao().getWishById(id);
                mEditTextName.setText(wishClick.getName());
                mEditTextAddress.setText(wishClick.getAddress());
                mEditTextCountry.setText(wishClick.getCountry());
                mEditTextUri.setText(wishClick.getUri());

                mButtonEdit.setOnClickListener(new View.OnClickListener() {
                    double mLatitude, mLongitude;
                    @Override
                    public void onClick(View v) {
                        GeocodingHelper.getCoordinatesFromAddress(mEditTextAddress.getText().toString(), new GeocodingHelper.OnGeocodingResult() {
                            @Override
                            public void onSuccess(double lat, double lng) {
                                mLatitude = lat;
                                mLongitude = lng;
                            }

                            @Override
                            public void onError(String message) {
                                CustomToast.show(WishActivity.this, "獲取經緯度錯誤");
                            }
                        });

                        wishClick.setAll(mEditTextName.getText().toString(), mEditTextAddress.getText().toString()
                                , mEditTextCountry.getText().toString(), mEditTextUri.getText().toString(), mLatitude, mLongitude);

                        db.wishDao().update(wishClick);
                        loadWishList();
                        mDialogEdit.cancel();
                    }
                });

                mButtonDel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        db.wishDao().deleteById(id);
                        loadWishList();
                        mDialogEdit.cancel();
                    }
                });

                mButtonMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String uri = wishClick.getUri();
                        if (!uri.isEmpty()) {
                            Intent intent = new Intent(WishActivity.this, WebActivity.class);
                            intent.putExtra("S", uri);
                            startActivity(intent);
                            mDialogEdit.cancel();
                        } else {
                            CustomToast.show(WishActivity.this, "沒有詳情網址");
                        }
                    }
                });

                mButtonBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialogEdit.cancel();
                    }
                });

                mDialogEdit.show();
            }
        });

        mRecyclerViewWish.setAdapter(mwishAdapter);
    }
}