package com.siphesihle.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Pantry List screen: shows every ingredient stored in the database.
 */
public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private PantryAdapter adapter;
    private RecyclerView rvPantry;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Connect Java variables to the views in activity_main.xml
        rvPantry = findViewById(R.id.rvPantry);
        tvEmpty = findViewById(R.id.tvEmpty);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        dbHelper = new DatabaseHelper(this);

        // Set up the RecyclerView with our adapter
        adapter = new PantryAdapter(dbHelper.getAllItems(), new PantryAdapter.OnItemClickListener() {
            // Tap a row: open the Add/Edit screen with that item's data
            @Override
            public void onItemClick(PantryItem item) {
                Intent intent = new Intent(MainActivity.this, AddEditItemActivity.class);
                intent.putExtra(AddEditItemActivity.EXTRA_ID, item.getId());
                intent.putExtra(AddEditItemActivity.EXTRA_NAME, item.getName());
                intent.putExtra(AddEditItemActivity.EXTRA_QUANTITY, item.getQuantity());
                intent.putExtra(AddEditItemActivity.EXTRA_UNIT, item.getUnit());
                intent.putExtra(AddEditItemActivity.EXTRA_EXPIRY, item.getExpiryDate());
                startActivity(intent);
            }

            // Long-press a row: delete it
            @Override
            public void onItemLongClick(PantryItem item) {
                dbHelper.deleteItem(item.getId());
                loadItems();
            }
        });
        rvPantry.setLayoutManager(new LinearLayoutManager(this));
        rvPantry.setAdapter(adapter);

        // + button: open the Add/Edit screen in "add" mode
        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddEditItemActivity.class)));

        // Bottom navigation bar (Pantry / Recipes / Settings)
        NavHelper.setup(this, R.id.nav_pantry);
    }

    // Reload the list every time the screen comes back into view
    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    // Read from the database, update the list, and show the empty message if needed
    private void loadItems() {
        List<PantryItem> items = dbHelper.getAllItems();
        adapter.setItems(items);
        if (items.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvPantry.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvPantry.setVisibility(View.VISIBLE);
        }
    }
}