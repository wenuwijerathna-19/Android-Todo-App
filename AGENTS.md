# AGENTS.md

Summary of workspace analysis and next steps

## Project snapshot
- Project path: `C:\Users\janit\AndroidStudioProjects\MyApplication3`
- Single Android application module: `:app`
- Build system: Gradle Kotlin DSL with version catalog (`gradle/libs.versions.toml`)

## PDF: Assignment text (exact content provided)

"Overview: 
This individual assignment requires you to develop an Android app that promotes personal wellness 
and helps users manage their daily health routines. The app must combine multiple features using 
Android Studio to demonstrate your ability to use various Android development tools and principles 
- Gradle wrapper: Gradle 8.13 (`gradle/wrapper/gradle-wrapper.properties`)
Objectives: 
• Design and implement an interactive mobile app to manage the daily health routines of users. 
• Utilize Android Studio and Kotlin for the development process. 
• Demonstrate  knowledge  of  Android  programming  concepts  and  data  persistence 
techniques without using databases. 
Requirements: 
1. Daily Habit Tracker – Add, edit, delete daily wellness habits (e.g., drink water, meditate, 
steps). Display completion progress for each day. 
2. Mood Journal with Emoji Selector – Log mood entries with date/time and a chosen emoji. 
Provide a simple list or calendar view of past moods. 
3. Hydration Reminder – Use Notifications/AlarmManager/WorkManager to remind users to 
drink water at their chosen intervals. 
4. Advanced Feature (choose at least one) – Home-screen Widget showing today’s habit 
completion percentage, Sensor Integration (e.g., accelerometer to count steps or detect 
shake to add a quick mood), or a Simple Chart (MPAndroidChart) showing mood trend over 
a week. 
Technical Requirements 
• Architecture: Use Fragments/Activities for screens (Habits, Mood Journal, Settings). 
• Data Persistence: Store user data using SharedPreferences where appropriate. 
• Intents: Use implicit/explicit intents for navigation and sharing (e.g., share mood summary). 
• State Management: Retain user settings across sessions. 
• Responsive UI: Must adapt to phones and tablets, portrait & landscape. 
Submission: 
• Submit a zipped file of your Android Studio project via the course web. 
• Include all the source code, resources, and a report in PDF format. 
• The report should contain a brief description, and screenshots of the app. 
Evaluation: 
• Code quality and organization. 
• How well does your app works and adheres to the specified requirements. 
• Creativity and UI design. 
• The app should not have significant bugs or issues. 
• Be prepared to explain your code and design decisions during the viva session."

## Mapping: assignment requirements -> current repo status

- 1) Daily Habit Tracker
   - Status: Partially implemented (now includes edit + per-day completion support).
      - `HabitPreference.kt` (uses `SharedPreferences` + JSON array) stores habits (name + time). Added `update(...)` that returns success/failure, and per-day completion APIs (`setCompletion` / `isCompleted`). Fixed delete loop.
      - `HabitActivity.kt`, `HabitAdapter.kt`, `item_view_habit.xml`, and `bottom_sheet_add_habit.xml` now support adding, deleting, editing (long-press opens edit sheet), and marking daily completion (checkbox).
   - Missing / gaps:
      - Duplicate-time prevention is enforced (insert/update return failure). Habit identity remains based on `time` per current design (multiple habits at the exact same time are not supported).
   - Files to update / review: `app/src/main/java/com/example/myapplication/db/HabitPreference.kt`, `app/src/main/java/com/example/myapplication/adapter/HabitAdapter.kt`, `app/src/main/java/com/example/myapplication/fragment/HabitAddBottomSheetFragment.kt`, `app/src/main/res/layout/item_view_habit.xml` (checkbox), and `app/src/main/java/com/example/myapplication/HabitActivity.kt` (listener wiring). Add UI for daily summary view when ready.

- 2) Mood Journal with Emoji Selector
      - Status: Partially implemented (new `MoodFragment`, model, preference, and layouts added).
      - Implemented files:
         - `app/src/main/java/com/example/myapplication/model/MoodEntry.kt` (data model)
         - `app/src/main/java/com/example/myapplication/db/MoodPreference.kt` (SharedPreferences persistence)
         - `app/src/main/java/com/example/myapplication/fragment/MoodFragment.kt` (calendar GridView, FAB, bottom sheet to add moods)
         - `app/src/main/res/layout/fragment_mood.xml`, `item_mood_day.xml`, `bottom_sheet_add_mood.xml`
      - How to use / Add mood to calendar:
         1. Open the app and navigate to the Mood tab (the project already wires `HabitActivity` bottom navigation to call `MoodFragment.newInstance()`).
         2. The calendar shows the current month. Days with a saved mood display the chosen emoji in the day cell.
         3. Tap the Floating Action Button (bottom-right) to add a mood for today. A bottom sheet appears with emoji buttons and an optional note field.
         4. Select an emoji, optionally add a note, and tap Save. The mood will be stored in `SharedPreferences` and the calendar will refresh to show the emoji on the selected day.
         5. Tap any day cell to add or edit the mood for that date.

      - Additional behavior implemented:
        - Month navigation: the calendar header shows the current month (e.g., "September 2025") with Prev/Next buttons to move between months.
        - Future dates are blocked: you can only add or edit moods for today or previous dates. Attempting to open the editor for a future date (or using the FAB while viewing a future month) shows a short message and does not allow editing.

