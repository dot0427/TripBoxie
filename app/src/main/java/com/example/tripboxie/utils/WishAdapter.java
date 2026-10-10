package com.example.tripboxie.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripboxie.R;

import java.util.List;

public class WishAdapter extends RecyclerView.Adapter<WishAdapter.WishViewHolder>{

    private List<Wish> wishList;
    private OnItemClickListener listener;

    public interface OnItemClickListener{
        void onItemClick(Wish wish);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
    public WishAdapter(List<Wish> wishList) {
        this.wishList = wishList;
    }

    @NonNull
    @Override
    public WishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wish, parent, false);
        return new WishViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishViewHolder holder, int position) {
        Wish wish = wishList.get(position);
        holder.mNameText.setText(wish.getName());
        holder.mAddressText.setText(wish.getAddress());
        holder.mCountryText.setText(wish.getCountry());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(wish);
            }
        });
    }

    @Override
    public int getItemCount() {
        return wishList.size();
    }

    public static class WishViewHolder extends RecyclerView.ViewHolder {
        TextView mNameText, mAddressText, mCountryText;

        public WishViewHolder(@NonNull View itemView) {
            super(itemView);
            mNameText = itemView.findViewById(R.id.text_name);
            mAddressText = itemView.findViewById(R.id.text_address);
            mCountryText = itemView.findViewById(R.id.text_country);
        }
    }
}

