package com.example.subscriptiontracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CategoryManagerActivity extends AppCompatActivity {

    private EditText categoryNameEditText;
    private Button addCategoryButton;
    private ListView categoryListView;
    private ArrayList<String> categoryList;
    private ArrayAdapter<String> categoryAdapter;
    private SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "SubscriptionTrackerPrefs";
    private static final String CATEGORIES_KEY = "Categories";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_manager);

        categoryNameEditText = findViewById(R.id.categoryNameEditText);
        addCategoryButton = findViewById(R.id.addCategoryButton);
        categoryListView = findViewById(R.id.categoryListView);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadCategories();

        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categoryList);
        categoryListView.setAdapter(categoryAdapter);

        addCategoryButton.setOnClickListener(v -> addCategory());

        categoryListView.setOnItemLongClickListener((parent, view, position, id) -> {
            new AlertDialog.Builder(CategoryManagerActivity.this)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete this category?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    deleteCategory(position);
                })
                .setNegativeButton("No", null)
                .show();
            return true;
        });
    }

    private void loadCategories() {
        Set<String> categories = sharedPreferences.getStringSet(CATEGORIES_KEY, new HashSet<>());
        categoryList = new ArrayList<>(categories);
    }

    private void saveCategories() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(CATEGORIES_KEY, new HashSet<>(categoryList));
        editor.apply();
    }

    private void addCategory() {
        String categoryName = categoryNameEditText.getText().toString().trim();
        if (categoryName.isEmpty()) {
            Toast.makeText(this, "Category name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (categoryName.length() > 25) {
            Toast.makeText(this, "Category name cannot exceed 25 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        if (categoryList.contains(categoryName)) {
            Toast.makeText(this, "Category already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        categoryList.add(categoryName);
        categoryAdapter.notifyDataSetChanged();
        saveCategories();
        categoryNameEditText.setText("");
        Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show();
    }

    private void deleteCategory(int position) {
        categoryList.remove(position);
        categoryAdapter.notifyDataSetChanged();
        saveCategories();
        Toast.makeText(this, "Category deleted", Toast.LENGTH_SHORT).show();
    }
}
