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
    private static final int DATABASE_VERSION = 3;

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
        seedRecipes(db);
    }

    // Runs when DATABASE_VERSION goes up; each step only runs for older versions
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            createRecipeTables(db);
        }
        if (oldVersion < 3) {
            seedRecipes(db);
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

    // ===================== PANTRY CRUD =====================

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

    // ===================== RECIPES =====================

    // READ: get every recipe together with its list of ingredients
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_RECIPES, null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));
            String steps = cursor.getString(cursor.getColumnIndexOrThrow(COL_STEPS));

            Recipe recipe = new Recipe(id, name, steps);

            // Load the ingredients that belong to this recipe
            Cursor ingCursor = db.rawQuery(
                    "SELECT * FROM " + TABLE_RECIPE_ING + " WHERE " + COL_RECIPE_ID + "=?",
                    new String[]{String.valueOf(id)});
            while (ingCursor.moveToNext()) {
                String ingName = ingCursor.getString(ingCursor.getColumnIndexOrThrow(COL_ING_NAME));
                double qty = ingCursor.getDouble(ingCursor.getColumnIndexOrThrow(COL_QUANTITY));
                String unit = ingCursor.getString(ingCursor.getColumnIndexOrThrow(COL_UNIT));
                recipe.addIngredient(new RecipeIngredient(ingName, qty, unit));
            }
            ingCursor.close();

            recipes.add(recipe);
        }
        cursor.close();
        return recipes;
    }

    // Inserts one recipe and its ingredients.
    // Each ingredient is written as "name|quantity|unit".
    private void insertRecipe(SQLiteDatabase db, String name, String steps, String... ingredients) {
        ContentValues recipeValues = new ContentValues();
        recipeValues.put(COL_NAME, name);
        recipeValues.put(COL_STEPS, steps);
        long recipeId = db.insert(TABLE_RECIPES, null, recipeValues);

        for (String ing : ingredients) {
            String[] parts = ing.split("\\|");
            ContentValues ingValues = new ContentValues();
            ingValues.put(COL_RECIPE_ID, recipeId);
            ingValues.put(COL_ING_NAME, parts[0]);
            ingValues.put(COL_QUANTITY, Double.parseDouble(parts[1]));
            ingValues.put(COL_UNIT, parts[2]);
            db.insert(TABLE_RECIPE_ING, null, ingValues);
        }
    }

    // Pre-loads the recipe collection (runs once)
    private void seedRecipes(SQLiteDatabase db) {
        insertRecipe(db, "Scrambled Eggs",
                "Beat the eggs with salt. Melt the butter in a pan. Pour in the eggs and stir gently until just set.",
                "egg|3|pcs", "butter|10|g", "salt|1|tsp");

        insertRecipe(db, "Tomato Omelette",
                "Chop the tomato. Beat the eggs with salt. Heat the oil, add the tomato for 1 minute, pour in the eggs and cook until set. Fold and serve.",
                "egg|2|pcs", "tomato|1|pcs", "salt|1|tsp", "oil|1|tbsp");

        insertRecipe(db, "Fried Rice",
                "Dice the onion and fry it in oil. Add the cooked rice, stir for 3 minutes. Push aside, scramble the eggs, then mix everything with soy sauce.",
                "rice|200|g", "egg|2|pcs", "onion|1|pcs", "oil|1|tbsp", "soy sauce|2|tbsp");

        insertRecipe(db, "Pancakes",
                "Whisk flour, milk, egg and sugar into a smooth batter. Pour small rounds into a hot pan and cook 2 minutes each side.",
                "flour|200|g", "milk|300|ml", "egg|1|pcs", "sugar|1|tbsp");

        insertRecipe(db, "Cheese Toast",
                "Butter the bread, top with sliced cheese and grill or toast until the cheese melts and bubbles.",
                "bread|2|pcs", "cheese|50|g", "butter|10|g");

        insertRecipe(db, "Tomato Pasta",
                "Boil the pasta until tender. Fry the chopped onion in oil, add chopped tomatoes and salt, and simmer for 10 minutes. Toss with the pasta.",
                "pasta|200|g", "tomato|3|pcs", "onion|1|pcs", "oil|1|tbsp", "salt|1|tsp");

        insertRecipe(db, "Mashed Potatoes",
                "Peel and boil the potatoes until soft. Drain, then mash with the butter, warm milk and salt until smooth.",
                "potato|4|pcs", "butter|30|g", "milk|100|ml", "salt|1|tsp");

        insertRecipe(db, "Chicken and Rice",
                "Season the chicken with salt and brown it in oil with chopped onion. Add the rice and enough water to cover, then simmer covered for 20 minutes.",
                "chicken|300|g", "rice|200|g", "onion|1|pcs", "salt|1|tsp", "oil|1|tbsp");

        insertRecipe(db, "Banana Pancakes",
                "Mash the bananas, mix in the egg and flour to make a batter, then cook spoonfuls in a hot pan for 2 minutes each side.",
                "banana|2|pcs", "egg|1|pcs", "flour|100|g");

        insertRecipe(db, "Vegetable Soup",
                "Chop the carrots, potatoes and onion. Fry the onion in oil, add the other vegetables, cover with water, add salt and simmer for 25 minutes.",
                "carrot|2|pcs", "potato|2|pcs", "onion|1|pcs", "salt|1|tsp", "oil|1|tbsp");

        insertRecipe(db, "Cheese Omelette",
                "Beat the eggs. Melt the butter in a pan, pour in the eggs, sprinkle with grated cheese and fold when set.",
                "egg|3|pcs", "cheese|40|g", "butter|10|g");

        insertRecipe(db, "Baked Potatoes",
                "Prick the potatoes, rub with oil and salt, and bake at 200 C for about 50 minutes until soft inside.",
                "potato|2|pcs", "oil|1|tbsp", "salt|1|tsp");

        insertRecipe(db, "Porridge",
                "Heat the oats and milk together, stirring, for 5 minutes until thick. Sweeten with sugar.",
                "oats|100|g", "milk|250|ml", "sugar|1|tbsp");

        insertRecipe(db, "Tuna Pasta",
                "Boil the pasta. Fry the chopped onion in oil, stir in the drained tuna, and toss with the pasta.",
                "pasta|200|g", "tuna|1|pcs", "onion|1|pcs", "oil|1|tbsp");

        insertRecipe(db, "French Toast",
                "Whisk eggs, milk and sugar. Soak the bread slices, then fry in a pan until golden on both sides.",
                "bread|4|pcs", "egg|2|pcs", "milk|100|ml", "sugar|1|tbsp");

        insertRecipe(db, "Rice and Beans",
                "Fry the chopped onion in oil, add the beans and salt, and warm through. Serve over the cooked rice.",
                "rice|200|g", "beans|200|g", "onion|1|pcs", "oil|1|tbsp", "salt|1|tsp");
    }
}