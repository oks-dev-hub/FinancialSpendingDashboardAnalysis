# Financial Spending Dashboard

A Kotlin-based Android financial analytics application designed to demonstrate how a large financial transaction dataset can be queried, aggregated, and presented through an interactive dashboard.

The project combines:

* Kotlin and Android.
* Jetpack Compose for the user interface.
* MVVM architecture.
* Room/SQLite for local financial data.
* Interactive Pie, Bar, and Line charts.
* A separate financial-data generation module.
* A large synthetic transaction dataset for performance testing.
* Docker-based Android QA and emulator support.
* Unit-test support for application logic.

The project is available on GitHub:

[Financial Spending Dashboard Analysis — GitHub](https://github.com/oks-dev-hub/FinancialSpendingDashboardAnalysis?utm_source=chatgpt.com)

---

# 1. What the Application Does

The **Financial Spending Dashboard** provides a graphical representation of financial spending across different months and spending categories.

The dashboard allows the user to:

* View spending for a selected month.
* Navigate between available months.
* View spending grouped by transaction category.
* Select individual spending categories.
* Compare spending for a selected category across multiple months.
* View daily spending trends for a selected month and category.
* Interact with one chart and see related charts update automatically.
* Explore different combinations of months and spending categories.

The dashboard is deliberately designed as an interconnected analytical experience rather than three independent charts.

The high-level relationship is:

```text
                 Selected Month
                      │
                      ▼
                ┌───────────┐
                │ Pie Chart │
                └─────┬─────┘
                      │
                Select Category
                      │
                      ▼
                ┌───────────┐
                │ Bar Chart │
                └─────┬─────┘
                      │
                 Select Month
                      │
                      ▼
                ┌───────────┐
                │ Line Chart│
                └───────────┘
                      │
                      ▼
             Daily Spending Trend
```

The user can therefore move from:

**Month → Category → Historical comparison → Daily detail**

without manually configuring each visualisation.

---

# 2. Dashboard Visualisations

## 2.1 Pie Chart — Monthly Category Breakdown

The Pie Chart provides the highest-level view of spending for the currently selected month.

Each slice represents a spending category.

For example:

```text
Selected Month
      │
      ├── Food
      ├── Transport
      ├── Insurance
      ├── Entertainment
      └── Other categories
```

The Pie Chart allows the user to understand:

* Which categories contributed to spending.
* The relative size of each category.
* The total amount associated with each category.
* The percentage contribution of each category.

The Pie Chart is also interactive.

When the user selects a slice:

```text
Pie Chart Slice
      │
      ▼
Selected Category
      │
      ├── Category indicator updates
      ├── Bar Chart updates
      └── Line Chart updates
```

The selected category therefore becomes the context for the other visualisations.

---

## 2.2 Bar Chart — Category Spending Across Months

Once a category has been selected, the Bar Chart provides a historical comparison of that category.

Conceptually:

```text
                 Selected Category
                        │
            ┌───────────┼───────────┐
            ▼           ▼           ▼
          Month 1     Month 2     Month 3
            │           │           │
            ▼           ▼           ▼
          £120        £185        £143
```

The Bar Chart answers:

> How much was spent on this category during each available month?

The Bar Chart therefore changes depending on the selected spending category.

A user can select a bar representing a particular month.

That interaction changes the dashboard's selected month and allows the other visualisations to update.

---

## 2.3 Line Chart — Daily Spending Trend

The Line Chart provides a more detailed view of spending within the selected month and category.

Conceptually:

```text
Selected Month
       +
Selected Category
       │
       ▼
Matching Transactions
       │
       ▼
Daily Aggregation
       │
       ▼
Line Chart
```

The Line Chart represents the daily spending behaviour for the selected month/category combination.

The Line Chart therefore changes when:

* The selected month changes.
* The selected category changes.
* A different month is selected from the Bar Chart.

---

# 3. Chart Interdependency

The charts are intentionally interconnected.

They represent different views of the same underlying transaction data.

## Selecting a Pie Chart Category

```text
Pie Chart
    │
    └── Select Category
             │
             ├── Updates selected category
             ├── Updates Bar Chart
             └── Updates Line Chart
```

## Selecting a Bar Chart Month

```text
Bar Chart
    │
    └── Select Month
             │
             ├── Updates selected month
             ├── Updates Pie Chart
             └── Updates Line Chart
```

## Changing the Month

```text
Month Pager
     │
     ├── Pie Chart changes
     ├── Line Chart changes
     └── Current dashboard state remains coordinated
```

This means that the charts are not independent components.

They are different projections of the same application state.

---

# 4. Chart Data Relationships

Each chart has a different analytical scope.

| Chart      | Scope                         | Purpose                        |
| ---------- | ----------------------------- | ------------------------------ |
| Pie Chart  | One month                     | Category distribution          |
| Bar Chart  | Multiple months, one category | Historical category comparison |
| Line Chart | One month + one category      | Daily spending trend           |

This can be represented as:

```text
1,000,000 Transactions
          │
          ├── Filter by Month
          │       │
          │       └── Pie Chart
          │
          ├── Filter by Category
          │       │
          │       └── Bar Chart
          │
          └── Filter by Month + Category
                  │
                  └── Line Chart
```

The application therefore avoids treating each chart as a separate data source.

Instead, each visualisation uses the same underlying financial dataset from a different analytical perspective.

---

# 5. Project Architecture

The repository is divided into several Gradle modules and application components.

```text
FinancialSpendingDashboardAnalysis
│
├── app
│   └── Android application
│
├── financialmodels
│   └── Shared financial/domain models
│
├── generatefinancialdashboarddatabase
│   └── Financial database generation
│
├── gradle
│   └── Gradle configuration
│
├── Dockerfile
│   └── Android QA environment
│
├── run.sh
│   └── Emulator/build/install/launch automation
│
└── README.md
```

The repository currently exposes these major modules and supporting build/QA files.

The separation exists to prevent the Android application from being responsible for generating its own large test dataset.

---

# 6. Why Separate the Database Generator?

Generating a large synthetic database is a different responsibility from displaying financial information.

The project therefore separates:

```text
Data Generation
      │
      ▼
Financial Database
      │
      ▼
Android Application
      │
      ▼
Queries / Aggregations
      │
      ▼
Dashboard
```

This provides several advantages.

### Separation of concerns

The Android application does not need to know how the test dataset was generated.

### Reproducibility

A prepared database can be supplied to the application.

### Faster application startup

The application does not need to generate hundreds of thousands of transactions before displaying the dashboard.

### Easier testing

The data-generation module can be changed independently from the UI.

### Realistic performance testing

The application can be tested against a substantially larger dataset than would normally be convenient to create manually.

---

# 7. Financial Data Generation

The `generatefinancialdashboarddatabase` module is responsible for creating the financial database used by the application.

The generator creates **synthetic financial transactions** rather than using real financial information.

The dataset is intended to provide sufficient volume for testing:

* Database queries.
* Aggregations.
* Filtering.
* Sorting.
* Chart calculations.
* UI responsiveness.
* ViewModel state changes.
* Application behaviour with large datasets.

The project uses financial categories and transaction characteristics to create data that is more useful for dashboard testing than simply generating identical rows.

The objective is not to reproduce a real person's exact spending behaviour.

The objective is to generate sufficiently varied financial patterns to exercise the application's aggregation and visualisation logic.

---

# 8. Large Dataset and Randomised Transactions

The application is designed to work with a large synthetic transaction dataset.

The dataset contains approximately:

```text
1,000,000 transactions
```

The transaction generator creates variations across factors such as:

* Transaction date.
* Spending category.
* Merchant.
* Transaction amount.
* Monthly distribution.

The use of randomised/synthetic values creates variation between categories and dates, allowing the charts to demonstrate meaningful differences.

For example:

```text
Transaction
    │
    ├── Date
    ├── Merchant
    ├── Category
    └── Amount
```

The generated transactions are then stored in the database.

This creates a realistic workload for the application without exposing real financial information.

---

# 9. Database Creation and Application Migration

The financial database is generated outside of the Android application.

The general process is:

```text
generatefinancialdashboarddatabase
              │
              ▼
       Generate transactions
              │
              ▼
          SQLite DB
              │
              ▼
       Application assets
              │
              ▼
        Android application
              │
              ▼
          Room / SQLite
              │
              ▼
       Repository / DAO
              │
              ▼
          ViewModel
              │
              ▼
       Jetpack Compose UI
```

This architecture means that the Android application receives a prepared dataset instead of spending its own runtime resources generating the dataset.

The application can then concentrate on querying, aggregating and displaying the financial information.

---

# 10. MVVM Architecture

The Android application follows an **MVVM-style architecture**.

MVVM stands for:

**Model — View — ViewModel**

The architecture can be represented as:

```text
┌──────────────────────────┐
│        Compose UI        │
│           View           │
└────────────┬─────────────┘
             │
             │ User actions
             ▼
┌──────────────────────────┐
│        ViewModel         │
│    State + coordination  │
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│       Repository         │
│    Data access boundary  │
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│       Room / SQLite      │
│     Financial database   │
└──────────────────────────┘
```

The repository's application source is organised into areas including `model`, `navGraph`, `room`, `ui/theme`, and `viewmodel`, together with the financial transaction repository.

---

# 11. Separation of Concerns

A major design principle of the project is **separation of concerns**.

Each component has a specific responsibility.

| Component          | Responsibility                                   |
| ------------------ | ------------------------------------------------ |
| Compose UI         | Display information and capture user interaction |
| ViewModel          | Maintain UI state and coordinate user actions    |
| Repository         | Provide a data-access boundary                   |
| DAO / Room         | Execute database operations                      |
| SQLite             | Store financial transactions                     |
| Financial Models   | Represent financial/domain data                  |
| Database Generator | Generate synthetic test data                     |
| Docker             | Provide a reproducible QA environment            |

This means that changing one responsibility does not necessarily require rewriting another.

For example:

```text
Change database generation
        ↓
Does not require
        ↓
Rewriting chart UI
```

Likewise:

```text
Change chart presentation
        ↓
Does not require
        ↓
Changing database generation
```

This makes the application easier to maintain, test and extend.

---

# 12. ViewModel Responsibilities

The ViewModel acts as the central coordination point between the Compose UI and application data.

It is responsible for maintaining UI-related state and responding to user actions.

Examples include:

* Current month.
* Selected spending category.
* Selected chart item.
* Pie Chart data.
* Bar Chart data.
* Line Chart data.
* Navigation-related state.
* State changes resulting from user interaction.

The intended flow is:

```text
User
 │
 ▼
Compose UI
 │
 ▼
ViewModel Action
 │
 ▼
ViewModel
 │
 ▼
Repository
 │
 ▼
Database
 │
 ▼
Aggregated Data
 │
 ▼
ViewModel State
 │
 ▼
Compose UI
```

The ViewModel therefore prevents individual Compose components from becoming responsible for database access and application-level decision making.

---

# 13. ViewModel Function Visibility and Encapsulation

One of the architectural benefits of this design is that internal ViewModel operations can remain **private**.

The UI should interact with the ViewModel through a controlled public interface rather than directly invoking every internal operation.

Conceptually:

```text
Compose UI
     │
     ▼
Public ViewModel Action
     │
     ▼
┌───────────────────────────┐
│        ViewModel          │
│                           │
│  private function A       │
│  private function B       │
│  private function C       │
│  private state handling   │
│                           │
└───────────────────────────┘
```

This provides stronger encapsulation.

The UI does not need to know how the ViewModel calculates its results.

For example, the UI can request:

```text
Select Category
```

without needing direct access to the internal functions that:

* Query the database.
* Transform the results.
* Calculate chart values.
* Update dependent state.
* Recalculate related visualisations.

## Why this is safer

Making internal functions private reduces the number of ways application logic can be accessed or modified.

It helps prevent:

* Accidental calls from UI components.
* Unintended state manipulation.
* Coupling between Compose components and internal implementation.
* Other classes depending on implementation details.

This is particularly useful because the dashboard contains several interdependent pieces of state.

## Important security clarification

Private ViewModel functions should **not** be interpreted as a security mechanism by themselves.

They improve **encapsulation and maintainability**, but Kotlin visibility modifiers do not provide encryption or protection against a determined attacker.

Application security is a separate concern.

For a production financial application, additional measures would be required, such as:

* Database encryption.
* Secure key management.
* Secure storage.
* Authentication and authorisation.
* Protection of sensitive logs.
* Network security.
* Appropriate access controls.

Therefore:

```text
Private ViewModel functions
        ↓
Better encapsulation
        ↓
Reduced accidental misuse
        ↓
More maintainable architecture
```

but not:

```text
Private functions
        ↓
Complete application security
```

---

# 14. ViewModel Actions and State Changes

The ViewModel acts as the single coordination point for dashboard interactions.

A typical interaction follows:

```text
User taps Pie Chart
        ↓
UI sends action
        ↓
ViewModel processes action
        ↓
Selected category changes
        ↓
Required data is obtained
        ↓
UI state is updated
        ↓
Compose recomposes affected UI
```

The same pattern applies to Bar Chart selections and month navigation.

This provides a predictable one-directional flow of information.

```text
UI
 │
 │ Action
 ▼
ViewModel
 │
 │ State
 ▼
UI
```

This reduces the risk of individual UI components maintaining conflicting versions of application state.

---

# 15. UI State and Recomposition

Jetpack Compose is declarative.

The UI is derived from application state.

Conceptually:

```text
State
  │
  ▼
Compose UI
  │
  ▼
User interaction
  │
  ▼
ViewModel action
  │
  ▼
Updated state
  │
  ▼
Compose recomposition
```

When state changes, Compose determines which parts of the UI need to be updated.

The application is structured so that state is owned at appropriate levels and components receive the data they actually need.

This allows Compose to avoid unnecessarily re-executing unrelated UI.

---

# 16. Smart Recomposition and Performance

The project benefits from what is commonly described as **smart recomposition** in Jetpack Compose.

More precisely, this is achieved through:

* Appropriate state ownership.
* Component boundaries.
* Stateless UI components where practical.
* Passing only the state required by a component.
* Avoiding unnecessary state changes.
* Allowing Compose to skip unaffected work where its stability analysis permits it.

The goal is not to prevent recomposition entirely.

Recomposition is a normal part of Compose.

The goal is to ensure that:

```text
State changes
      ↓
Only affected UI needs to be reconsidered
```

rather than:

```text
Small state change
      ↓
Entire dashboard unnecessarily recalculated
```

This is particularly important for an interactive dashboard containing several charts.

---

# 17. Why Recomposition Matters for the ViewModel

The ViewModel and Compose work together to keep state changes predictable.

For example, selecting a category can result in:

```text
Selected Category changes
        │
        ├── Pie Chart selection changes
        │
        ├── Bar Chart data changes
        │
        └── Line Chart data changes
```

The ViewModel produces the new state.

Compose then uses that state to update the appropriate UI.

The ViewModel should therefore avoid exposing unnecessary internal implementation details.

This is another reason why private helper functions are valuable.

The ViewModel can have:

```text
Public
 └── User-facing actions

Private
 ├── Data transformation
 ├── Chart calculations
 ├── State coordination
 └── Internal helpers
```

This creates a cleaner boundary between UI and application logic.

---

# 18. Why This Matters for a Large Dataset

The application is deliberately tested with a large transaction dataset.

A poor architecture could attempt to:

```text
Load every transaction
        ↓
Move all records into UI memory
        ↓
Calculate everything on UI thread
        ↓
Draw charts
```

This could create unnecessary:

* CPU usage.
* Memory usage.
* Garbage collection.
* UI thread workload.
* Recomposition work.

The dashboard instead benefits from aggregating the information closer to the database.

The intended flow is:

```text
Large transaction table
        │
        ▼
Filtered / aggregated query
        │
        ▼
Small chart dataset
        │
        ▼
ViewModel
        │
        ▼
Compose
        │
        ▼
Chart
```

The UI therefore does not need to create one UI element for every transaction.

---

# 19. Repository and Database Layer

The Repository provides a boundary between the ViewModel and the persistence layer.

Conceptually:

```text
ViewModel
    │
    ▼
Repository
    │
    ▼
DAO
    │
    ▼
Room
    │
    ▼
SQLite
```

The Repository is useful because the ViewModel does not need to know the implementation details of how financial data is stored.

For example, the ViewModel can request:

```text
Get spending for category X
```

without needing to construct the underlying SQL query itself.

This improves:

* Separation of concerns.
* Testability.
* Maintainability.
* Database abstraction.

---

# 20. Chart Data Aggregation

The charts represent different levels of aggregation.

| Chart      | Data Scope                     | Analytical Purpose    |
| ---------- | ------------------------------ | --------------------- |
| Pie Chart  | One month                      | Category distribution |
| Bar Chart  | Multiple months + one category | Historical comparison |
| Line Chart | One month + one category       | Daily spending trend  |

The same transaction table can therefore support multiple analytical views.

For example:

```text
1,000,000 transactions
          │
          ├── Month filter
          │       └── Category totals
          │               └── Pie Chart
          │
          ├── Category filter
          │       └── Monthly totals
          │               └── Bar Chart
          │
          └── Month + Category filter
                  └── Daily totals/averages
                          └── Line Chart
```

This is one of the main reasons the application can work with a large dataset without presenting all records directly to the UI.

---

# 21. Application Responsiveness

Responsiveness is important because the application combines:

* Large database queries.
* Financial aggregation.
* Interactive charts.
* Animated state changes.
* Month navigation.
* Multiple dependent visualisations.

The architectural objective is to keep expensive work away from the main UI rendering path.

The desired flow is:

```text
User interaction
      │
      ▼
ViewModel action
      │
      ▼
Data operation
      │
      ▼
Aggregated result
      │
      ▼
Updated UI state
      │
      ▼
Compose updates affected UI
```

This approach allows the UI to work with relatively small chart datasets rather than the entire transaction table.

---

# 22. Why the Architecture Is Efficient

The architecture is efficient primarily because it separates:

**Storage → Aggregation → State → Presentation**

Instead of:

```text
Database
   ↓
1,000,000 records
   ↓
UI memory
   ↓
UI calculations
   ↓
Charts
```

the intended flow is:

```text
Database
   ↓
Filtered / aggregated query
   ↓
Small result set
   ↓
ViewModel state
   ↓
Compose
   ↓
Chart
```

This reduces the amount of information crossing each architectural boundary.

It also makes the application easier to reason about and test.

The efficiency comes from the architecture and data-access strategy rather than simply from the size of the database.

---

# 23. ViewModel Unit Testing

Unit testing is important because the ViewModel contains application-level state and coordination logic.

A ViewModel test should verify that when a particular action occurs, the expected state is produced.

For example:

```text
User Action
     │
     ▼
ViewModel
     │
     ▼
Expected State
```

Tests can validate scenarios such as:

* Initial dashboard state.
* Month selection.
* Category selection.
* Pie Chart selection.
* Bar Chart selection.
* State transitions.
* Correct coordination between selected month and category.
* Handling of empty or unexpected data.
* Correct chart data being exposed to the UI.

## Current Test Structure

The current repository contains the standard Android unit-test source location:

```text
app/
└── src/
    └── test/
        └── java/
            └── com/
                └── example/
                    └── financialspendingdashboardanalysis/
                        └── ExampleUnitTest.kt
```

The current repository therefore provides the unit-test location, although the visible test file is currently `ExampleUnitTest.kt` rather than a dedicated ViewModel test class.

A dedicated ViewModel test can be introduced in the same test source set, for example:

```text
FinancialSpendingDashboardViewModelTest.kt
```

The purpose would be to test the ViewModel independently from the Android UI.

---

# 24. Purpose of ViewModel Unit Tests

A ViewModel unit test provides confidence that application logic works independently of Compose rendering.

For example:

```text
Test:
Select "Food"

Expected:
Selected category = Food
Bar Chart = Food monthly totals
Line Chart = Food daily data
```

Another test could verify:

```text
Test:
Select Month B

Expected:
Selected month = Month B
Pie Chart = Month B data
Line Chart = selected category for Month B
```

This is valuable because a chart displaying incorrect data may still look visually correct.

Unit tests can verify the **data and state relationships** behind the UI.

This gives two complementary levels of testing:

```text
Unit Tests
    │
    └── Verify logic/state

UI / QA Tests
    │
    └── Verify actual user interaction
```

Both are important.

---

# 25. ViewModel Testing and Private Functions

The fact that ViewModel helper functions are private does not prevent them from being tested.

The preferred approach is to test the **public behaviour** of the ViewModel rather than testing private implementation details.

For example, instead of testing:

```text
private calculateBarChartData()
```

directly, test:

```text
User selects category
        ↓
ViewModel
        ↓
Expected Bar Chart state
```

This is beneficial because tests remain focused on behaviour rather than implementation.

If the internal implementation changes but the externally observable behaviour remains correct, the test should continue to pass.

This supports maintainability.

---

# 26. Why Private Functions and Unit Tests Work Well Together

The combination provides two useful boundaries:

```text
Outside ViewModel
        │
        ▼
Public behaviour
        │
        ▼
Private implementation
        │
        ▼
Internal calculations
```

The UI and tests can verify the behaviour through the public interface.

The internal implementation can remain private.

This means developers are less likely to create tests that become tightly coupled to implementation details.

The result is:

* Better encapsulation.
* Cleaner APIs.
* Easier refactoring.
* More meaningful tests.
* Lower coupling.

---

# 27. Docker QA Environment

The project includes a Docker environment so testers do not need to manually install:

* Android Studio.
* Android SDK.
* Android emulator configuration.
* Gradle.
* Java/JDK.

The Docker environment provides the required Android build and emulator tooling.

The repository's Docker-based README describes a workflow in which Docker prepares the Android environment, builds the application, installs the APK and launches the application.

The high-level environment is:

```text
Docker
   │
   ├── Java
   ├── Android SDK
   ├── Android build tools
   ├── Android Emulator
   └── Financial Spending Dashboard
```

---

# 28. Docker Android Environment

The Docker environment is configured around:

* Java 17.
* Android command-line tools.
* Android SDK.
* Android Platform Tools.
* Android API 34.
* Android Build Tools.
* Android Emulator.
* Android API 34 Google APIs x86_64 system image.
* A `Pixel_API_34` Android Virtual Device.

The purpose is to provide a repeatable QA environment.

Instead of asking every tester to manually configure an Android development machine:

```text
Tester
  ↓
Docker
  ↓
Preconfigured Android environment
  ↓
Application
```

This makes onboarding easier for people who have little or no Android development experience.

---

# 29. Requirements

Recommended:

| Resource                | Recommendation              |
| ----------------------- | --------------------------- |
| Docker                  | Required                    |
| Git                     | Required                    |
| Internet                | Required for initial build  |
| RAM                     | 8 GB or more                |
| CPU                     | 4 cores or more             |
| Free disk space         | Approximately 20 GB or more |
| Hardware virtualisation | Recommended                 |

The Android emulator is resource-intensive.

If Docker does not have enough memory or CPU available, the emulator may start slowly or fail to boot.

---

# 30. Install Docker

Docker is the main external dependency required to run the QA environment.

Install Docker Desktop using the official Docker documentation:

[Docker Desktop installation documentation](https://docs.docker.com/desktop/setup/install/?utm_source=chatgpt.com)

Verify the installation:

```bash
docker --version
```

You can also test Docker:

```bash
docker run hello-world
```

A successful installation should display a message similar to:

```text
Hello from Docker!
This message shows that your installation appears to be working correctly.
```

---

# 31. Clone the Repository

Clone the project:

```bash
git clone https://github.com/oks-dev-hub/FinancialSpendingDashboardAnalysis.git
```

Enter the project:

```bash
cd FinancialSpendingDashboardAnalysis
```

The repository contains the application, financial models, database-generation module, Docker configuration and Gradle configuration.

---

# 32. Git LFS

If the repository uses Git LFS-managed files, initialise Git LFS:

```bash
git lfs install
```

Then retrieve the large files:

```bash
git lfs pull
```

This is important because Git LFS files may otherwise appear as pointer files rather than their actual contents.

---

# 33. Build the Docker Environment

Build the environment:

```bash
docker compose build
```

The first build may take several minutes because Docker may need to download:

```text
Java
  ↓
Android command-line tools
  ↓
Android SDK
  ↓
Android API 34
  ↓
Build Tools
  ↓
Android Emulator
  ↓
Android system image
  ↓
Gradle dependencies
```

Subsequent builds can be faster because Docker and Gradle can reuse cached content.

---

# 34. Start the Application

Start the environment:

```bash
docker compose up
```

The intended workflow is:

```text
Start Docker container
        ↓
Start Android emulator
        ↓
Wait for Android boot
        ↓
Build APK
        ↓
Install APK
        ↓
Launch Financial Spending Dashboard
        ↓
Application ready for QA
```

The project startup script automates the emulator/build/install/launch process.

---

# 35. Gradle Cache

The Docker Compose configuration uses a persistent Gradle cache volume.

Conceptually:

```yaml
volumes:
  - gradle-cache:/root/.gradle
```

The purpose is to avoid downloading the same Gradle dependencies every time the container starts.

The first run may require:

```text
Internet
   ↓
Download dependencies
   ↓
Gradle cache
```

Later runs can reuse:

```text
Gradle cache
   ↓
Existing dependencies
   ↓
Faster build
```

The cache is managed by Docker.

---

# 36. Understanding Android Components

If you have never worked with Android or Kotlin before, the following concepts are useful.

## Android SDK

The Android SDK contains the tools required to build and run Android applications.

Normally, developers install these tools through Android Studio.

This project installs the required Android tooling inside Docker.

---

## Kotlin

Kotlin is the primary programming language used by the application.

Kotlin does not need to be installed separately on the host computer when using the Docker environment.

The required build tooling is provided through the project and Gradle environment.

---

## Gradle

Gradle is the build system used by the Android project.

It is responsible for tasks such as:

```text
Download dependencies
        ↓
Compile Kotlin
        ↓
Process Android resources
        ↓
Package application
        ↓
Create APK
```

The project uses the Gradle Wrapper, allowing the required Gradle version to be obtained and used by the project.

---

## APK

An APK is the installable Android application package.

The build process is:

```text
Kotlin source
     ↓
Gradle
     ↓
APK
     ↓
Android device/emulator
     ↓
Financial Spending Dashboard
```

---

## Android Emulator

An Android emulator is a virtual Android device.

The Docker environment creates the project's configured emulator automatically.

The emulator is used as the default QA target when running the Docker environment without a physical Android device.

---

# 37. Optional Physical Android Device Testing

The application can also be tested on a physical Android device where the Docker/ADB setup and host configuration support device passthrough.

The Android device must have:

* Developer Options enabled.
* USB debugging enabled.
* A USB cable capable of data transfer.

A charging-only USB cable will not work.

For official Android instructions:

[Android Developer Options documentation](https://developer.android.com/studio/debug/dev-options?utm_source=chatgpt.com)

When connecting the device:

1. Connect the Android device using USB.
2. Ensure the cable supports data transfer.
3. Enable USB debugging.
4. Accept the Android USB debugging prompt if displayed.
5. Start the Docker environment.
6. Verify that the application is installed and launched on the intended device.

If device passthrough is unavailable in the host/Docker configuration, use the provided emulator.

---

# 38. Running the Application — Quick Start

If Docker and Git are already installed, run the commands separately in this order.

### 1. Download the project

```bash
git clone https://github.com/oks-dev-hub/FinancialSpendingDashboardAnalysis.git
```

### 2. Enter the project

```bash
cd FinancialSpendingDashboardAnalysis
```

### 3. Enable Git LFS

```bash
git lfs install
```

### 4. Download Git LFS files

```bash
git lfs pull
```

### 5. Build the Docker environment

```bash
docker compose build
```

### 6. Start the application

```bash
docker compose up
```

Then wait for:

```text
Docker
  ↓
Android Emulator
  ↓
Android Boot
  ↓
Gradle Build
  ↓
APK Installation
  ↓
Financial Spending Dashboard
```

No local Android Studio installation is required.

No local Android SDK installation is required.

No local Kotlin installation is required.

No local Gradle installation is required.

---

# 39. QA Testing Strategy

The application should not only be tested for whether it launches.

The most important part of the application is the relationship between:

```text
Month
 +
Category
 +
Pie Chart
 +
Bar Chart
 +
Line Chart
```

Testing should therefore cover both individual features and combinations of user actions.

---

# 40. Initial Application Test

When the application launches, verify:

* Application opens without crashing.
* Dashboard is displayed.
* Financial data is populated.
* A valid month is selected.
* Pie Chart contains data.
* Bar Chart contains data.
* Line Chart contains data.
* Categories are displayed correctly.

The default month should not be hard-coded in the test documentation because the dataset may change.

---

# 41. Month Navigation Tests

Test:

* Previous month.
* Next month.
* Every available month.
* Forward navigation.
* Backward navigation.
* Repeated navigation.

For each month, verify:

* Month label is correct.
* Pie Chart updates.
* Relevant Line Chart data updates.
* Category state remains consistent.
* No stale data from another month is displayed.

---

# 42. Spending Category Tests

For every available category:

1. Select the category.
2. Verify the Pie Chart slice is selected.
3. Verify the category indicator/index changes appropriately.
4. Verify the selected category is clearly visible.
5. Verify the Bar Chart updates.
6. Verify the Line Chart updates.
7. Select another category.
8. Verify the previous category does not incorrectly remain selected.

Repeat this for every category.

---

# 43. Pie Chart Tests

Verify:

* Pie Chart is visible.
* All applicable categories are represented.
* Each category has a corresponding slice.
* Slice sizes correspond to spending values.
* Category totals are correct.
* Category percentages are correct.
* Changing month changes the Pie Chart.
* Selecting a slice changes the selected category.
* Selected slices provide visual feedback.
* The category/index state reflects the selected slice.

---

# 44. Pie Chart Interaction Tests

For every available category:

```text
Select Slice
     ↓
Verify Slice Highlight
     ↓
Verify Category Selection
     ↓
Verify Bar Chart
     ↓
Verify Line Chart
```

The selected category must remain consistent across the dashboard.

---

# 45. Bar Chart Tests

Verify:

* Bar Chart is visible.
* Each bar corresponds to the correct month.
* Each bar displays the correct category spending.
* Changing the selected Pie Chart category updates the Bar Chart.
* Selecting a bar provides visual feedback.
* Selecting a bar changes the selected month.
* The selected category remains consistent.

---

# 46. Bar Chart Selection Tests

For every selectable bar:

1. Select the bar.
2. Verify that it becomes highlighted.
3. Verify that the corresponding month becomes selected.
4. Verify that the Pie Chart updates.
5. Verify that the Line Chart updates.
6. Verify that the selected category remains unchanged.

---

# 47. Line Chart Tests

Verify:

* Line Chart is visible.
* Selected month is correct.
* Selected category is correct.
* Daily values are displayed.
* Daily aggregation is correct.
* Changing month updates the graph.
* Changing category updates the graph.
* Selecting a Bar Chart month updates the graph.

The Line Chart should always represent:

```text
Selected Month
       +
Selected Category
```

---

# 48. Cross-Chart End-to-End Test

This is one of the most important functional tests.

Perform:

1. Select a month.
2. Select a category from the Pie Chart.
3. Verify the selected Pie Chart slice.
4. Verify the category indicator/index.
5. Verify the Bar Chart updates.
6. Select a month from the Bar Chart.
7. Verify the selected month.
8. Verify the Pie Chart updates.
9. Verify the Line Chart updates.
10. Verify the Line Chart represents the selected month/category combination.

Expected relationship:

```text
Selected Month
      │
      ▼
  Pie Chart
      │
      │ Select Category
      ▼
Selected Category
      │
      ▼
  Bar Chart
      │
      │ Select Month
      ▼
  Line Chart
      │
      ▼
Daily Spending Trend
```

---

# 49. Month × Category Permutation Testing

The application should be tested across combinations of months and categories.

For example:

```text
Month A + Category A
Month A + Category B
Month A + Category C

Month B + Category A
Month B + Category B
Month B + Category C

Month C + Category A
Month C + Category B
Month C + Category C
```

Continue until all available combinations have been exercised.

For each combination verify:

* Pie Chart.
* Selected category.
* Bar Chart.
* Selected month.
* Line Chart.
* Displayed values.
* Selected state.

This is particularly important because bugs can occur only when two valid states are combined.

---

# 50. Rapid Interaction Testing

Repeatedly change:

* Months.
* Pie Chart categories.
* Bar Chart selections.
* Pager position.
* Category selection.

For example:

```text
Month A
    ↓
Category A
    ↓
Month B
    ↓
Category C
    ↓
Month C
    ↓
Category B
    ↓
Bar Chart Month A
    ↓
Category D
    ↓
Month B
    ↓
Category A
```

Verify:

* UI remains responsive.
* No crashes occur.
* No stale data appears.
* Selected states remain consistent.
* Charts converge on the correct state.
* No visualisation displays data belonging to a previous selection.

---

# 51. UI Responsiveness Testing

Repeatedly interact with:

* Pie Chart slices.
* Category index.
* Bar Chart bars.
* Month navigation.
* Page/swipe navigation.

Verify:

* Animations remain usable.
* Selection feedback is immediate or reasonably responsive.
* Charts update correctly.
* The application does not freeze.
* The application does not crash.
* The UI does not display inconsistent states.

The purpose of this test is particularly important given the size of the underlying dataset.

---

# 52. Application Restart Testing

After completing several interactions:

1. Stop the application.
2. Restart the Docker environment.
3. Launch the application.
4. Verify the database is still available.
5. Verify the dashboard loads correctly.
6. Repeat the core interactions.

This verifies that database initialisation and application startup remain reliable between sessions.

---

# 53. Emulator and Physical Device Testing

Where supported, repeat the core tests on:

* Docker Android emulator.
* Physical Android device.

At minimum verify:

* Application launch.
* Month navigation.
* Pie Chart selection.
* Bar Chart selection.
* Line Chart updates.
* Category selection.
* Rapid interaction.
* Application restart.

Different hardware can expose different performance or rendering issues.

---

# 54. Complete End-to-End QA Scenario

A complete test should follow the user through the entire dashboard.

1. Start Docker.
2. Launch the application.
3. Verify the initial dashboard.
4. Verify the current/default month.
5. Verify the Pie Chart.
6. Select every available Pie Chart category.
7. Verify each category selection.
8. Verify the Bar Chart after each category selection.
9. Verify the Line Chart after each category selection.
10. Select each relevant Bar Chart month.
11. Verify the selected month.
12. Verify the Pie Chart.
13. Verify the Line Chart.
14. Navigate through every available month.
15. Repeat category selection for each month.
16. Test month/category combinations.
17. Navigate backwards through the months.
18. Repeat several interactions rapidly.
19. Restart the application.
20. Repeat the core interactions.
21. Test on a physical Android device where supported.

The application should produce consistent results regardless of the order in which the user navigates between months, categories and charts.

---

# 55. Docker Commands

## Build

```bash
docker compose build
```

## Start

```bash
docker compose up
```

## Start in background

```bash
docker compose up -d
```

## View logs

```bash
docker compose logs -f
```

## Stop

```bash
docker compose down
```

## Rebuild without cache

```bash
docker compose build --no-cache
```

---

# 56. Docker Desktop Resource Configuration

If the emulator is slow or fails to start, increase Docker resources.

Recommended starting point:

```text
Docker Desktop
    ↓
Settings
    ↓
Resources
    ↓
CPU: 4+
Memory: 8 GB+
```

The Android emulator requires considerably more resources than a typical Docker container.

---

# 57. Hardware Virtualisation

Android emulators generally perform better when hardware virtualisation is available.

If the emulator:

* Starts very slowly.
* Crashes.
* Fails to boot.
* Performs extremely slowly.

check that hardware virtualisation is enabled and available to Docker/the host operating system.

---

# 58. Troubleshooting

## Docker does not start

Check:

```bash
docker --version
```

Then:

```bash
docker run hello-world
```

---

## Git LFS files are missing

Run:

```bash
git lfs install
```

Then:

```bash
git lfs pull
```

---

## Docker build fails

Try:

```bash
docker compose build --no-cache
```

---

## Emulator is slow

Check:

* Docker memory.
* Docker CPU allocation.
* Hardware virtualisation.
* Available host resources.

Recommended:

```text
CPU: 4+
RAM: 8 GB+
```

---

## View Docker logs

Run:

```bash
docker compose logs -f
```

Review the output for:

* Emulator boot failures.
* Gradle errors.
* Android SDK errors.
* APK build errors.
* Installation errors.

---

## Stop the environment

```bash
docker compose down
```

---

# 59. Project Structure

At a high level:

```text
FinancialSpendingDashboardAnalysis/
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   └── java/
│   │   │       └── com/example/
│   │   │           └── financialspendingdashboardanalysis/
│   │   │               ├── model/
│   │   │               ├── navGraph/
│   │   │               ├── room/
│   │   │               ├── ui/
│   │   │               └── viewmodel/
│   │   │
│   │   └── test/
│   │       └── java/
│   │           └── com/example/
│   │               └── financialspendingdashboardanalysis/
│   │                   └── ExampleUnitTest.kt
│   │
│   └── build.gradle.kts
│
├── financialmodels/
│   └── Shared financial/domain models
│
├── generatefinancialdashboarddatabase/
│   └── Database generation module
│
├── gradle/
│   └── Gradle configuration
│
├── Dockerfile
│   └── Android Docker environment
│
├── run.sh
│   └── Emulator/build/install/launch automation
│
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
└── README.md
```

The main repository currently exposes these application packages and modules.

---

# 60. Architectural Summary

The complete architecture can be summarised as:

```text
                    SYNTHETIC DATA
                         │
                         ▼
        ┌────────────────────────────────┐
        │ Database Generation Module     │
        └───────────────┬────────────────┘
                        │
                        ▼
                 SQLite Database
                        │
                        ▼
        ┌────────────────────────────────┐
        │ Android Application            │
        │                                │
        │ Room / DAO                     │
        │       │                        │
        │       ▼                        │
        │ Repository                     │
        │       │                        │
        │       ▼                        │
        │ ViewModel                      │
        │       │                        │
        │       ▼                        │
        │ Jetpack Compose                │
        │       │                        │
        │       ├── Pie Chart            │
        │       ├── Bar Chart            │
        │       └── Line Chart            │
        └────────────────────────────────┘
                        │
                        ▼
                   Android Device
                  / Emulator
```

---

# 62. Overall Efficiency

The application's efficiency comes from combining several architectural decisions.

```text
Large dataset
      │
      ▼
Pre-generated database
      │
      ▼
Database-side aggregation
      │
      ▼
Small chart result sets
      │
      ▼
Repository abstraction
      │
      ▼
ViewModel state management
      │
      ▼
Compose state-driven UI
      │
      ▼
Optimised recomposition
      │
      ▼
Interactive charts
```

Each layer has a defined responsibility.

This prevents the application from unnecessarily moving the entire transaction dataset through every layer.

---

# 63. Key Design Principles

The project demonstrates several important software-engineering principles.

## Separation of Concerns

Each module has a defined responsibility.

## Encapsulation

ViewModel implementation details remain private where possible.

## Single Responsibility

Database generation, database access, state management and UI rendering are separated.

## Unidirectional State Flow

User actions enter the ViewModel and resulting state flows back toward the UI.

## Reproducibility

Docker provides a consistent Android QA environment.

## Testability

ViewModel behaviour can be tested independently from UI rendering.

## Scalability

Database aggregation allows a large dataset to be represented by relatively small chart datasets.

## Maintainability

Changing one architectural layer should require minimal changes to unrelated layers.

---

# 64. Final Application Flow

The complete application can be understood as:

```text
Generate synthetic transactions
            │
            ▼
Create SQLite database
            │
            ▼
Package database for application
            │
            ▼
Room / SQLite
            │
            ▼
Repository
            │
            ▼
ViewModel
            │
            ▼
Compose UI
            │
            ▼
┌───────────────────────────────┐
│       Financial Dashboard     │
│                               │
│       Pie Chart               │
│           │                   │
│           ▼                   │
│       Category                │
│           │                   │
│           ▼                   │
│       Bar Chart               │
│           │                   │
│           ▼                   │
│        Month                  │
│           │                   │
│           ▼                   │
│       Line Chart              │
│           │                   │
│           ▼                   │
│   Daily Spending Trend        │
└───────────────────────────────┘
```

---

# 65. Summary

The **Financial Spending Dashboard** combines a large synthetic financial dataset with an interactive Android dashboard.

The project demonstrates how a large dataset can be transformed into useful visual information without requiring the UI to directly process every transaction.

The primary architectural flow is:

```text
Synthetic Data
      ↓
Database Generator
      ↓
SQLite Database
      ↓
Room / DAO
      ↓
Repository
      ↓
ViewModel
      ↓
Jetpack Compose
      ↓
Interactive Charts
```

The three charts provide different analytical perspectives:

```text
Pie Chart
Monthly category distribution
        ↓
Bar Chart
Category history across months
        ↓
Line Chart
Daily trend for selected month/category
```

The ViewModel provides the central state-management boundary, while private helper functions keep implementation details encapsulated.

Compose recomposition optimisation allows the UI to respond to state changes without treating every interaction as a reason to rebuild unrelated parts of the dashboard.

Unit testing provides a way to validate ViewModel behaviour independently of the UI, while the Docker environment provides a repeatable environment for end-to-end QA testing.

The database-generation module keeps synthetic-data creation separate from the Android application, making the project easier to reproduce, maintain and performance-test.

Overall, the project demonstrates:

* Kotlin Android development.
* Jetpack Compose.
* MVVM architecture.
* Separation of concerns.
* Repository-based data access.
* Room/SQLite.
* Large-scale synthetic data generation.
* Database aggregation.
* Interactive data visualisation.
* ViewModel state management.
* Recomposition-aware UI design.
* Encapsulation.
* Unit testing.
* Docker-based Android QA.
* End-to-end interaction testing.

---

# 66. Repository

The complete source code, Android application, financial models, database-generation module, Docker environment, Gradle configuration and QA scripts are available here:

[Financial Spending Dashboard Analysis — GitHub Repository](https://github.com/oks-dev-hub/FinancialSpendingDashboardAnalysis?utm_source=chatgpt.com)

---
