package com.example.subscriptiontracker;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import android.widget.CompoundButton;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import android.content.SharedPreferences;


public class AddSubscriptionActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText startDateEditText;
    private EditText dateEditText;
    private EditText emailEditText;
    private EditText notesEditText;
    private EditText priceEditText;
    private Switch recurringSwitch;
    private Switch monthlySwitch;
    private CheckBox addToCalendarCheckBox;
    private Button saveButton;
    private Spinner categorySpinner;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "SubscriptionTrackerPrefs";
    private static final String CATEGORIES_KEY = "Categories";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_subscription);

        nameEditText = findViewById(R.id.nameEditText);
        startDateEditText = findViewById(R.id.startDateEditText);
        dateEditText = findViewById(R.id.dateEditText);
        emailEditText = findViewById(R.id.emailEditText);
        notesEditText = findViewById(R.id.notesEditText);
        priceEditText = findViewById(R.id.priceEditText);
        recurringSwitch = findViewById(R.id.recurringSwitch);
        monthlySwitch = findViewById(R.id.monthlySwitch);
        addToCalendarCheckBox = findViewById(R.id.addToCalendarCheckBox);
        saveButton = findViewById(R.id.saveButton);
        categorySpinner = findViewById(R.id.categorySpinner);

        loadCategories();

        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {
            if (recurringSwitch.isChecked() && monthlySwitch.isChecked()) {
                autoPopulateRenewalDate();
            }
        };

        recurringSwitch.setOnCheckedChangeListener(listener);
        monthlySwitch.setOnCheckedChangeListener(listener);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSubscription();
            }
        });

        startDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(startDateEditText);
            }
        });

        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(dateEditText);
            }
        });
    }

    private void showDatePickerDialog(final EditText dateEditText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    Calendar newDate = Calendar.getInstance();
                    newDate.set(year1, monthOfYear, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                    dateEditText.setText(sdf.format(newDate.getTime()));
                }, year, month, day);
        datePickerDialog.show();
    }

    private void loadCategories() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Set<String> categories = sharedPreferences.getStringSet(CATEGORIES_KEY, new HashSet<>());
        ArrayList<String> categoryList = new ArrayList<>(categories);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
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
                dateEditText.setText(sdf.format(calendar.getTime()));
            } catch (ParseException e) {
                // Ignore
            }
        }
    }

    private void saveSubscription() {
        String name = nameEditText.getText().toString();
        String startDateString = startDateEditText.getText().toString();
        String dateString = dateEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String notes = notesEditText.getText().toString();
        String priceString = priceEditText.getText().toString();
        boolean isRecurring = recurringSwitch.isChecked();
        boolean isMonthly = monthlySwitch.isChecked();
        boolean addToCalendar = addToCalendarCheckBox.isChecked();
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

        Intent resultIntent = new Intent();
        resultIntent.putExtra("name", name);
        resultIntent.putExtra("startDate", startDate.getTime());
        resultIntent.putExtra("date", renewalDate.getTime());
        resultIntent.putExtra("email", email);
        resultIntent.putExtra("notes", notes);
        resultIntent.putExtra("price", price);
        resultIntent.putExtra("isRecurring", isRecurring);
        resultIntent.putExtra("isMonthly", isMonthly);
        resultIntent.putExtra("addToCalendar", addToCalendar);
        resultIntent.putExtra("category", category);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
