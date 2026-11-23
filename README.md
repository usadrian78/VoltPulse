# VOLT // PULSE (`ro.us.adrian`)

A lightweight, AMOLED Pure Black battery telemetry, health, and charge cycle monitor built specifically for **Motorola phones running Android 14+** (no root required).

### Screenshots (`v1.1`)

| Motorola Edge 30 Fusion (`68W TurboPower`) | Moto G75 5G (`30W Fast Charge`) |
| :---: | :---: |
| <img width="280" alt="Edge 30 Fusion 68W" src="https://github.com/user-attachments/assets/419a583c-85d1-468b-9154-d430feeeea0d" /> | <img width="280" alt="Moto G75 5G 30W" src="https://github.com/user-attachments/assets/2adb678d-3978-4654-a747-91674d78349d" /> |

### Key Features
* **Exact Charge Cycle Count & Cell Wear %** (read directly from Motorola's proprietary `IMotHealth` / `dumpsys battery` service)
* **Real Full Capacity vs. Design Capacity** (tested on **Motorola Edge 30 Fusion** — `4213 / 4400 mAh` at `390` cycles, and **Moto G75 5G** — `5162 / 5111 mAh` at `313` cycles)
* **Real Charger Power Detection (`v1.1`):** Displays negotiated USB charger wattage (`TURBOPOWER 68W`, `FAST CHARGE 30W`) alongside live cell intake power, USB voltage (V), and battery temperature (°C)
* **Bilingual UI:** Automatically switches between **English** and **Romanian** based on system language
* **100% Offline & Transparent:** Requests **ZERO internet permissions** (`AndroidManifest.xml` only uses `BATTERY_STATS` and the local `ShizukuProvider`)

### How to Use (No Root Required)
1. Install and start **[Shizuku](https://rikka.app)** on your phone (pairs in 30 seconds via *Developer Options → Wireless Debugging*).
2. Download **`VoltPulse-v1.1.apk`** from the **[Releases](../../releases)** page and install it.
3. Open **VoltPulse**, tap the **"MOTOROLA HEALTH HAL (SHIZUKU)"** card at the bottom, and tap **Allow all the time**.

### Build From Source
1. Create an **Empty Activity (Jetpack Compose)** project in Android Studio (`minSdk = 34`).
2. Add `dev.rikka.shizuku:api:13.1.5` and `dev.rikka.shizuku:provider:13.1.5` to `build.gradle.kts`.
3. Copy `AndroidManifest.xml` and `MainActivity.kt` from this repository and build.
