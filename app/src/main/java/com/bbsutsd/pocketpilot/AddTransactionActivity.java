package com.bbsutsd.pocketpilot;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bbsutsd.pocketpilot.database.DatabaseHelper;
import com.bbsutsd.pocketpilot.model.Transaction;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {
    public static final String EXTRA_TRANSACTION_ID = "EXTRA_TRANSACTION_ID";

    private TextView txtAddTitle;
    private EditText editAmount, editDate, editDescription;
    private Spinner spinnerType, spinnerCategory;
    private Button btnSave, btnDelete;
    private DatabaseHelper dbHelper;

    private boolean isEditMode = false;
    private int editingTransactionId = -1;
    private final Calendar calendar = Calendar.getInstance();

    private static final String[] CATEGORIES = new String[]{
        "🍔 Food", "🚌 Transport", "📚 Education", "🛍️ Shopping", "📦 Other"
    };
    private static final String[] TYPES = new String[]{"Expense", "Income"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        dbHelper = new DatabaseHelper(this);

        txtAddTitle = findViewById(R.id.txtAddTitle);
        editAmount = findViewById(R.id.editAmount);
        editDate = findViewById(R.id.editDate);
        editDescription = findViewById(R.id.editDescription);
        spinnerType = findViewById(R.id.spinnerType);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, TYPES);
        spinnerType.setAdapter(typeAdapter);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, CATEGORIES);
        spinnerCategory.setAdapter(categoryAdapter);

        // Setup DatePickerDialog on date field tap
        editDate.setOnClickListener(v -> showDatePicker());

        // Check if editing existing transaction
        if (getIntent().hasExtra(EXTRA_TRANSACTION_ID)) {
            editingTransactionId = getIntent().getIntExtra(EXTRA_TRANSACTION_ID, -1);
            if (editingTransactionId != -1) {
                isEditMode = true;
                setupEditMode();
            }
        }

        if (!isEditMode) {
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            editDate.setText(currentDate);
        }

        btnSave.setOnClickListener(v -> saveOrUpdateTransaction());
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void showDatePicker() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, selectedYear, selectedMonth, selectedDay) -> {
                calendar.set(Calendar.YEAR, selectedYear);
                calendar.set(Calendar.MONTH, selectedMonth);
                calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
                editDate.setText(formattedDate);
            },
            year, month, day
        );
        datePickerDialog.show();
    }

    private void setupEditMode() {
        txtAddTitle.setText("Update Transaction");
        btnSave.setText("Update Transaction");
        btnDelete.setVisibility(View.VISIBLE);

        Transaction tx = dbHelper.getTransactionById(editingTransactionId);
        if (tx != null) {
            editAmount.setText(String.valueOf(tx.getAmount()));
            editDate.setText(tx.getDate());
            editDescription.setText(tx.getDescription());

            // Select type
            for (int i = 0; i < TYPES.length; i++) {
                if (TYPES[i].equalsIgnoreCase(tx.getType())) {
                    spinnerType.setSelection(i);
                    break;
                }
            }

            // Select category (match raw category or category with emoji)
            for (int i = 0; i < CATEGORIES.length; i++) {
                if (CATEGORIES[i].contains(tx.getCategory()) || tx.getCategory().contains(CATEGORIES[i])) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        }
    }

    private void saveOrUpdateTransaction() {
        String amountStr = editAmount.getText().toString().trim();
        String dateStr = editDate.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        String type = spinnerType.getSelectedItem().toString();
        String selectedCategoryWithEmoji = spinnerCategory.getSelectedItem().toString();

        // Extract clean category name (e.g. "Food" from "🍔 Food")
        String category = extractCleanCategory(selectedCategoryWithEmoji);

        if (amountStr.isEmpty()) {
            editAmount.setError("Amount is required");
            editAmount.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            editAmount.setError("Enter a valid numerical amount");
            editAmount.requestFocus();
            return;
        }

        if (amount <= 0) {
            editAmount.setError("Amount must be greater than 0");
            editAmount.requestFocus();
            return;
        }

        if (dateStr.isEmpty()) {
            dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        }

        if (isEditMode) {
            boolean success = dbHelper.updateTransaction(editingTransactionId, type, amount, category, dateStr, description);
            if (success) {
                Toast.makeText(this, "Transaction Updated Successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error Updating Transaction", Toast.LENGTH_SHORT).show();
            }
        } else {
            boolean success = dbHelper.addTransaction(type, amount, category, dateStr, description);
            if (success) {
                Toast.makeText(this, "Transaction Saved Successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error Saving Transaction", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String extractCleanCategory(String categoryWithEmoji) {
        if (categoryWithEmoji.contains("Food")) return "Food";
        if (categoryWithEmoji.contains("Transport")) return "Transport";
        if (categoryWithEmoji.contains("Education")) return "Education";
        if (categoryWithEmoji.contains("Shopping")) return "Shopping";
        if (categoryWithEmoji.contains("Other")) return "Other";
        return categoryWithEmoji;
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean deleted = dbHelper.deleteTransaction(editingTransactionId);
                    if (deleted) {
                        Toast.makeText(AddTransactionActivity.this, "Transaction Deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AddTransactionActivity.this, "Failed to delete transaction", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
