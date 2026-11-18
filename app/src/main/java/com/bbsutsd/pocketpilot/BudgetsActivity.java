package com.bbsutsd.pocketpilot;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bbsutsd.pocketpilot.adapter.BudgetAdapter;
import com.bbsutsd.pocketpilot.database.DatabaseHelper;
import com.bbsutsd.pocketpilot.model.Budget;
import java.util.List;

public class BudgetsActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private ListView listViewBudgets;
    private TextView txtNoBudgets;
    private Button btnSetNewBudget;
    private BudgetAdapter adapter;
    private List<Budget> budgetList;

    private static final String[] CATEGORIES = new String[]{
        "🍔 Food", "🚌 Transport", "📚 Education", "🛍️ Shopping", "📦 Other"
    };
    private static final String[] PERIODS = new String[]{"Monthly", "Weekly", "Semester"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budgets);

        dbHelper = new DatabaseHelper(this);

        listViewBudgets = findViewById(R.id.listViewBudgets);
        txtNoBudgets = findViewById(R.id.txtNoBudgets);
        btnSetNewBudget = findViewById(R.id.btnSetNewBudget);

        btnSetNewBudget.setOnClickListener(v -> showSetBudgetDialog(null));

        loadBudgets();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBudgets();
    }

    private void loadBudgets() {
        budgetList = dbHelper.getAllBudgetsWithSpending();
        if (budgetList.isEmpty()) {
            txtNoBudgets.setVisibility(View.VISIBLE);
            listViewBudgets.setVisibility(View.GONE);
        } else {
            txtNoBudgets.setVisibility(View.GONE);
            listViewBudgets.setVisibility(View.VISIBLE);
            adapter = new BudgetAdapter(this, budgetList, new BudgetAdapter.OnBudgetActionListener() {
                @Override
                public void onUpdateLimit(Budget budget) {
                    showSetBudgetDialog(budget);
                }

                @Override
                public void onDeleteBudget(Budget budget) {
                    confirmDeleteBudget(budget);
                }
            });
            listViewBudgets.setAdapter(adapter);
        }
    }

    private void showSetBudgetDialog(Budget existingBudget) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_set_budget, null);
        builder.setView(dialogView);

        Spinner spinnerCategory = dialogView.findViewById(R.id.dialogSpinnerCategory);
        Spinner spinnerPeriod = dialogView.findViewById(R.id.dialogSpinnerPeriod);
        EditText editLimit = dialogView.findViewById(R.id.dialogEditBudgetLimit);

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, CATEGORIES);
        spinnerCategory.setAdapter(catAdapter);

        ArrayAdapter<String> periodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, PERIODS);
        spinnerPeriod.setAdapter(periodAdapter);

        if (existingBudget != null) {
            editLimit.setText(String.valueOf(existingBudget.getLimitAmount()));
            for (int i = 0; i < CATEGORIES.length; i++) {
                if (CATEGORIES[i].contains(existingBudget.getCategory()) || existingBudget.getCategory().contains(CATEGORIES[i])) {
                    spinnerCategory.setSelection(i);
                    spinnerCategory.setEnabled(false); // keep category fixed on edit
                    break;
                }
            }
            for (int i = 0; i < PERIODS.length; i++) {
                if (PERIODS[i].equalsIgnoreCase(existingBudget.getPeriod())) {
                    spinnerPeriod.setSelection(i);
                    break;
                }
            }
        }

        builder.setPositiveButton("Save Limit", (dialog, which) -> {
            String selectedCategoryWithEmoji = spinnerCategory.getSelectedItem().toString();
            String period = spinnerPeriod.getSelectedItem().toString();
            String limitStr = editLimit.getText().toString().trim();

            String category = extractCleanCategory(selectedCategoryWithEmoji);

            if (limitStr.isEmpty()) {
                Toast.makeText(BudgetsActivity.this, "Limit amount is required", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double limit = Double.parseDouble(limitStr);
                if (limit <= 0) {
                    Toast.makeText(BudgetsActivity.this, "Limit must be greater than 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean success = dbHelper.setOrUpdateBudget(category, period, limit);
                if (success) {
                    Toast.makeText(BudgetsActivity.this, "Budget saved for " + category, Toast.LENGTH_SHORT).show();
                    loadBudgets();
                } else {
                    Toast.makeText(BudgetsActivity.this, "Failed to save budget", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(BudgetsActivity.this, "Invalid number entered", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private String extractCleanCategory(String categoryWithEmoji) {
        if (categoryWithEmoji.contains("Food")) return "Food";
        if (categoryWithEmoji.contains("Transport")) return "Transport";
        if (categoryWithEmoji.contains("Education")) return "Education";
        if (categoryWithEmoji.contains("Shopping")) return "Shopping";
        if (categoryWithEmoji.contains("Other")) return "Other";
        return categoryWithEmoji;
    }

    private void confirmDeleteBudget(Budget budget) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Budget")
                .setMessage("Remove budget limit for " + budget.getCategory() + "?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    boolean success = dbHelper.deleteBudget(budget.getId());
                    if (success) {
                        Toast.makeText(BudgetsActivity.this, "Budget removed", Toast.LENGTH_SHORT).show();
                        loadBudgets();
                    } else {
                        Toast.makeText(BudgetsActivity.this, "Failed to remove budget", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
