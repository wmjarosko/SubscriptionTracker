package com.example.subscriptiontracker;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SubscriptionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    private List<Object> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public SubscriptionAdapter(List<Object> items) {
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof String) {
            return VIEW_TYPE_HEADER;
        } else {
            return VIEW_TYPE_ITEM;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_item, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.subscription_item, parent, false);
            return new SubscriptionViewHolder(view, listener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_HEADER) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            headerViewHolder.headerTextView.setText((String) items.get(position));
        } else {
            SubscriptionViewHolder subscriptionViewHolder = (SubscriptionViewHolder) holder;
            Subscription subscription = (Subscription) items.get(position);

            subscriptionViewHolder.nameTextView.setText(subscription.getName());

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            subscriptionViewHolder.dateTextView.setText(sdf.format(subscription.getRenewalDate()));

            subscriptionViewHolder.recurringTextView.setText(subscription.isRecurring() ? "Recurring" : "One-time");

            if (subscription.isActive()) {
                subscriptionViewHolder.statusIndicatorTextView.setText("Active");
                subscriptionViewHolder.statusIndicatorTextView.setTextColor(Color.GREEN);
            } else {
                subscriptionViewHolder.statusIndicatorTextView.setText("Inactive");
                subscriptionViewHolder.statusIndicatorTextView.setTextColor(Color.RED);
            }

            if (subscription.isMonthly()) {
                subscriptionViewHolder.monthlyIndicatorTextView.setText("Monthly");
                subscriptionViewHolder.monthlyIndicatorTextView.setVisibility(View.VISIBLE);
            } else {
                subscriptionViewHolder.monthlyIndicatorTextView.setVisibility(View.GONE);
            }

            subscriptionViewHolder.priceTextView.setText(String.format(Locale.US, "$%.2f", subscription.getPrice()));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SubscriptionViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView dateTextView;
        TextView recurringTextView;
        TextView statusIndicatorTextView;
        TextView monthlyIndicatorTextView;
        TextView priceTextView;

        public SubscriptionViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            recurringTextView = itemView.findViewById(R.id.recurringTextView);
            statusIndicatorTextView = itemView.findViewById(R.id.statusIndicatorTextView);
            monthlyIndicatorTextView = itemView.findViewById(R.id.monthlyIndicatorTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerTextView;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerTextView = itemView.findViewById(R.id.headerTextView);
        }
    }
}
