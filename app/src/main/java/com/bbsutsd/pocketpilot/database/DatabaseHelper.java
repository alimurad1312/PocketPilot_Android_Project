package com.bbsutsd.pocketpilot.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.bbsutsd.pocketpilot.model.Budget;
import com.bbsutsd.pocketpilot.model.SavingsGoal;
import com.bbsutsd.pocketpilot.model.Transaction;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "pocket_pilot.db";
    private static final int DATABASE_VERSION = 2;

    // 1. Transactions Table
    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String COLUMN_ID = "transaction_id";
    public static final String COLUMN_TYPE = "type"; // "Income" or "Expense"
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_DESCRIPTION = "description";

    // 2. Budgets Table
    public static final String TABLE_BUDGETS = "budgets";
    public static final String COLUMN_BUDGET_ID = "budget_id";
    public static final String COLUMN_BUDGET_CATEGORY = "category";
    public static final String COLUMN_BUDGET_PERIOD = "period";
    public static final String COLUMN_BUDGET_LIMIT = "limit_amount";

    // 3. SavingsGoals Table
    public static final String TABLE_SAVINGS_GOALS = "savings_goals";
    public static final String COLUMN_GOAL_ID = "goal_id";
    public static final String COLUMN_GOAL_NAME = "goal_name";
    public static final String COLUMN_GOAL_TARGET = "target_amount";
    public static final String COLUMN_GOAL_CURRENT = "current_amount";
    public static final String COLUMN_GOAL_DATE = "target_date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Transactions Table
        String CREATE_TRANSACTIONS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_TRANSACTIONS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TYPE + " TEXT,"
                + COLUMN_AMOUNT + " REAL,"
                + COLUMN_CATEGORY + " TEXT,"
                + COLUMN_DATE + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT" + ")";
        db.execSQL(CREATE_TRANSACTIONS_TABLE);

        // Create Budgets Table
        String CREATE_BUDGETS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_BUDGETS + "("
                + COLUMN_BUDGET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_BUDGET_CATEGORY + " TEXT UNIQUE,"
                + COLUMN_BUDGET_PERIOD + " TEXT,"
                + COLUMN_BUDGET_LIMIT + " REAL" + ")";
        db.execSQL(CREATE_BUDGETS_TABLE);

        // Create SavingsGoals Table
        String CREATE_SAVINGS_GOALS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_SAVINGS_GOALS + "("
                + COLUMN_GOAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_GOAL_NAME + " TEXT,"
                + COLUMN_GOAL_TARGET + " REAL,"
                + COLUMN_GOAL_CURRENT + " REAL,"
                + COLUMN_GOAL_DATE + " TEXT" + ")";
        db.execSQL(CREATE_SAVINGS_GOALS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Upgrade without losing existing transaction data
        if (oldVersion < 2) {
            String CREATE_BUDGETS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_BUDGETS + "("
                    + COLUMN_BUDGET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_BUDGET_CATEGORY + " TEXT UNIQUE,"
                    + COLUMN_BUDGET_PERIOD + " TEXT,"
                    + COLUMN_BUDGET_LIMIT + " REAL" + ")";
            db.execSQL(CREATE_BUDGETS_TABLE);

            String CREATE_SAVINGS_GOALS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_SAVINGS_GOALS + "("
                    + COLUMN_GOAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_GOAL_NAME + " TEXT,"
                    + COLUMN_GOAL_TARGET + " REAL,"
                    + COLUMN_GOAL_CURRENT + " REAL,"
                    + COLUMN_GOAL_DATE + " TEXT" + ")";
            db.execSQL(CREATE_SAVINGS_GOALS_TABLE);
        }
    }

    // ==========================================
    // TRANSACTIONS OPERATIONS
    // ==========================================

    public boolean addTransaction(String type, double amount, String category, String date, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_DESCRIPTION, description);

        long result = db.insert(TABLE_TRANSACTIONS, null, values);
        return result != -1;
    }

    public boolean updateTransaction(int id, String type, double amount, String category, String date, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_DESCRIPTION, description);

        int rows = db.update(TABLE_TRANSACTIONS, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public boolean deleteTransaction(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_TRANSACTIONS, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public Cursor getAllTransactions() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_TRANSACTIONS + " ORDER BY " + COLUMN_ID + " DESC", null);
    }

    public List<Transaction> getTransactionsList(String typeFilter, String categoryFilter) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder query = new StringBuilder("SELECT * FROM " + TABLE_TRANSACTIONS + " WHERE 1=1");
        List<String> argsList = new ArrayList<>();

        if (typeFilter != null && !typeFilter.equalsIgnoreCase("All")) {
            query.append(" AND ").append(COLUMN_TYPE).append(" = ?");
            argsList.add(typeFilter);
        }

        if (categoryFilter != null && !categoryFilter.equalsIgnoreCase("All")) {
            query.append(" AND ").append(COLUMN_CATEGORY).append(" = ?");
            argsList.add(categoryFilter);
        }

        query.append(" ORDER BY ").append(COLUMN_ID).append(" DESC");

        String[] selectionArgs = argsList.isEmpty() ? null : argsList.toArray(new String[0]);
        Cursor cursor = db.rawQuery(query.toString(), selectionArgs);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE));
                    double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT));
                    String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));

                    list.add(new Transaction(id, type, amount, category, date, description));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return list;
    }

    public Transaction getTransactionById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TRANSACTIONS + " WHERE " + COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        Transaction tx = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                tx = new Transaction(id, type, amount, category, date, description);
            }
            cursor.close();
        }
        return tx;
    }

    public double getTotalIncome() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_TRANSACTIONS + " WHERE " + COLUMN_TYPE + "='Income'", null);
        double total = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
            cursor.close();
        }
        return total;
    }

    public double getTotalExpense() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_TRANSACTIONS + " WHERE " + COLUMN_TYPE + "='Expense'", null);
        double total = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
            cursor.close();
        }
        return total;
    }

    public double getCategoryExpenseTotal(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_TRANSACTIONS + " WHERE " + COLUMN_TYPE + "='Expense' AND " + COLUMN_CATEGORY + "=?", new String[]{category});
        double total = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
            cursor.close();
        }
        return total;
    }

    public String generateTransactionsCsv() {
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("ID,Type,Amount,Category,Date,Description\n");

        Cursor cursor = getAllTransactions();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE));
                    double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT));
                    String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));

                    String safeDesc = description != null ? "\"" + description.replace("\"", "\"\"") + "\"" : "\"\"";

                    csvBuilder.append(id).append(",")
                              .append(type).append(",")
                              .append(amount).append(",")
                              .append(category).append(",")
                              .append(date).append(",")
                              .append(safeDesc).append("\n");
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return csvBuilder.toString();
    }

    // ==========================================
    // BUDGETS OPERATIONS (Section 10 & Test T4)
    // ==========================================

    public boolean setOrUpdateBudget(String category, String period, double limitAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BUDGET_CATEGORY, category);
        values.put(COLUMN_BUDGET_PERIOD, period);
        values.put(COLUMN_BUDGET_LIMIT, limitAmount);

        long result = db.insertWithOnConflict(TABLE_BUDGETS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public List<Budget> getAllBudgetsWithSpending() {
        List<Budget> budgets = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_BUDGETS + " ORDER BY " + COLUMN_BUDGET_CATEGORY + " ASC", null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BUDGET_ID));
                    String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BUDGET_CATEGORY));
                    String period = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BUDGET_PERIOD));
                    double limitAmount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_BUDGET_LIMIT));
                    double spentAmount = getCategoryExpenseTotal(category);

                    budgets.add(new Budget(id, category, period, limitAmount, spentAmount));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return budgets;
    }

    public boolean deleteBudget(int budgetId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_BUDGETS, COLUMN_BUDGET_ID + "=?", new String[]{String.valueOf(budgetId)});
        return rows > 0;
    }

    // ==========================================
    // SAVINGS GOALS OPERATIONS (Section 10 & Test T9)
    // ==========================================

    public boolean addSavingsGoal(String goalName, double targetAmount, double currentAmount, String targetDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GOAL_NAME, goalName);
        values.put(COLUMN_GOAL_TARGET, targetAmount);
        values.put(COLUMN_GOAL_CURRENT, currentAmount);
        values.put(COLUMN_GOAL_DATE, targetDate);

        long result = db.insert(TABLE_SAVINGS_GOALS, null, values);
        return result != -1;
    }

    public boolean addSavingsGoalDeposit(int goalId, double depositAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_GOAL_CURRENT + " FROM " + TABLE_SAVINGS_GOALS + " WHERE " + COLUMN_GOAL_ID + "=?", new String[]{String.valueOf(goalId)});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                double current = cursor.getDouble(0);
                cursor.close();
                ContentValues values = new ContentValues();
                values.put(COLUMN_GOAL_CURRENT, current + depositAmount);
                int rows = db.update(TABLE_SAVINGS_GOALS, values, COLUMN_GOAL_ID + "=?", new String[]{String.valueOf(goalId)});
                return rows > 0;
            }
            cursor.close();
        }
        return false;
    }

    public boolean updateSavingsGoal(int goalId, String goalName, double targetAmount, double currentAmount, String targetDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GOAL_NAME, goalName);
        values.put(COLUMN_GOAL_TARGET, targetAmount);
        values.put(COLUMN_GOAL_CURRENT, currentAmount);
        values.put(COLUMN_GOAL_DATE, targetDate);

        int rows = db.update(TABLE_SAVINGS_GOALS, values, COLUMN_GOAL_ID + "=?", new String[]{String.valueOf(goalId)});
        return rows > 0;
    }

    public boolean deleteSavingsGoal(int goalId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_SAVINGS_GOALS, COLUMN_GOAL_ID + "=?", new String[]{String.valueOf(goalId)});
        return rows > 0;
    }

    public List<SavingsGoal> getAllSavingsGoals() {
        List<SavingsGoal> goals = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SAVINGS_GOALS + " ORDER BY " + COLUMN_GOAL_ID + " DESC", null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GOAL_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GOAL_NAME));
                    double target = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_GOAL_TARGET));
                    double current = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_GOAL_CURRENT));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GOAL_DATE));

                    goals.add(new SavingsGoal(id, name, target, current, date));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return goals;
    }
}
