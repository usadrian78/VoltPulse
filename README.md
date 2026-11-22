# VoltPulse
Lightweight AMOLED battery health, charge cycle count, and live power monitor for Motorola phones (Android 14+, no root required via Shizuku). Built by ro.us.adrian.
# VOLT // PULSE (`ro.us.adrian`)

A lightweight, AMOLED Pure Black battery telemetry, health, and charge cycle monitor built specifically for **Motorola phones running Android 14+** (no root required).

### Key Features
* **Exact Charge Cycle Count & Cell Wear %** (read directly from Motorola's proprietary `IMotHealth` / `dumpsys battery` service)
* **Real Full Capacity vs. Design Capacity** (tested on **Motorola Edge 30 Fusion** — `4245 / 4400 mAh` at `389` cycles, and **Moto G75 5G** — `5169 / 5111 mAh` at `313` cycles)
* **Live Charging Telemetry:** Real-time power (Watts), negotiated USB charger voltage (V), session peak Watts, current (mA), and battery temperature (°C)
* **Bilingual UI:** Automatically switches between **English** and **Romanian** based on system language
* **100% Offline & Transparent:** Requests **ZERO internet permissions** (`AndroidManifest.xml` only uses `BATTERY_STATS` and the local `ShizukuProvider`)

### How to Use (No Root Required)
1. Install and start **[Shizuku](https://rikka.app)** on your phone (pairs in 30 seconds via *Developer Options → Wireless Debugging*).
2. Download **`VoltPulse-v1.0.apk`** from the **[Releases](../../releases)** page and install it.
3. Open **VoltPulse**, tap the **"MOTOROLA HEALTH HAL (SHIZUKU)"** card at the bottom, and tap **Allow all the time**.

### Build From Source
1. Create an **Empty Activity (Jetpack Compose)** project in Android Studio (`minSdk = 34`).
2. Add `dev.rikka.shizuku:api:13.1.5` and `dev.rikka.shizuku:provider:13.1.5` to `build.gradle.kts`.
3. Copy `AndroidManifest.xml` and `MainActivity.kt` from this repository and build. <img width="576" height="1280" alt="Edge 30 Fusion 2026-11-22_19-40-07" src="https://github.com/user-attachments/assets/9eb497bc-07c3-4ede-8226-10fffff198d0" />

