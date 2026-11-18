package com.bbsutsd.pocketpilot.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.bbsutsd.pocketpilot.R;
import com.bbsutsd.pocketpilot.model.SavingsGoal;
import java.util.List;
import java.util.Locale;

public class SavingsGoalAdapter extends ArrayAdapter<SavingsGoal> {

    public interface OnGoalActionListener {
        void onAddDeposit(SavingsGoal goal);
        void onDeleteGoal(SavingsGoal goal);
    }

    private final Context context;
    private final List<SavingsGoal> goals;
    private final OnGoalActionListener listener;

    public SavingsGoalAdapter(Context context, List<SavingsGoal> goals, OnGoalActionListener listener) {
        super(context, R.layout.item_savings_goal, goals);
        this.context = context;
        this.goals = goals;
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_savings_goal, parent, false);
            holder = new ViewHolder();
            holder.txtGoalName = convertView.findViewById(R.id.txtGoalName);
            holder.txtTargetDate = convertView.findViewById(R.id.txtGoalTargetDate);
            holder.txtProgress = convertView.findViewById(R.id.txtGoalProgress);
            holder.txtPercentage = convertView.findViewById(R.id.txtGoalPercentage);
            holder.progressBar = convertView.findViewById(R.id.progressBarGoal);
            holder.btnAddDeposit = convertView.findViewById(R.id.btnAddSavingsDeposit);
            holder.btnDelete = convertView.findViewById(R.id.btnDeleteGoal);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SavingsGoal goal = goals.get(position);
        holder.txtGoalName.setText("🎯 " + goal.getGoalName());
        holder.txtTargetDate.setText(String.format("Target: %s", goal.getTargetDate()));

        holder.txtProgress.setText(String.format(Locale.getDefault(), "Saved: Rs. %.0f / Rs. %.0f", goal.getCurrentAmount(), goal.getTargetAmount()));

        int pct = goal.getProgressPercentage();
        holder.progressBar.setProgress(pct);

        if (goal.isCompleted()) {
            holder.txtPercentage.setTextColor(Color.parseColor("#059669"));
            holder.txtPercentage.setText(String.format(Locale.getDefault(), "%d%% (🎉 Completed!)", pct));
            holder.progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#059669")));
        } else {
            holder.txtPercentage.setTextColor(Color.parseColor("#2563EB"));
            holder.txtPercentage.setText(String.format(Locale.getDefault(), "%d%%", pct));
            holder.progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#2563EB")));
        }

        holder.btnAddDeposit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddDeposit(goal);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteGoal(goal);
            }
        });

        return convertView;
    }

    private static class ViewHolder {
        TextView txtGoalName;
        TextView txtTargetDate;
        TextView txtProgress;
        TextView txtPercentage;
        ProgressBar progressBar;
        Button btnAddDeposit;
        Button btnDelete;
    }
}
