package com.siphesihle.smartpantrymanager;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Shows the full ingredient list and method for one recipe.
 * The recipe is found using the id passed in through the Intent.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvTitle = findViewById(R.id.tvRecipeTitle);
        TextView tvIngredients = findViewById(R.id.tvIngredientList);
        TextView tvSteps = findViewById(R.id.tvSteps);

        // Find the recipe whose id was sent by the previous screen
        int recipeId = getIntent().getIntExtra(EXTRA_RECIPE_ID, -1);
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Recipe found = null;
        for (Recipe r : dbHelper.getAllRecipes()) {
            if (r.getId() == recipeId) {
                found = r;
                break;
            }
        }

        if (found == null) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvTitle.setText(found.getName());

        StringBuilder list = new StringBuilder();
        for (RecipeIngredient ing : found.getIngredients()) {
            double q = ing.getQuantity();
            String amount = (q == (long) q) ? String.valueOf((long) q) : String.valueOf(q);
            list.append("\u2022 ").append(ing.getName()).append(" - ")
                    .append(amount).append(" ").append(ing.getUnit()).append("\n");
        }
        tvIngredients.setText(list.toString().trim());
        tvSteps.setText(found.getSteps());
    }
}