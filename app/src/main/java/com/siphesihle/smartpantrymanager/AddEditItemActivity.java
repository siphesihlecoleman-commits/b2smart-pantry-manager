package com.siphesihle.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Screen used to add a new pantry item or edit an existing one.
 * If the launching Intent contains an item id, the screen is in "edit" mode.
 */
public class AddEditItemActivity extends AppCompatActivity {

    // Keys used to pass data between screens through an Intent
    public static final String EXTRA_ID = "item_id";
    public static final String EXTRA_NAME = "item_name";
    public static final String EXTRA_QUANTITY = "item_quantity";
    public static final String EXTRA_UNIT = "item_unit";
    public static final String EXTRA_EXPIRY = "item_expiry";

    private static final String[] UNITS = {"g", "kg", "ml", "l", "pcs", "cups", "tbsp", "tsp"};

    private EditText etName, etQuantity, etExpiry;
    private Spinner spUnit;
    private DatabaseHelper dbHelper;
    private int editId = -1;   // -1 means "adding a new item"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_item);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left + 32, systemBars.top + 32,
                    systemBars.right + 32, systemBars.bottom + 32);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        TextView tvFormTitle = findViewById(R.id.tvFormTitle);
        etName = findViewById(R.id.etName);
        etQuantity = findViewById(R.id.etQuantity);
        etExpiry = findViewById(R.id.etExpiry);
        spUnit = findViewById(R.id.spUnit);
        Button btnSave = findViewById(R.id.btnSave);

        // Fill the unit dropdown
        spUnit.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, UNITS));

        // If an item id was passed in, we are editing: pre-fill the form
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_ID)) {
            editId = intent.getIntExtra(EXTRA_ID, -1);
            tvFormTitle.setText("Edit Ingredient");
            etName.setText(intent.getStringExtra(EXTRA_NAME));
            etQuantity.setText(String.valueOf(intent.getDoubleExtra(EXTRA_QUANTITY, 0)));
            etExpiry.setText(intent.getStringExtra(EXTRA_EXPIRY));

            String unit = intent.getStringExtra(EXTRA_UNIT);
            for (int i = 0; i < UNITS.length; i++) {
                if (UNITS[i].equals(unit)) {
                    spUnit.setSelection(i);
                }
            }
        }

        btnSave.setOnClickListener(v -> saveItem());
    }

    // Validate the form, then insert or update the item
    private void saveItem() {
        String name = etName.getText().toString().trim();
        String quantityText = etQuantity.getText().toString().trim();
        String expiry = etExpiry.getText().toString().trim();
        String unit = spUnit.getSelectedItem().toString();

        // --- Input validation ---
        if (name.isEmpty()) {
            etName.setError("Please enter an ingredient name");
            return;
        }
        if (quantityText.isEmpty()) {
            etQuantity.setError("Please enter a quantity");
            return;
        }
        double quantity;
        try {
            quantity = Double.parseDouble(quantityText);
        } catch (NumberFormatException e) {
            etQuantity.setError("Quantity must be a number");
            return;
        }
        if (quantity <= 0) {
            etQuantity.setError("Quantity must be greater than 0");
            return;
        }
        if (!expiry.isEmpty() && !expiry.matches("\\d{4}-\\d{2}-\\d{2}")) {
            etExpiry.setError("Use the format yyyy-MM-dd, or leave blank");
            return;
        }

        // --- Save ---
        if (editId == -1) {
            dbHelper.addItem(new PantryItem(name, quantity, unit, expiry));
            Toast.makeText(this, "Ingredient added", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.updateItem(new PantryItem(editId, name, quantity, unit, expiry));
            Toast.makeText(this, "Ingredient updated", Toast.LENGTH_SHORT).show();
        }
        finish();   // close this screen and go back to the list
    }
}