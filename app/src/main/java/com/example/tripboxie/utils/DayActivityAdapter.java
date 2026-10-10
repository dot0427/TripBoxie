package com.example.tripboxie.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tripboxie.R;
import java.util.List;

public class DayActivityAdapter extends RecyclerView.Adapter<DayActivityAdapter.DayActivityViewHolder> {
    private List<DayActivity> dayActivityList;
    private OnItemClickListener listener;

    public interface OnItemClickListener{
        void OnItemClick(DayActivity dayActivity);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
    public DayActivityAdapter(List<DayActivity> dayActivityList) {
        this. dayActivityList= dayActivityList;
    }

    @NonNull
    @Override
    public DayActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day_activity, parent, false);
        return new DayActivityViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull DayActivityViewHolder holder, int position) {
        DayActivity dayActivity = dayActivityList.get(position);
        holder.mStartTimeText.setText(dayActivity.getStartTime());
        holder.mEndTimeText.setText(dayActivity.getEndTime());
        holder.mLocationText.setText(dayActivity.getName());
        holder.mAddressText.setText(dayActivity.getAddress());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.OnItemClick(dayActivity);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dayActivityList.size();
    }

    public static class DayActivityViewHolder extends RecyclerView.ViewHolder {
        TextView mStartTimeText, mEndTimeText, mLocationText, mAddressText;

        public DayActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            mStartTimeText = itemView.findViewById(R.id.textView_startTime);
            mEndTimeText = itemView.findViewById(R.id.textView_endTime);
            mLocationText = itemView.findViewById(R.id.textView_day_location);
            mAddressText = itemView.findViewById(R.id.textView_day_address);
        }
    }
}
