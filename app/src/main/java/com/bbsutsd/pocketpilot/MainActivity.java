package com.bbsutsd.pocketpilot;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bbsutsd.pocketpilot.adapter.TransactionAdapter;
import com.bbsutsd.pocketpilot.database.DatabaseHelper;
import com.bbsutsd.pocketpilot.model.Transaction;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private TextView txtBalance, txtIncome, txtExpense, txtNoTransactions;
    private ListView listViewTransactions;
    private Button btnAddTransaction, btnBudgets, btnSavingsGoals, btnExportCsv, btnAboutProject;
    private Spinner spinnerFilterType, spinnerFilterCategory;

    private static final String[] FILTER_TYPES = new String[]{"All Types", "Income", "Expense"};
    private static final String[] FILTER_CATEGORIES = new String[]{
        "All Categories", "🍔 Food", "🚌 Transport", "📚 Education", "🛍️ Shopping", "📦 Other"
    };

    private final ActivityResultLauncher<String> createCsvLauncher =
        registerForActivityResult(new ActivityResultContracts.CreateDocument("text/csv"), uri -> {
            if (uri != null) {
                writeCsvToUri(uri);
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        txtBalance = findViewById(R.id.txtBalance);
        txtIncome = findViewById(R.id.txtIncome);
        txtExpense = findViewById(R.id.txtExpense);
        txtNoTransactions = findViewById(R.id.txtNoTransactions);
        listViewTransactions = findViewById(R.id.listViewTransactions);

        btnAddTransaction = findViewById(R.id.btnAddTransaction);
        btnBudgets = findViewById(R.id.btnBudgets);
        btnSavingsGoals = findViewById(R.id.btnSavingsGoals);
        btnExportCsv = findViewById(R.id.btnExportCsv);
        btnAboutProject = findViewById(R.id.btnAboutProject);

        spinnerFilterType = findViewById(R.id.spinnerFilterType);
        spinnerFilterCategory = findViewById(R.id.spinnerFilterCategory);

        // Setup filter spinners
        ArrayAdapter<String> typeFilterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, FILTER_TYPES);
        spinnerFilterType.setAdapter(typeFilterAdapter);

        ArrayAdapter<String> categoryFilterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, FILTER_CATEGORIES);
        spinnerFilterCategory.setAdapter(categoryFilterAdapter);

        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadTransactions();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        spinnerFilterType.setOnItemSelectedListener(filterListener);
        spinnerFilterCategory.setOnItemSelectedListener(filterListener);

        // Navigation actions
        btnAddTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
            startActivity(intent);
        });

        btnBudgets.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BudgetsActivity.class);
            startActivity(intent);
        });

        btnSavingsGoals.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SavingsGoalsActivity.class);
            startActivity(intent);
        });

        btnExportCsv.setOnClickListener(v -> {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date());
            createCsvLauncher.launch("PocketPilot_Transactions_" + timestamp + ".csv");
        });

        btnAboutProject.setOnClickListener(v -> showAboutProjectDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
        loadTransactions();
    }

    private void showAboutProjectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_about_project, null);
        builder.setView(dialogView);
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    private void loadDashboardData() {
        double income = dbHelper.getTotalIncome();
        double expense = dbHelper.getTotalExpense();
        double balance = income - expense;

        txtIncome.setText(String.format(Locale.getDefault(), "+ Rs. %.2f", income));
        txtExpense.setText(String.format(Locale.getDefault(), "- Rs. %.2f", expense));
        txtBalance.setText(String.format(Locale.getDefault(), "Rs. %.2f", balance));
    }

    private void loadTransactions() {
        String selectedType = spinnerFilterType.getSelectedItem() != null ? spinnerFilterType.getSelectedItem().toString() : "All Types";
        String selectedCategory = spinnerFilterCategory.getSelectedItem() != null ? spinnerFilterCategory.getSelectedItem().toString() : "All Categories";

        String typeFilter = selectedType.equals("All Types") ? "All" : selectedType;
        String categoryFilter = selectedCategory.equals("All Categories") ? "All" : extractCleanCategory(selectedCategory);

        List<Transaction> transactions = dbHelper.getTransactionsList(typeFilter, categoryFilter);

        if (transactions.isEmpty()) {
            txtNoTransactions.setVisibility(View.VISIBLE);
            listViewTransactions.setVisibility(View.GONE);
        } else {
            txtNoTransactions.setVisibility(View.GONE);
            listViewTransactions.setVisibility(View.VISIBLE);

            TransactionAdapter adapter = new TransactionAdapter(this, transactions, new TransactionAdapter.OnTransactionActionListener() {
                @Override
                public void onEdit(Transaction transaction) {
                    Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
                    intent.putExtra(AddTransactionActivity.EXTRA_TRANSACTION_ID, transaction.getId());
                    startActivity(intent);
                }

                @Override
                public void onDelete(Transaction transaction) {
                    confirmDeleteTransaction(transaction);
                }
            });
            listViewTransactions.setAdapter(adapter);
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

    private void confirmDeleteTransaction(Transaction transaction) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Transaction")
                .setMessage("Delete transaction of Rs. " + transaction.getAmount() + " (" + transaction.getCategory() + ")?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean success = dbHelper.deleteTransaction(transaction.getId());
                    if (success) {
                        Toast.makeText(MainActivity.this, "Transaction Deleted", Toast.LENGTH_SHORT).show();
                        loadDashboardData();
                        loadTransactions();
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to delete transaction", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void writeCsvToUri(Uri uri) {
        try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
            if (outputStream != null) {
                String csvContent = dbHelper.generateTransactionsCsv();
                outputStream.write(csvContent.getBytes(StandardCharsets.UTF_8));
                Toast.makeText(this, "CSV exported successfully!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
