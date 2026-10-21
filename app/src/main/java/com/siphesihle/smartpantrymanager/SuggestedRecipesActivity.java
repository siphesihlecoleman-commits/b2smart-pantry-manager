package com.siphesihle.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Shows only the recipes the user can make right now,
 * using the strict-matching rule in RecipeMatcher.
 */
public class SuggestedRecipesActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView rvRecipes;
    private TextView tvNoRecipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);
        rvRecipes = findViewById(R.id.rvRecipes);
        tvNoRecipes = findViewById(R.id.tvNoRecipes);
        rvRecipes.setLayoutManager(new LinearLayoutManager(this));
    }

    // Recalculate every time the screen is shown, so the list follows pantry changes
    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestions();
    }

    private void loadSuggestions() {
        List<PantryItem> pantry = dbHelper.getAllItems();
        List<Recipe> allRecipes = dbHelper.getAllRecipes();
        List<Recipe> matches = RecipeMatcher.getMatchingRecipes(allRecipes, pantry);

        if (matches.isEmpty()) {
            tvNoRecipes.setVisibility(View.VISIBLE);
            rvRecipes.setVisibility(View.GONE);
        } else {
            tvNoRecipes.setVisibility(View.GONE);
            rvRecipes.setVisibility(View.VISIBLE);
            rvRecipes.setAdapter(new RecipeAdapter(matches, recipe -> {
                Intent intent = new Intent(SuggestedRecipesActivity.this, RecipeDetailActivity.class);
                intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
                startActivity(intent);
            }));
        }
    }
}