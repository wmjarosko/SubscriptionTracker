package com.example.subscriptiontracker;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SubscriptionViewModel extends AndroidViewModel {

    private static final String FILE_NAME = "subscriptions.json";
    private final MutableLiveData<List<Subscription>> subscriptions = new MutableLiveData<>();

    public SubscriptionViewModel(@NonNull Application application) {
        super(application);
        loadSubscriptionsFromFile();
    }

    private void loadSubscriptionsFromFile() {
        File file = new File(getApplication().getFilesDir(), FILE_NAME);
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Gson gson = new Gson();
                Type subscriptionListType = new TypeToken<ArrayList<Subscription>>(){}.getType();
                List<Subscription> loadedSubscriptions = gson.fromJson(reader, subscriptionListType);
                subscriptions.setValue(loadedSubscriptions);
            } catch (IOException e) {
                // Log error or handle, for now init empty list
                subscriptions.setValue(new ArrayList<>());
            }
        } else {
            subscriptions.setValue(new ArrayList<>());
        }
    }

    public LiveData<List<Subscription>> getSubscriptions() {
        return subscriptions;
    }

    public void addSubscription(Subscription subscription) {
        List<Subscription> currentList = subscriptions.getValue();
        if (currentList != null) {
            currentList.add(subscription);
            subscriptions.setValue(currentList);
            saveSubscriptionsToFile();
        }
    }

    public void updateSubscription(Subscription updatedSubscription) {
        List<Subscription> currentList = subscriptions.getValue();
        if (currentList != null) {
            for (int i = 0; i < currentList.size(); i++) {
                if (currentList.get(i).getName().equals(updatedSubscription.getName())) {
                    currentList.set(i, updatedSubscription);
                    break;
                }
            }
            subscriptions.setValue(currentList);
            saveSubscriptionsToFile();
        }
    }

    public void deleteSubscription(Subscription subscriptionToDelete) {
        List<Subscription> currentList = subscriptions.getValue();
        if (currentList != null) {
            for (int i = 0; i < currentList.size(); i++) {
                if (currentList.get(i).getName().equals(subscriptionToDelete.getName())) {
                    currentList.remove(i);
                    break;
                }
            }
            subscriptions.setValue(currentList);
            saveSubscriptionsToFile();
        }
    }

    public void replaceAll(List<Subscription> newSubscriptions) {
        subscriptions.setValue(newSubscriptions);
        saveSubscriptionsToFile();
    }

    private void saveSubscriptionsToFile() {
        File file = new File(getApplication().getFilesDir(), FILE_NAME);
        try (FileWriter writer = new FileWriter(file)) {
            Gson gson = new Gson();
            gson.toJson(subscriptions.getValue(), writer);
        } catch (IOException e) {
            // Log error or handle
        }
    }
}
