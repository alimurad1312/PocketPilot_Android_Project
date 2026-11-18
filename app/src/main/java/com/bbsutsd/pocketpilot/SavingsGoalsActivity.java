package com.bbsutsd.pocketpilot;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bbsutsd.pocketpilot.adapter.SavingsGoalAdapter;
import com.bbsutsd.pocketpilot.database.DatabaseHelper;
import com.bbsutsd.pocketpilot.model.SavingsGoal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SavingsGoalsActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private ListView listViewGoals;
    private TextView txtNoGoals;
    private Button btnAddNewGoal;
    private SavingsGoalAdapter adapter;
    private List<SavingsGoal> goalList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savings_goals);

        dbHelper = new DatabaseHelper(this);

        listViewGoals = findViewById(R.id.listViewGoals);
        txtNoGoals = findViewById(R.id.txtNoGoals);
        btnAddNewGoal = findViewById(R.id.btnAddNewGoal);

        btnAddNewGoal.setOnClickListener(v -> showAddGoalDialog());

        loadGoals();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGoals();
    }

    private void loadGoals() {
        goalList = dbHelper.getAllSavingsGoals();
        if (goalList.isEmpty()) {
            txtNoGoals.setVisibility(View.VISIBLE);
            listViewGoals.setVisibility(View.GONE);
        } else {
            txtNoGoals.setVisibility(View.GONE);
            listViewGoals.setVisibility(View.VISIBLE);
            adapter = new SavingsGoalAdapter(this, goalList, new SavingsGoalAdapter.OnGoalActionListener() {
                @Override
                public void onAddDeposit(SavingsGoal goal) {
                    showDepositDialog(goal);
                }

                @Override
                public void onDeleteGoal(SavingsGoal goal) {
                    confirmDeleteGoal(goal);
                }
            });
            listViewGoals.setAdapter(adapter);
        }
    }

    private void showAddGoalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_goal, null);
        builder.setView(dialogView);

        EditText editGoalName = dialogView.findViewById(R.id.dialogEditGoalName);
        EditText editGoalTarget = dialogView.findViewById(R.id.dialogEditGoalTarget);
        EditText editGoalInitial = dialogView.findViewById(R.id.dialogEditGoalInitial);
        EditText editGoalDate = dialogView.findViewById(R.id.dialogEditGoalDate);

        String defaultDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        editGoalDate.setText(defaultDate);

        final Calendar calendar = Calendar.getInstance();
        editGoalDate.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                SavingsGoalsActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(Calendar.YEAR, selectedYear);
                    calendar.set(Calendar.MONTH, selectedMonth);
                    calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                    String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
                    editGoalDate.setText(formattedDate);
                },
                year, month, day
            );
            datePickerDialog.show();
        });

        builder.setPositiveButton("Create Goal", (dialog, which) -> {
            String name = editGoalName.getText().toString().trim();
            String targetStr = editGoalTarget.getText().toString().trim();
            String initialStr = editGoalInitial.getText().toString().trim();
            String targetDate = editGoalDate.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(SavingsGoalsActivity.this, "Goal name is required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (targetStr.isEmpty()) {
                Toast.makeText(SavingsGoalsActivity.this, "Target amount is required", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double target = Double.parseDouble(targetStr);
                double initial = initialStr.isEmpty() ? 0.0 : Double.parseDouble(initialStr);

                if (target <= 0) {
                    Toast.makeText(SavingsGoalsActivity.this, "Target amount must be > 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean success = dbHelper.addSavingsGoal(name, target, initial, targetDate);
                if (success) {
                    Toast.makeText(SavingsGoalsActivity.this, "Savings Goal Created!", Toast.LENGTH_SHORT).show();
                    loadGoals();
                } else {
                    Toast.makeText(SavingsGoalsActivity.this, "Failed to create goal", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(SavingsGoalsActivity.this, "Invalid number entered", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDepositDialog(SavingsGoal goal) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_deposit_goal, null);
        builder.setView(dialogView);

        TextView txtTitle = dialogView.findViewById(R.id.dialogTxtDepositTitle);
        txtTitle.setText("Add Savings: " + goal.getGoalName());

        EditText editDeposit = dialogView.findViewById(R.id.dialogEditDepositAmount);

        builder.setPositiveButton("Add Savings", (dialog, which) -> {
            String depositStr = editDeposit.getText().toString().trim();
            if (depositStr.isEmpty()) {
                Toast.makeText(SavingsGoalsActivity.this, "Amount is required", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double deposit = Double.parseDouble(depositStr);
                if (deposit <= 0) {
                    Toast.makeText(SavingsGoalsActivity.this, "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean success = dbHelper.addSavingsGoalDeposit(goal.getId(), deposit);
                if (success) {
                    Toast.makeText(SavingsGoalsActivity.this, "Savings updated! Keep it up! 🎉", Toast.LENGTH_SHORT).show();
                    loadGoals();
                } else {
                    Toast.makeText(SavingsGoalsActivity.this, "Failed to update savings", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(SavingsGoalsActivity.this, "Invalid amount", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void confirmDeleteGoal(SavingsGoal goal) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Savings Goal")
                .setMessage("Are you sure you want to delete '" + goal.getGoalName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean success = dbHelper.deleteSavingsGoal(goal.getId());
                    if (success) {
                        Toast.makeText(SavingsGoalsActivity.this, "Goal deleted", Toast.LENGTH_SHORT).show();
                        loadGoals();
                    } else {
                        Toast.makeText(SavingsGoalsActivity.this, "Failed to delete goal", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
