# MedReminder 💊

An Android medication reminder app built with **Kotlin** and **Jetpack Compose**, focused on solving a real health safety problem — accidental double-dosing.

> Built as a personal project to demonstrate Android architecture skills for co-op applications.

---

## The Problem

Most reminder apps let you tap a button to mark a dose as taken. That's it. No confirmation, no safety check — making it easy to accidentally confirm twice, or re-take a dose too soon after the first.

MedReminder adds two layers of protection:
- **Swipe-to-confirm** instead of tap — requires deliberate intent
- **Double-dose warning** — detects if the same medication was taken within the last 2 hours and forces an explicit second confirmation

---

## Screenshots

> _Add screenshots here after running on device_

---

## Features

- 📅 **Today Screen** — see all medications scheduled for today with taken/pending status
- 💊 **Add Medication** — name, dosage, scheduled time, repeat days
- 🔔 **Push Notifications** — scheduled reminders via WorkManager, survive device reboots
- ⚠️ **Double-Dose Warning** — hard dialog if same med taken within 2 hours
- 👆 **Swipe-to-Confirm** — prevents accidental taps
- 📊 **Adherence Stats** — taken vs scheduled count, streak tracking
- 🕓 **Intake History** — timestamped log of all doses with on-time tracking
- 🌙 **Dark Mode** — full Material 3 dark theme support
- ⚙️ **Settings** — notifications toggle, snooze duration, daily reset time

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Repository Pattern |
| DI | Hilt (Dagger) |
| Database | Room |
| Preferences | DataStore |
| Background Work | WorkManager |
| Async | Kotlin Coroutines + Flow |
| Testing | JUnit 4, MockK, Turbine |

---

## Architecture

```
app/
├── data/
│   ├── local/          # Room entities, DAOs, Database
│   ├── datastore/      # UserPreferencesDataStore
│   └── repository/     # Repository implementation
├── di/                 # Hilt modules (Database, Repository, Worker)
├── domain/
│   ├── model/          # Medication, IntakeLog, AdherenceStats
│   └── usecase/        # Business logic use cases
├── presentation/
│   ├── today/          # Today screen + ViewModel
│   ├── add/            # Add medication screen + ViewModel
│   ├── history/        # Intake history screen + ViewModel
│   └── settings/       # Settings screen + ViewModel
├── worker/             # MedicationReminderWorker, DailyResetWorker
└── navigation/         # AppNavigation (NavHost)
```

**Key design decisions:**
- Use cases encapsulate all business logic — ViewModels only call use cases, never the repository directly
- `MarkAsTakenUseCase` returns a sealed class (`Success`, `DoubleDoseWarning`, `AlreadyTaken`) — the ViewModel reacts to each case
- `StateFlow` drives all UI state — single source of truth per screen

---

## Key Use Cases

### Double-Dose Prevention
```kotlin
sealed class MarkAsTakenResult {
    object Success : MarkAsTakenResult()
    data class DoubleDoseWarning(
        val medication: Medication,
        val lastTakenAt: Long,
        val minutesSinceLastDose: Long
    ) : MarkAsTakenResult()
    object AlreadyTaken : MarkAsTakenResult()
}
```

When a user marks a medication as taken, the app checks the last intake timestamp. If taken within 120 minutes, it returns `DoubleDoseWarning` instead of logging — requiring explicit confirmation before proceeding.

---

## Testing

12 unit tests across 3 test classes:

| Test Class | What it tests |
|-----------|--------------|
| `MarkAsTakenUseCaseTest` | Success, AlreadyTaken, DoubleDoseWarning, forceConfirm |
| `CheckDoubleDoseUseCaseTest` | 2-hour window logic, null handling, timing accuracy |
| `TodayViewModelTest` | StateFlow emissions, snackbar, warning state, dismiss |

```bash
./gradlew test
```

---

## How to Run

1. Clone the repo
2. Open in Android Studio Hedgehog or later
3. Run on emulator or physical device (API 26+)

```bash
git clone https://github.com/tandelj-coder/MedReminder.git
cd MedReminder
./gradlew assembleDebug
```

---

## What I Learned

- Structuring a multi-layer Android app with clean architecture from scratch
- Using Hilt for dependency injection across ViewModels, Workers, and Repositories
- Testing coroutine-based ViewModels with `StandardTestDispatcher` and Turbine
- WorkManager constraints and `BootReceiver` for reliable background scheduling

---

## Author

**Jigar Tandel** — 3rd Year B.Sc. Computer Science, Sheridan College  
[GitHub](https://github.com/tandelj-coder) · [LinkedIn](https://linkedin.com/in/student-sheridan)