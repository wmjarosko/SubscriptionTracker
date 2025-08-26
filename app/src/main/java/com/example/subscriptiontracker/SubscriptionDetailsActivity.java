package com.example.subscriptiontracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.content.SharedPreferences;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import android.widget.CompoundButton;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

public class SubscriptionDetailsActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText startDateEditText;
    private EditText renewalDateEditText;
    private EditText emailEditText;
    private EditText notesEditText;
    private EditText priceEditText;
    private Switch recurringSwitch;
    private Switch monthlySwitch;
    private Button toggleStatusButton;
    private Button editButton;
    private Button saveButton;
    private Button deleteButton;
    private Button addToCalendarButton;
    private Spinner categorySpinner;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "SubscriptionTrackerPrefs";
    private static final String CATEGORIES_KEY = "Categories";

    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_details);

        nameEditText = findViewById(R.id.nameEditText);
        startDateEditText = findViewById(R.id.startDateEditText);
        renewalDateEditText = findViewById(R.id.renewalDateEditText);
        emailEditText = findViewById(R.id.emailEditText);
        notesEditText = findViewById(R.id.notesEditText);
        priceEditText = findViewById(R.id.priceEditText);
        recurringSwitch = findViewById(R.id.recurringSwitch);
        monthlySwitch = findViewById(R.id.monthlySwitch);
        toggleStatusButton = findViewById(R.id.toggleStatusButton);
        editButton = findViewById(R.id.editButton);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);
        addToCalendarButton = findViewById(R.id.addToCalendarButton);
        categorySpinner = findViewById(R.id.categorySpinner);

        loadCategories();

        subscription = (Subscription) getIntent().getSerializableExtra("subscription");

        if (subscription != null) {
            populateUI();
        }

        toggleStatusButton.setOnClickListener(v -> {
            subscription.setActive(!subscription.isActive());
            populateUI();
        });

        editButton.setOnClickListener(v -> {
            enableEditing(true);
        });

        saveButton.setOnClickListener(v -> {
            saveChanges();
        });

        deleteButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("subscription_to_delete", subscription);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        addToCalendarButton.setOnClickListener(v -> {
            CalendarHelper.addEventToCalendar(this, subscription);
            Toast.makeText(this, "Event added to calendar", Toast.LENGTH_SHORT).show();
        });

        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {
            if (recurringSwitch.isChecked() && monthlySwitch.isChecked()) {
                autoPopulateRenewalDate();
            }
        };

        recurringSwitch.setOnCheckedChangeListener(listener);
        monthlySwitch.setOnCheckedChangeListener(listener);
    }

    private void autoPopulateRenewalDate() {
        String startDateString = startDateEditText.getText().toString();
        if (!startDateString.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            try {
                Date startDate = sdf.parse(startDateString);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(startDate);
                calendar.add(Calendar.MONTH, 1);
                renewalDateEditText.setText(sdf.format(calendar.getTime()));
            } catch (ParseException e) {
                // Ignore
            }
        }
    }

    private void populateUI() {
        nameEditText.setText(subscription.getName());

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        startDateEditText.setText(sdf.format(subscription.getStartDate()));
        renewalDateEditText.setText(sdf.format(subscription.getRenewalDate()));
        emailEditText.setText(subscription.getEmail());
        notesEditText.setText(subscription.getNotes());
        priceEditText.setText(String.valueOf(subscription.getPrice()));
        recurringSwitch.setChecked(subscription.isRecurring());
        monthlySwitch.setChecked(subscription.isMonthly());
        toggleStatusButton.setText(subscription.isActive() ? "Deactivate" : "Activate");

        deleteButton.setVisibility(subscription.isActive() ? View.GONE : View.VISIBLE);

        if (subscription.getCategory() != null) {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) categorySpinner.getAdapter();
            int position = adapter.getPosition(subscription.getCategory());
            categorySpinner.setSelection(position);
        }
    }

    private void loadCategories() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Set<String> categories = sharedPreferences.getStringSet(CATEGORIES_KEY, new HashSet<>());
        ArrayList<String> categoryList = new ArrayList<>(categories);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private void enableEditing(boolean isEnabled) {
        nameEditText.setEnabled(isEnabled);
        startDateEditText.setEnabled(isEnabled);
        renewalDateEditText.setEnabled(isEnabled);
        emailEditText.setEnabled(isEnabled);
        notesEditText.setEnabled(isEnabled);
        priceEditText.setEnabled(isEnabled);
        recurringSwitch.setEnabled(isEnabled);
        monthlySwitch.setEnabled(isEnabled);
        categorySpinner.setEnabled(isEnabled);
        toggleStatusButton.setEnabled(!isEnabled);
        deleteButton.setEnabled(!isEnabled);

        editButton.setVisibility(isEnabled ? View.GONE : View.VISIBLE);
        saveButton.setVisibility(isEnabled ? View.VISIBLE : View.GONE);
    }

    private void saveChanges() {
        String name = nameEditText.getText().toString();
        String startDateString = startDateEditText.getText().toString();
        String dateString = renewalDateEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String notes = notesEditText.getText().toString();
        String priceString = priceEditText.getText().toString();
        boolean isRecurring = recurringSwitch.isChecked();
        boolean isMonthly = monthlySwitch.isChecked();
        String category = categorySpinner.getSelectedItem() != null ? categorySpinner.getSelectedItem().toString() : "";

        if (name.isEmpty() || startDateString.isEmpty() || dateString.isEmpty() || email.isEmpty() || priceString.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        Date startDate;
        Date renewalDate;
        double price;
        try {
            startDate = sdf.parse(startDateString);
            renewalDate = sdf.parse(dateString);
            price = Double.parseDouble(priceString);
        } catch (ParseException | NumberFormatException e) {
            Toast.makeText(this, "Invalid date or price format.", Toast.LENGTH_SHORT).show();
            return;
        }

        subscription.setName(name);
        subscription.setStartDate(startDate);
        subscription.setRenewalDate(renewalDate);
        subscription.setEmail(email);
        subscription.setNotes(notes);
        subscription.setPrice(price);
        subscription.setRecurring(isRecurring);
        subscription.setMonthly(isMonthly);
        subscription.setCategory(category);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("subscription", subscription);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("subscription", subscription);
        setResult(RESULT_OK, resultIntent);
        super.onBackPressed();
    }
}