- 3) Hydration Reminder
   - Status: Partially implemented (basic alarm scheduling present).
      - `MainActivity.kt` sets an `AlarmManager` one-shot alarm (demo) and `AlarmReceiver.kt` exists (logs the event).
   - Missing / gaps:
      - No Notification creation in `AlarmReceiver` (only Log.d).
      - No user-configurable intervals or persistent scheduling across reboots.
      - Consider using `WorkManager` or repeating `AlarmManager` with proper `SCHEDULE_EXACT_ALARM` handling.
   - Files to update: `AlarmReceiver.kt` (create NotificationChannel + show notification), add `Settings` screen to store interval, update `MainActivity` / `Settings` to schedule repeating alarms.

- 4) Advanced Feature (at least one)
   - Status: Not implemented.
   - Options and recommendations:
      - Home-screen Widget: `AppWidgetProvider` showing today's completion percentage.
      - Sensor integration: add accelerometer handling in a background service or activity to count steps or detect shake to add mood entry.
      - Chart: add `MPAndroidChart` dependency and implement a simple weekly mood trend chart.

## Technical requirements check

- Architecture: Activities and Fragments present (`HabitActivity`, `MainActivity`, `HabitAddBottomSheetFragment`) — OK.
- Data persistence: `SharedPreferences` used for habits (`HabitPreference`) — OK for assignment requirement (no DB required).
- Intents: `PendingIntent` with `AlarmReceiver` present; explicit navigation between activities should be checked/implemented where needed (e.g., a Settings activity or Mood activity is missing).
- State Management: Preferences used; settings UI is missing.
- Responsive UI: multiple layouts exist; needs verification on different screen sizes and orientations.
- Android Gradle Plugin (AGP): 8.13.0 (version catalog)
## Actionable implementation plan (prioritized)

1) Make the Habit Tracker feature complete (2–4 hours)
    - Implement edit habit (update by time key) in `HabitPreference.kt` and add edit UI in `HabitAddBottomSheetFragment.kt` or inline editor in `HabitAdapter`.
    - Implement per-day completion tracking: store a per-habit map keyed by ISO date to booleans (SharedPreferences JSON). Add UI (checkbox or swipe) in `item_view_habit.xml` to mark completion and a daily summary header showing percent complete.
    - Files: `db/HabitPreference.kt`, `adapter/HabitAdapter.kt`, `fragment/HabitAddBottomSheetFragment.kt`, layouts.

2) Implement Hydration Reminder (1–3 hours)
    - Create Notification channel and show notifications from `AlarmReceiver.kt` instead of just logging.
    - Add a `SettingsActivity`/fragment to configure reminder intervals and enable/disable reminders. Persist setting in `SharedPreferences`.
    - Schedule repeating alarms correctly (consider `AlarmManager.setExactAndAllowWhileIdle()` for exact alarms; request `SCHEDULE_EXACT_ALARM` rights on devices that require it). Optionally implement `WorkManager` for periodic reminders.

3) Add Mood Journal (2–4 hours)
    - Add `MoodActivity` with emoji selector, date/time stamp, store entries in `SharedPreferences` as JSON array (reuse the existing pattern).
    - Add list/calendar view; basic list view is enough to meet requirements.

4) Add one Advanced Feature (3–8 hours depending on choice)
    - Widget: implement `AppWidgetProvider`, widget layout, update logic reading today's completion from `SharedPreferences`.
    - Sensor: implement accelerometer listener in `MainActivity` or a dedicated foreground service for step detection / shake detection to add quick mood entry.
    - Chart: add `MPAndroidChart` dependency and create a weekly mood trend chart reading stored mood entries.

5) Tests, polish, and submission (2–4 hours)
    - Add unit tests for `HabitPreference` (parsing JSON, insert/delete/update) in `app/src/test`.
    - Add instrumented tests for key UI flows if possible.
    - Build release/zip, produce screenshots, and write the PDF report with description and screenshots.

## Quick commands (PowerShell, project root)

```powershell
.\gradlew.bat clean assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest  # requires device/emulator
```

