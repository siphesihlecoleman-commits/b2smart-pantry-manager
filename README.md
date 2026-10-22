# b2smart-pantry-manager

**Smart Pantry Manager** is a Java Android app that helps reduce food waste. The user records the ingredients they have at home, and the app suggests only the recipes they can cook right now, with no shopping trip needed.

## Features
- Add, edit and delete pantry items (name, quantity, unit, optional expiry date) with input validation
- Pantry list built with a RecyclerView and a custom adapter
- 16 pre-loaded recipes stored in the database
- **Strict matching:** a recipe is suggested only if every ingredient is in the pantry in at least the required quantity
- Matching copes with plurals ("tomatoes" / "tomato") and unit differences (kg / g, l / ml)
- Recipe detail screen with ingredients and method
- Settings screen with an "expiring soon" flag toggle
- Bottom navigation (Pantry, Recipes, Settings)

## Database choice: SQLite
I chose SQLite (SQLiteOpenHelper) because it is stored locally on the device, needs no accounts, servers or internet connection, and is the approach covered in the module. The data persists between sessions. Tables: `pantry_items`, `recipes`, `recipe_ingredients`.

## Setup and run instructions
1. Clone the repository: `git clone https://github.com/siphesihlecoleman-commits/b2smart-pantry-manager.git`
2. Open the `SmartPantryManager` folder in Android Studio.
3. Wait for the Gradle sync to finish.
4. Create or select an emulator (for example Medium Phone, API 37) or connect a physical device.
5. Click **Run**.

## Technologies
Java, Android Studio, SQLite, RecyclerView, Intents, SharedPreferences

## Author
Siphesihle Xolani  AND Hlatshwayo, 402307146
Mobile App Development 700, Richfield