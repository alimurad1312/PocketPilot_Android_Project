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
import com.bbsutsd.pocketpilot.model.Budget;
import java.util.List;
import java.util.Locale;

public class BudgetAdapter extends ArrayAdapter<Budget> {

    public interface OnBudgetActionListener {
        void onUpdateLimit(Budget budget);
        void onDeleteBudget(Budget budget);
    }

    private final Context context;
    private final List<Budget> budgets;
    private final OnBudgetActionListener listener;

    public BudgetAdapter(Context context, List<Budget> budgets, OnBudgetActionListener listener) {
        super(context, R.layout.item_budget, budgets);
        this.context = context;
        this.budgets = budgets;
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_budget, parent, false);
            holder = new ViewHolder();
            holder.txtCategory = convertView.findViewById(R.id.txtBudgetCategory);
            holder.txtPeriod = convertView.findViewById(R.id.txtBudgetPeriod);
            holder.txtSpent = convertView.findViewById(R.id.txtBudgetSpent);
            holder.txtRemaining = convertView.findViewById(R.id.txtBudgetRemaining);
            holder.progressBar = convertView.findViewById(R.id.progressBarBudget);
            holder.btnEdit = convertView.findViewById(R.id.btnEditBudget);
            holder.btnDelete = convertView.findViewById(R.id.btnDeleteBudget);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Budget budget = budgets.get(position);
        holder.txtCategory.setText(getFormattedCategory(budget.getCategory()));
        holder.txtPeriod.setText(budget.getPeriod());

        holder.txtSpent.setText(String.format(Locale.getDefault(), "Spent: Rs. %.0f / Limit: Rs. %.0f", budget.getSpentAmount(), budget.getLimitAmount()));

        double remaining = budget.getRemainingAmount();
        int usagePct = budget.getUsagePercentage();
        holder.progressBar.setProgress(usagePct);

        // Professional color-coded budget status & progress tinting
        if (budget.isOverBudget()) {
            holder.txtRemaining.setText(String.format(Locale.getDefault(), "Over budget by Rs. %.0f!", Math.abs(remaining)));
            holder.txtRemaining.setTextColor(Color.parseColor("#DC2626"));
            holder.progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#DC2626")));
        } else if (usagePct >= 80) {
            holder.txtRemaining.setText(String.format(Locale.getDefault(), "Remaining: Rs. %.0f (Near Limit)", remaining));
            holder.txtRemaining.setTextColor(Color.parseColor("#D97706"));
            holder.progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#D97706")));
        } else {
            holder.txtRemaining.setText(String.format(Locale.getDefault(), "Remaining: Rs. %.0f", remaining));
            holder.txtRemaining.setTextColor(Color.parseColor("#059669"));
            holder.progressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#059669")));
        }

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUpdateLimit(budget);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteBudget(budget);
            }
        });

        return convertView;
    }

    private String getFormattedCategory(String category) {
        if (category == null) return "📦 Other";
        switch (category.toLowerCase(Locale.ROOT)) {
            case "food": return "🍔 Food";
            case "transport": return "🚌 Transport";
            case "education": return "📚 Education";
            case "shopping": return "🛍️ Shopping";
            case "other": return "📦 Other";
            default:
                if (category.startsWith("🍔") || category.startsWith("🚌") || category.startsWith("📚") || category.startsWith("🛍️") || category.startsWith("📦")) {
                    return category;
                }
                return "📦 " + category;
        }
    }

    private static class ViewHolder {
        TextView txtCategory;
        TextView txtPeriod;
        TextView txtSpent;
        TextView txtRemaining;
        ProgressBar progressBar;
        Button btnEdit;
        Button btnDelete;
    }
}
