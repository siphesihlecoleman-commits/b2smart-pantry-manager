package com.siphesihle.smartpantrymanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Connects the list of PantryItem objects to the RecyclerView.
 * Each item is displayed using the item_pantry.xml row layout.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    // Lets the Activity react when a row is tapped or long-pressed
    public interface OnItemClickListener {
        void onItemClick(PantryItem item);
        void onItemLongClick(PantryItem item);
    }

    private List<PantryItem> items;
    private final OnItemClickListener listener;

    public PantryAdapter(List<PantryItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    // Replace the data and redraw the list
    public void setItems(List<PantryItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    // Creates a new, empty row from item_pantry.xml
    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    // Fills a row with the data of the item at this position
    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = items.get(position);

        holder.tvName.setText(item.getName());

        String details = item.getQuantity() + " " + item.getUnit();
        if (item.getExpiryDate() != null && !item.getExpiryDate().isEmpty()) {
            details += "  |  Expires: " + item.getExpiryDate();

            // Only flag items if the user switched the setting on
            boolean alertsOn = holder.itemView.getContext()
                    .getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE)
                    .getBoolean(SettingsActivity.KEY_EXPIRY_ALERTS, true);
            if (alertsOn && isExpiringSoon(item.getExpiryDate())) {
                details += "  (EXPIRING SOON)";
            }
        }
        holder.tvDetails.setText(details);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onItemLongClick(item);
            return true;
        });
    }

    // True if the date (yyyy-MM-dd) is within 3 days from now, or already past
    private boolean isExpiringSoon(String expiry) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(expiry);
            if (date == null) {
                return false;
            }
            long daysLeft = (date.getTime() - System.currentTimeMillis()) / (1000L * 60 * 60 * 24);
            return daysLeft <= 3;
        } catch (ParseException e) {
            return false;
        }
    }

    // How many rows the list has
    @Override
    public int getItemCount() {
        return items.size();
    }

    // Holds the views of one row so they can be reused when scrolling
    static class PantryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvDetails;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvDetails = itemView.findViewById(R.id.tvItemDetails);
        }
    }
}