package com.siphesihle.smartpantrymanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Core business logic of the app: the strict-matching rule.
 * A recipe is only suggested if EVERY ingredient it needs is in the pantry
 * in at least the required quantity.
 */
public class RecipeMatcher {

    // Returns only the recipes the user can cook right now
    public static List<Recipe> getMatchingRecipes(List<Recipe> recipes, List<PantryItem> pantry) {
        Map<String, Double> available = buildPantryTotals(pantry);
        List<Recipe> matches = new ArrayList<>();

        for (Recipe recipe : recipes) {
            if (canMake(recipe, available)) {
                matches.add(recipe);
            }
        }
        return matches;
    }

    // Step 1: total the pantry by ingredient name, in base units
    private static Map<String, Double> buildPantryTotals(List<PantryItem> pantry) {
        Map<String, Double> totals = new HashMap<>();
        for (PantryItem item : pantry) {
            String key = makeKey(item.getName(), item.getUnit());
            double amount = toBaseAmount(item.getQuantity(), item.getUnit());
            Double existing = totals.get(key);
            totals.put(key, existing == null ? amount : existing + amount);
        }
        return totals;
    }

    // Step 2-3: one missing or too-small ingredient rejects the whole recipe
    private static boolean canMake(Recipe recipe, Map<String, Double> available) {
        if (recipe.getIngredients().isEmpty()) {
            return false;
        }
        for (RecipeIngredient needed : recipe.getIngredients()) {
            String key = makeKey(needed.getName(), needed.getUnit());
            Double have = available.get(key);
            double required = toBaseAmount(needed.getQuantity(), needed.getUnit());

            if (have == null || have < required) {
                return false;   // missing or not enough: strict rule fails
            }
        }
        return true;   // every ingredient passed
    }

    // Name + kind of measurement, e.g. "tomato:count" or "flour:amount"
    private static String makeKey(String name, String unit) {
        String kind = unit.trim().equalsIgnoreCase("pcs") ? "count" : "amount";
        return normalizeName(name) + ":" + kind;
    }

    // Cleans names so simple differences still match
    // ("Tomatoes " -> "tomato", "Eggs" -> "egg", "Potatoes" -> "potato")
    static String normalizeName(String name) {
        String n = name.trim().toLowerCase(Locale.ROOT);
        if (n.endsWith("ies") && n.length() > 4) {
            n = n.substring(0, n.length() - 3) + "y";
        } else if (n.endsWith("oes") && n.length() > 4) {
            n = n.substring(0, n.length() - 2);
        } else if (n.endsWith("s") && !n.endsWith("ss") && n.length() > 3) {
            n = n.substring(0, n.length() - 1);
        }
        return n;
    }

    // Converts a quantity to a base unit so different units can be compared.
    // Simplification: 1 g is treated as 1 ml; pieces are counted separately.
    static double toBaseAmount(double quantity, String unit) {
        switch (unit.trim().toLowerCase(Locale.ROOT)) {
            case "kg":   return quantity * 1000;
            case "l":    return quantity * 1000;
            case "cups": return quantity * 240;
            case "tbsp": return quantity * 15;
            case "tsp":  return quantity * 5;
            default:     return quantity;   // g, ml, pcs
        }
    }
}