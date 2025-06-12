# 🚘 SpeedLimitDetector

**SpeedLimitDetector** is an Android app that uses the camera and GPS to detect speed limit signs and the "yield" traffic sign. It alerts the driver with a sound notification when they exceed the allowed speed or approach a yield sign.

---

## ✨ Features

- 📷 Detects **speed limit signs** using the camera and displays them on screen.
- ⚠️ Detects the **yield ("give way") sign** and warns the driver.
- 🚗 Compares the detected sign with the current **GPS speed** (in km/h).
- 🔊 **Plays a sound alert** when the user exceeds the speed limit.
- 🎥 Shows **camera frames per second (FPS)** in the top-left corner to indicate app performance on the current device.
- ⚙️ Allows adjustment of **detection confidence**, number of signs processed, and camera positioning.

---

## 🛠️ Technologies Used

- Kotlin  
- AndroidX  
- ML Kit  
- TensorFlow Lite  

---

## 🧰 Requirements & Development Environment

- ✅ **minSdk**: 28  
- ✅ **targetSdk**: 35  
- ✅ **compileSdk**: 35  
- ✅ **Gradle**: 8.2.0  
- ✅ Compatible with Android Studio Hedgehog (2023.1.1) and Meerkat (2024.3.2)

---

## 📱 Tested Devices

| Device                  | Average FPS | Notes                                |
|-------------------------|------------:|--------------------------------------|
| Samsung Galaxy S22      |     10–15   | Works well, set `signs count = 1`    |
| Samsung Galaxy S24 Plus |     18–22   | Excellent performance, set `signs count = 3` |

---

## 🧠 User Tips & Configuration

1. **Camera position matters** – Adjust it by swiping on the preview screen in settings.
2. **Detection confidence** – Best results are usually between **70% and 85%**.
3. **Number of signs processed** – Depends on your device:
   - Weaker devices: use `1`
   - Stronger devices: use `2–5`
4. **FPS under 10** – On low-end phones, performance may be too slow to be useful.
5. **Weather & lighting** – Performance drops significantly in rainy weather or when facing direct sunlight.

---

## 🚀 Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/YourRepo/SpeedLimitDetector.git
   ```
2. Open the project in **Android Studio**.
3. Ensure SDK versions and Gradle settings match the above requirements.
4. Run the app on a real Android device (API level 28+).
5. Grant camera and location permissions when prompted.

---

## 📸 Screenshots

> *(Add screenshots of the camera view, detected signs, alerts, and FPS overlay here.)*

---

## 👤 Author

> *(Add your GitHub, LinkedIn, or email here when ready.)*  
📫 [Contact info here]

---

## 🧪 Project Status

This is a side project that is **occasionally updated** as new ideas arise. Expect occasional improvements and optimizations.

---

## 🛣️ Future Ideas (optional)

> *(Here you could list plans like recognizing additional traffic signs, integrating with maps, or adding HUD projection.)*
