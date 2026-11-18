package com.bbsutsd.pocketpilot.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import com.bbsutsd.pocketpilot.R;
import com.bbsutsd.pocketpilot.model.Transaction;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends ArrayAdapter<Transaction> {

    public interface OnTransactionActionListener {
        void onEdit(Transaction transaction);
        void onDelete(Transaction transaction);
    }

    private final Context context;
    private final List<Transaction> transactions;
    private final OnTransactionActionListener listener;

    public TransactionAdapter(Context context, List<Transaction> transactions, OnTransactionActionListener listener) {
        super(context, R.layout.item_transaction, transactions);
        this.context = context;
        this.transactions = transactions;
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
            holder = new ViewHolder();
            holder.txtIconAvatar = convertView.findViewById(R.id.txtItemIconAvatar);
            holder.txtCategory = convertView.findViewById(R.id.txtItemCategory);
            holder.txtTypeBadge = convertView.findViewById(R.id.txtItemTypeBadge);
            holder.txtDescription = convertView.findViewById(R.id.txtItemDescription);
            holder.txtDate = convertView.findViewById(R.id.txtItemDate);
            holder.txtAmount = convertView.findViewById(R.id.txtItemAmount);
            holder.btnEdit = convertView.findViewById(R.id.btnItemEdit);
            holder.btnDelete = convertView.findViewById(R.id.btnItemDelete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Transaction tx = transactions.get(position);
        String category = tx.getCategory() != null ? tx.getCategory() : "Other";

        holder.txtCategory.setText(category);
        holder.txtIconAvatar.setText(getCategoryEmoji(category));
        holder.txtIconAvatar.setBackgroundResource(getCategoryBgDrawable(category));
        holder.txtDate.setText(tx.getDate());

        if (tx.getDescription() != null && !tx.getDescription().trim().isEmpty()) {
            holder.txtDescription.setText(tx.getDescription());
            holder.txtDescription.setVisibility(View.VISIBLE);
        } else {
            holder.txtDescription.setVisibility(View.GONE);
        }

        boolean isIncome = "Income".equalsIgnoreCase(tx.getType());
        if (isIncome) {
            holder.txtTypeBadge.setText("Income");
            holder.txtTypeBadge.setTextColor(Color.parseColor("#059669"));
            holder.txtTypeBadge.setBackgroundResource(R.drawable.bg_income_badge);
            holder.txtAmount.setText(String.format(Locale.getDefault(), "+ Rs. %.2f", tx.getAmount()));
            holder.txtAmount.setTextColor(Color.parseColor("#059669"));
        } else {
            holder.txtTypeBadge.setText("Expense");
            holder.txtTypeBadge.setTextColor(Color.parseColor("#DC2626"));
            holder.txtTypeBadge.setBackgroundResource(R.drawable.bg_expense_badge);
            holder.txtAmount.setText(String.format(Locale.getDefault(), "- Rs. %.2f", tx.getAmount()));
            holder.txtAmount.setTextColor(Color.parseColor("#DC2626"));
        }

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(tx);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(tx);
            }
        });

        return convertView;
    }

    private String getCategoryEmoji(String category) {
        if (category == null) return "📦";
        switch (category.toLowerCase(Locale.ROOT)) {
            case "food": return "🍔";
            case "transport": return "🚌";
            case "education": return "📚";
            case "shopping": return "🛍️";
            default: return "📦";
        }
    }

    private int getCategoryBgDrawable(String category) {
        if (category == null) return R.drawable.bg_category_other;
        switch (category.toLowerCase(Locale.ROOT)) {
            case "food": return R.drawable.bg_category_food;
            case "transport": return R.drawable.bg_category_transport;
            case "education": return R.drawable.bg_category_education;
            case "shopping": return R.drawable.bg_category_shopping;
            default: return R.drawable.bg_category_other;
        }
    }

    private static class ViewHolder {
        TextView txtIconAvatar;
        TextView txtCategory;
        TextView txtTypeBadge;
        TextView txtDescription;
        TextView txtDate;
        TextView txtAmount;
        Button btnEdit;
        Button btnDelete;
    }
}
