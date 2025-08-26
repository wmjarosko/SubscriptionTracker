package com.example.subscriptiontracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import android.content.Context;
import android.content.SharedPreferences;


import static android.app.Activity.RESULT_OK;

public class AllSubscriptionsFragment extends Fragment {

    private static final int ADD_SUBSCRIPTION_REQUEST = 1;
    private static final int EDIT_SUBSCRIPTION_REQUEST = 2;
    private static final int EXPORT_SUBSCRIPTIONS_REQUEST = 3;
    private static final int IMPORT_SUBSCRIPTIONS_REQUEST = 4;
    private static final String PREFS_NAME = "SubscriptionTrackerPrefs";
    private static final String CATEGORIES_KEY = "Categories";

    private SubscriptionViewModel subscriptionViewModel;
    private RecyclerView recyclerView;
    private SubscriptionAdapter adapter;
    private List<Object> items = new ArrayList<>();
    private TextView monthlyTotalTextView;
    private TextView yearlyTotalTextView;
    private Spinner categoryFilterSpinner;
    private List<Subscription> allSubscriptions = new ArrayList<>();
    private boolean isDataLoaded = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_subscriptions, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        setupCategoryFilter();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        monthlyTotalTextView = view.findViewById(R.id.monthly_total_textview);
        yearlyTotalTextView = view.findViewById(R.id.yearly_total_textview);
        categoryFilterSpinner = view.findViewById(R.id.categoryFilterSpinner);

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddSubscriptionActivity.class);
            startActivityForResult(intent, ADD_SUBSCRIPTION_REQUEST);
        });

        subscriptionViewModel = new ViewModelProvider(requireActivity()).get(SubscriptionViewModel.class);
        subscriptionViewModel.getSubscriptions().observe(getViewLifecycleOwner(), subscriptions -> {
            allSubscriptions = subscriptions;
            isDataLoaded = true;
            filterAndDisplaySubscriptions();
        });
    }

    private void updateRecyclerView(List<Subscription> subscriptions) {
        if (subscriptions == null) return;
        List<Subscription> sortedSubscriptions = new ArrayList<>(subscriptions);
        Collections.sort(sortedSubscriptions, (s1, s2) -> Boolean.compare(s2.isActive(), s1.isActive()));

        items.clear();
        boolean activeHeaderAdded = false;
        boolean inactiveHeaderAdded = false;

        for (Subscription subscription : sortedSubscriptions) {
            if (subscription.isActive()) {
                if (!activeHeaderAdded) {
                    items.add("Active");
                    activeHeaderAdded = true;
                }
                items.add(subscription);
            } else {
                if (!inactiveHeaderAdded) {
                    items.add("Inactive");
                    inactiveHeaderAdded = true;
                }
                items.add(subscription);
            }
        }

        if (adapter == null) {
            adapter = new SubscriptionAdapter(items);
            adapter.setOnItemClickListener(position -> {
                Object item = items.get(position);
                if (item instanceof Subscription) {
                    Intent intent = new Intent(getActivity(), SubscriptionDetailsActivity.class);
                    intent.putExtra("subscription", (Subscription) item);
                    startActivityForResult(intent, EDIT_SUBSCRIPTION_REQUEST);
                }
            });
            recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private void calculateAndDisplayTotals(List<Subscription> subscriptions) {
        if (subscriptions == null) return;
        double monthlyTotal = 0;
        double yearlyTotal = 0;

        for (Subscription sub : subscriptions) {
            if (sub.isActive() && sub.isRecurring()) {
                if (sub.isMonthly()) {
                    monthlyTotal += sub.getPrice();
                } else {
                    yearlyTotal += sub.getPrice();
                }
            }
        }

        monthlyTotalTextView.setText(String.format("Monthly total: $%.2f", monthlyTotal));
        yearlyTotalTextView.setText(String.format("Yearly total: $%.2f", yearlyTotal));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_import) {
            showImportConfirmationDialog();
            return true;
        } else if (itemId == R.id.action_export) {
            exportSubscriptions();
            return true;
        } else if (itemId == R.id.action_manage_categories) {
            Intent intent = new Intent(getActivity(), CategoryManagerActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showImportConfirmationDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Import Subscriptions")
                .setMessage("This will replace all your current subscriptions. Are you sure you want to continue?")
                .setPositiveButton("Import", (dialog, which) -> importSubscriptions())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void importSubscriptions() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        startActivityForResult(intent, IMPORT_SUBSCRIPTIONS_REQUEST);
    }

    private void exportSubscriptions() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, "subscriptions.json");
        startActivityForResult(intent, EXPORT_SUBSCRIPTIONS_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) {
            return;
        }

        switch (requestCode) {
            case ADD_SUBSCRIPTION_REQUEST:
                handleAddSubscriptionResult(data);
                break;
            case EDIT_SUBSCRIPTION_REQUEST:
                handleEditSubscriptionResult(data);
                break;
            case EXPORT_SUBSCRIPTIONS_REQUEST:
                handleExportResult(data);
                break;
            case IMPORT_SUBSCRIPTIONS_REQUEST:
                handleImportResult(data);
                break;
        }
    }

    private void handleAddSubscriptionResult(Intent data) {
        String name = data.getStringExtra("name");
        long startDateMillis = data.getLongExtra("startDate", -1);
        long dateMillis = data.getLongExtra("date", -1);
        String email = data.getStringExtra("email");
        String notes = data.getStringExtra("notes");
        double price = data.getDoubleExtra("price", 0.0);
        boolean isRecurring = data.getBooleanExtra("isRecurring", false);
        boolean isMonthly = data.getBooleanExtra("isMonthly", false);
        String category = data.getStringExtra("category");

        if (name != null && startDateMillis != -1 && dateMillis != -1 && email != null) {
            Subscription newSubscription = new Subscription(name, new Date(startDateMillis), new Date(dateMillis), email, isRecurring, notes, isMonthly, price, category);
            subscriptionViewModel.addSubscription(newSubscription);
            // Calendar event logic would also go here if needed
        }
    }

    private void handleEditSubscriptionResult(Intent data) {
        if (data.hasExtra("subscription_to_delete")) {
            Subscription subscriptionToDelete = (Subscription) data.getSerializableExtra("subscription_to_delete");
            if (subscriptionToDelete != null) {
                subscriptionViewModel.deleteSubscription(subscriptionToDelete);
            }
        } else {
            Subscription updatedSubscription = (Subscription) data.getSerializableExtra("subscription");
            if (updatedSubscription != null) {
                subscriptionViewModel.updateSubscription(updatedSubscription);
            }
        }
    }

    private void handleExportResult(Intent data) {
        Uri uri = data.getData();
        if (uri != null) {
            try (OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri);
                 OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
                Gson gson = new Gson();
                gson.toJson(subscriptionViewModel.getSubscriptions().getValue(), writer);
                Log.d("AllSubscriptionsFragment", "Subscriptions exported successfully.");
            } catch (IOException e) {
                Log.e("AllSubscriptionsFragment", "Error exporting subscriptions", e);
            }
        }
    }

    private void handleImportResult(Intent data) {
        Uri uri = data.getData();
        if (uri != null) {
            try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                 InputStreamReader reader = new InputStreamReader(inputStream)) {
                Gson gson = new Gson();
                Type subscriptionListType = new TypeToken<ArrayList<Subscription>>(){}.getType();
                List<Subscription> importedSubscriptions = gson.fromJson(reader, subscriptionListType);
                if (importedSubscriptions != null) {
                    subscriptionViewModel.replaceAll(importedSubscriptions);
                    rebuildCategoriesFromImport(importedSubscriptions);
                    Log.d("AllSubscriptionsFragment", "Subscriptions imported successfully.");
                }
            } catch (IOException e) {
                Log.e("AllSubscriptionsFragment", "Error importing subscriptions", e);
            }
        }
    }

    private void rebuildCategoriesFromImport(List<Subscription> subscriptions) {
        Set<String> categories = new HashSet<>();
        for (Subscription sub : subscriptions) {
            if (sub.getCategory() != null && !sub.getCategory().isEmpty()) {
                categories.add(sub.getCategory());
            }
        }

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(CATEGORIES_KEY, categories);
        editor.apply();
    }

    private void setupCategoryFilter() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> categories = sharedPreferences.getStringSet(CATEGORIES_KEY, new HashSet<>());
        List<String> categoryList = new ArrayList<>();
        categoryList.add("All Categories");
        categoryList.addAll(categories);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryFilterSpinner.setAdapter(adapter);

        categoryFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterAndDisplaySubscriptions();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void filterAndDisplaySubscriptions() {
        if (!isDataLoaded || categoryFilterSpinner.getSelectedItem() == null) {
            return;
        }

        String selectedCategory = categoryFilterSpinner.getSelectedItem().toString();
        List<Subscription> filteredList = new ArrayList<>();

        if (selectedCategory.equals("All Categories")) {
            filteredList.addAll(allSubscriptions);
        } else {
            for (Subscription sub : allSubscriptions) {
                if (selectedCategory.equals(sub.getCategory())) {
                    filteredList.add(sub);
                }
            }
        }
        updateRecyclerView(filteredList);
        calculateAndDisplayTotals(filteredList);
    }
}