## Small checklist I can implement for you (pick items)
- [ ] Implement edit habit + per-day completion tracking (I can modify `HabitPreference.kt`, adapter, and layouts).
- [ ] Implement hydration notification in `AlarmReceiver.kt` and a `Settings` UI for intervals.
- [ ] Add `MoodActivity` and persistence for mood entries.
- [ ] Add one advanced feature (Widget / Sensor integration / MPAndroidChart).
- [ ] Add GitHub Actions workflow to run `assembleDebug` and unit tests.

If you want, I can start with any of the checklist items above — say which one and I'll update the code and tests. 
- Kotlin: 2.0.21 (version catalog)
- compileSdk = 36, minSdk = 24, targetSdk = 36 (from `app/build.gradle.kts`)
- Java compatibility: 11 (source/target) + Kotlin `jvmTarget = "11"`

## Files reviewed
- `build.gradle.kts` (root)
- `settings.gradle.kts`
- `gradle/libs.versions.toml` (versions and plugin aliases)
- `gradle/wrapper/gradle-wrapper.properties`
- `gradle.properties`
- `local.properties` (contains local SDK path; not for VCS)
- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- Key Kotlin sources in `app/src/main/java/com/example/myapplication/` (MainActivity, HabitActivity, AlarmReceiver, adapters/fragments, model)
- Tests: `app/src/test/...` and `app/src/androidTest/...` (example tests present)
- Attached document: `2025_LabExam-3.pdf` (referenced below)

## Progress so far
- Completed project inspection: Gradle files, version catalog, wrapper version, module build settings, manifest, Kotlin sources, resources, and test stubs.
- Confirmed: single module app, Kotlin language, AGP/Kotlin/Gradle versions, compile/min/target SDKs, and a minimal test scaffold.
- Implemented duplicate-time prevention for habits and basic per-day completion plumbing.
   - Files changed: `app/src/main/java/com/example/myapplication/db/HabitPreference.kt` (insert/update now return boolean, completion APIs added, delete loop fixed), `app/src/main/java/com/example/myapplication/fragment/HabitAddBottomSheetFragment.kt` (shows validation error when time collides), `app/src/main/res/layout/item_view_habit.xml` (completion CheckBox added), `app/src/main/java/com/example/myapplication/adapter/HabitAdapter.kt` and `app/src/main/java/com/example/myapplication/HabitActivity.kt` (adapter binding, long-press edit, and completion callbacks wired).
   - Behavior: attempting to add or update a habit to a time already used will fail and the UI shows an inline error instead of dismissing.
   - Status: code updated and local file-level error checks passed for edited files; build not executed here.

## Issues/risks found
- Local SDK path in `local.properties` must be set per developer machine.
- `SCHEDULE_EXACT_ALARM` permission is requested — verify runtime behavior across Android versions.
- No CI workflows present.
- Minimal test coverage (only example tests). Consider expanding tests for critical logic.

## Recommended prioritized next build tasks
1. Verify local build environment and run the first build
   - Ensure JDK 11 is installed and Android SDK at `C:\Users\janit\AppData\Local\Android\Sdk` or update `local.properties`.
   - From project root (PowerShell):
     ```powershell
     .\gradlew.bat clean assembleDebug
     .\gradlew.bat testDebugUnitTest
     .\gradlew.bat connectedDebugAndroidTest  # requires emulator/device
     ```
2. Add CI (GitHub Actions) to run `assembleDebug` and unit tests on push — create `.github/workflows/android.yml` with Gradle cache support.
3. Improve automated tests
   - Add unit tests for `HabitPreference`, `HabitAdapter` logic, and any data transformation.
   - Add UI/instrumented tests for scheduling alarms and RecyclerView behavior.
4. Handle release configuration
   - Set up signing configs (keystore) and enable `isMinifyEnabled` + proguard rules if you plan to release.
5. Review runtime permissions & exact alarms
   - Add runtime-checks and guidance UI for `SCHEDULE_EXACT_ALARM` where needed.
6. Documentation & onboarding
   - Add `README.md` with quick start (IDE and CLI commands) and SDK/JDK requirements.

## Suggested low-risk improvements (quick wins)
- Add a `README.md` or expand this `AGENTS.md` with build badges after CI is added.
- Add a GitHub Actions workflow for fast feedback.
- Add a small script or Gradle task to ensure consistent formatting (e.g., ktlint or detekt later).

## Small checklist I implemented
- [x] Prevent duplicate habit times (insert/update now validate and return success boolean; UI reports conflict)

## How to get me to run a build or add CI
- Tell me to run `assembleDebug` and tests; I can run the Gradle tasks in the workspace and report results.
- Or, ask me to create a GitHub Actions workflow file — I can add it and show the YAML.

---

If you want I will now:
- Run a local build and tests in this workspace and report the results, or
- Add a basic GitHub Actions workflow and a `README.md` with build instructions.
