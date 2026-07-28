# Financial Spending Dashboard - Docker QA Environment

This document explains how to run the **Financial Spending Dashboard** application using Docker.

The purpose of this setup is to provide a consistent testing environment without requiring:

- Android Studio
- Android SDK installation
- Manual emulator setup
- Gradle installation

Docker will automatically create an Android testing environment, build the APK, install it, and launch the application.

---

# Requirements

Before starting, ensure the machine has:

- Docker installed
- Internet connection (required for the first Docker build)

Minimum recommended hardware:

- 8GB RAM
- 4 CPU cores
- 20GB free disk space

For the Android emulator to run efficiently:

- Hardware virtualization should be enabled

---

# Installing Docker

Docker must be installed before running this project.

Follow the official Docker installation documentation:

https://docs.docker.com/desktop/setup/install/

After installation, verify Docker is running:

```bash
docker --version
```

Test the installation:

```bash
docker run hello-world
```

Expected output:

```text
Hello from Docker!
This message shows that your installation appears to be working correctly.
```

Docker is now ready.

---

# Running the Financial Dashboard Test Environment

## 1. Clone Repository

Clone the repository:

```bash
# You need Git LFS installed to automatically download the database
git lfs install
```

```bash
git clone https://github.com/oks-dev-hub/FinancialSpendingDashboardAnalysis.git
```

Navigate into the project:

```bash
cd FinancialSpendingDashboardAnalysis
```

The project should contain:

```text
Dockerfile
run.sh
gradlew
settings.gradle
app/
```

---

# 2. Build Docker Image

Run:

```bash
docker build -t financial-dashboard .
```

During this process Docker automatically:

```text
Downloads Java environment
        ↓
Installs Android SDK tools
        ↓
Installs Android Emulator
        ↓
Downloads Android API 34 system image
        ↓
Creates Pixel_API_34 emulator
        ↓
Configures Gradle
        ↓
Prepares application
```

The first build may take several minutes.

---

# 3. Run Application

Start the test environment:

```bash
docker run --rm financial-dashboard
```

Docker automatically:

```text
Starts Android Emulator
        ↓
Waits for Android boot
        ↓
Unlocks emulator
        ↓
Builds APK
        ↓
Installs APK
        ↓
Launches Financial Spending Dashboard
```

Expected output:

```text
Starting emulator...

Waiting for Android boot...

Unlocking emulator...

Building APK...

Installing APK...

Launching application...

Done
```

---

## Emulator Performance Issues

Ensure:

- Hardware virtualization is enabled
- Docker has enough allocated resources

Recommended Docker Desktop settings:

```text
Settings
    ↓
Resources
    ↓
CPU: 4+
Memory: 8GB+
```

---

## Build Failure

Clean Docker resources:

```bash
docker system prune
```

Rebuild:

```bash
docker build --no-cache -t financial-dashboard-test .
```

---