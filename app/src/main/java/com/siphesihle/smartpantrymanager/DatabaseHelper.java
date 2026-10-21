package com.siphesihle.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates and manages the local SQLite database for Smart Pantry Manager.
 * Tables: pantry_items, recipes, recipe_ingredients.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pantry.db";
    private static final int DATABASE_VERSION = 2;

    // Table: pantry_items
    public static final String TABLE_PANTRY = "pantry_items";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_UNIT = "unit";
    public static final String COL_EXPIRY = "expiry_date";

    // Table: recipes
    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_STEPS = "steps";

    // Table: recipe_ingredients
    public static final String TABLE_RECIPE_ING = "recipe_ingredients";
    public static final String COL_RECIPE_ID = "recipe_id";
    public static final String COL_ING_NAME = "ingredient_name";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Runs once, the first time the database is created
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createPantry = "CREATE TABLE " + TABLE_PANTRY + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_QUANTITY + " REAL NOT NULL, "
                + COL_UNIT + " TEXT NOT NULL, "
                + COL_EXPIRY + " TEXT)";
        db.execSQL(createPantry);
        createRecipeTables(db);
    }

    // Runs when DATABASE_VERSION goes up: add the recipe tables, keep pantry data
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            createRecipeTables(db);
        }
    }

    // Creates the two tables used for recipes
    private void createRecipeTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_STEPS + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE_ING + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_RECIPE_ID + " INTEGER NOT NULL, "
                + COL_ING_NAME + " TEXT NOT NULL, "
                + COL_QUANTITY + " REAL NOT NULL, "
                + COL_UNIT + " TEXT NOT NULL, "
                + "FOREIGN KEY(" + COL_RECIPE_ID + ") REFERENCES "
                + TABLE_RECIPES + "(" + COL_ID + "))");
    }

    // CREATE: insert a new pantry item, returns the new row id
    public long addItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, item.getName());
        values.put(COL_QUANTITY, item.getQuantity());
        values.put(COL_UNIT, item.getUnit());
        values.put(COL_EXPIRY, item.getExpiryDate());
        return db.insert(TABLE_PANTRY, null, values);
    }

    // READ: get every pantry item from the database
    public List<PantryItem> getAllItems() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PANTRY, null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));
            double quantity = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_QUANTITY));
            String unit = cursor.getString(cursor.getColumnIndexOrThrow(COL_UNIT));
            String expiry = cursor.getString(cursor.getColumnIndexOrThrow(COL_EXPIRY));

            items.add(new PantryItem(id, name, quantity, unit, expiry));
        }
        cursor.close();
        return items;
    }

    // UPDATE: change an existing pantry item (found by its id)
    public int updateItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, item.getName());
        values.put(COL_QUANTITY, item.getQuantity());
        values.put(COL_UNIT, item.getUnit());
        values.put(COL_EXPIRY, item.getExpiryDate());
        return db.update(TABLE_PANTRY, values, COL_ID + "=?",
                new String[]{String.valueOf(item.getId())});
    }

    // DELETE: remove a pantry item by its id
    public int deleteItem(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_PANTRY, COL_ID + "=?", new String[]{String.valueOf(id)});
    }
}