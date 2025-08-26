This is an Android application for tracking subscriptions.

## Project Structure

The project follows a standard Android application structure:

```
.
├── app
│   ├── build.gradle
│   └── src
│       └── main
│           ├── java
│           │   └── com
│           │       └── example
│           │           └── subscriptiontracker
│           │               ├── MainActivity.java
│           │               └── Subscription.java
│           ├── res
│           │   ├── layout
│           │   │   └── activity_main.xml
│           │   └── values
│           │       ├── colors.xml
│           │       ├── strings.xml
│           │       └── themes.xml
│           └── AndroidManifest.xml
├── build.gradle
└── settings.gradle
```

## How to Build

This is a standard Gradle project. You can build it from the command line using:

```bash
./gradlew build
```

## How to Run

You can run the app on an emulator or a physical device using Android Studio or by running:

```bash
./gradlew installDebug
```

## Coding Conventions

- Use Java for all application code.
- Follow standard Android coding conventions.
- Use XML for layouts.
- Use `RecyclerView` for displaying lists of data.
- Use a `ViewModel` to hold and manage UI-related data.
- Use `LiveData` to notify the UI of data changes.
- Use `Room` for local data persistence.

(Note: Some of these components like `ViewModel`, `LiveData`, and `Room` will be added in later stages of development).
