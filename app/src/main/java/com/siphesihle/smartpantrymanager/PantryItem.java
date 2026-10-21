package com.siphesihle.smartpantrymanager;

/**
 * Represents one ingredient the user currently has in their pantry.
 * Maps to a row in the pantry_items table.
 */
public class PantryItem {

    private int id;              // assigned by the database
    private String name;         // e.g. "tomato"
    private double quantity;     // e.g. 500
    private String unit;         // e.g. "g", "ml", "pcs"
    private String expiryDate;   // optional, e.g. "2026-10-30" (can be null or empty)

    // Used when creating a NEW item (the database will assign the id)
    public PantryItem(String name, double quantity, String unit, String expiryDate) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    // Used when loading an EXISTING item back from the database
    public PantryItem(int id, String name, double quantity, String unit, String expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
}