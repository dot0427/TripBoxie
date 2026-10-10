package com.example.tripboxie.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripboxie.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.PlanViewHolder> {

    private List<Plan> planList;
    private OnItemClickListener listener;
    SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
    SimpleDateFormat noYearFormat = new SimpleDateFormat("MM/dd", Locale.US);

    public interface OnItemClickListener{
        void onItemClick(Plan plan);
    }
    public void setOnItemClickListener(PlanAdapter.OnItemClickListener listener){
        this.listener = listener;
    }
    public PlanAdapter(List<Plan> planList) {
        this.planList = planList;
    }

    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plan, parent, false);
        return new PlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        Plan plan = planList.get(position);
        holder.mTitleText.setText(plan.getLocation());

        try {
            Date startDate = fullFormat.parse(plan.getStartDate());
            Date endDate = fullFormat.parse(plan.getEndDate());

            String startStr = fullFormat.format(startDate);
            String endStr = noYearFormat.format(endDate);

            holder.mStartDateText.setText(startStr);
            holder.mEndDateText.setText(endStr);

        } catch (Exception e) {
            holder.mStartDateText.setText(plan.getStartDate());
            holder.mEndDateText.setText(plan.getEndDate());
            e.printStackTrace();
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(plan);
            }
        });
    }

    @Override
    public int getItemCount() {
        return planList.size();
    }

    public static class PlanViewHolder extends RecyclerView.ViewHolder {
        TextView mTitleText, mStartDateText, mEndDateText;

        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            mTitleText = itemView.findViewById(R.id.text_title);
            mStartDateText = itemView.findViewById(R.id.text_startDate);
            mEndDateText = itemView.findViewById(R.id.text_endDate);
        }
    }
}

