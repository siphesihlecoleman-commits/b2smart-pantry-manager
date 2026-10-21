package com.siphesihle.smartpantrymanager;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
            @Override
            public void onItemClick(PantryItem item) {
                Toast.makeText(MainActivity.this, "Edit: " + item.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(PantryItem item) {
                dbHelper.deleteItem(item.getId());
                loadItems();
            }
        });
        rvPantry.setLayoutManager(new LinearLayoutManager(this));
        rvPantry.setAdapter(adapter);

        // TEMPORARY: the + button adds a test item (will become the Add screen)
        fabAdd.setOnClickListener(v -> {
            dbHelper.addItem(new PantryItem("Tomato", 3, "pcs", null));
            loadItems();
        });
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