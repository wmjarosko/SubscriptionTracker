package com.example.subscriptiontracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UpcomingSubscriptionsFragment extends Fragment {

    private SubscriptionViewModel subscriptionViewModel;
    private RecyclerView recyclerView;
    private SubscriptionAdapter adapter;
    private List<Object> items = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_upcoming_subscriptions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.upcoming_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        subscriptionViewModel = new ViewModelProvider(requireActivity()).get(SubscriptionViewModel.class);
        subscriptionViewModel.getSubscriptions().observe(getViewLifecycleOwner(), subscriptions -> {
            updateUpcomingList(subscriptions);
        });
    }

    private void updateUpcomingList(List<Subscription> subscriptions) {
        if (subscriptions == null) return;

        // Filter for upcoming subscriptions (next 60 days)
        List<Subscription> upcomingSubscriptions = new ArrayList<>();
        Calendar cal60Days = Calendar.getInstance();
        cal60Days.add(Calendar.DAY_OF_YEAR, 60);
        Date sixtyDaysFromNow = cal60Days.getTime();
        Date today = new Date();

        for (Subscription sub : subscriptions) {
            if (sub.isActive() && sub.getRenewalDate() != null &&
                sub.getRenewalDate().after(today) && sub.getRenewalDate().before(sixtyDaysFromNow)) {
                upcomingSubscriptions.add(sub);
            }
        }

        // Sort by renewal date
        Collections.sort(upcomingSubscriptions, (s1, s2) -> s1.getRenewalDate().compareTo(s2.getRenewalDate()));

        // Group by month
        items.clear();
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);
        String currentHeader = "";

        for (Subscription sub : upcomingSubscriptions) {
            String header = monthYearFormat.format(sub.getRenewalDate());
            if (!header.equals(currentHeader)) {
                items.add(header);
                currentHeader = header;
            }
            items.add(sub);
        }

        if (adapter == null) {
            adapter = new SubscriptionAdapter(items);
            // We can optionally set a click listener if we want to navigate to details from here
            // For now, this view is read-only
            recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }
}
