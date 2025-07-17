
# 🔐 Trinetra AI – Intelligent Crime Mapping & Predictive Policing

[![MIT License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**Trinetra AI** is a prototype mobile application designed to revolutionize urban policing through smart crime pattern detection, zone-based prediction, and patrol optimization. Built for law enforcement use, the app leverages machine learning, FIR analytics, and zone-based heatmaps to aid safer and smarter decision-making.

> 🚨 **Note**: This app uses **dummy FIR data for New Delhi**, with 60 virtual zones for demonstration only. No real FIRs or personal data are involved.

---

## 🚨 Problem Overview

Traditional policing struggles with:

- Unstructured and scattered FIR data  
- Unoptimized and manual patrol planning  
- Lack of predictive insights from crime trends  

These issues lead to **delayed responses**, **inefficient resource deployment**, and **reduced public safety**.

---

## ✅ Proposed Solution

Trinetra AI addresses these gaps through:

- FIR data analysis to detect crime patterns  
- Geospatial crime heatmaps to visualize hotspots  
- Predictive ML model to suggest future crime-prone areas  
- Smart patrol route generation based on severity and history  
- Women safety matrix and risk zone detection  

---

## 🧠 Key Features

### 🔴 **Crime Heatmap Generator**
- Visualize high-crime areas using filters:
  - Crime type 
  - Date range
  - Time slot
- Expandable map view for detailed zone analysis

### 📈 **ML-Powered Crime Trend Analyzer**
- Predict crime type and potential zones
- Uses past FIR patterns and trained classification model
- Displays last analyzed trends with bar charts

### 🛣️ **Smart Patrol Route Generator**
- Generates shift-based and AI-predicted patrol routes
- View history-based route maps
- Expand and view patrol route details for all 60 zones

### 👩‍🦰 **Women Safety Module**
- Auto-detection of FIRs under sections like 354, 376
- Highlights zones with >5 women-related FIRs
- Matrix-style visualization of risk intensity by zone

---

## 🛡️ Access & Authentication

- **Restricted Access**: Only authorized police users can register  
- **Role-Based Dashboard**: Admin vs Field Officers  
- **Secure FIR Management**: Add/Edit FIRs, Patrol Access only for verified users

---

## 🗂️ Additional Features

- Add & view detailed FIR cases  
- FIR list filters by zone, category, time  
- FIR count visualized zone-wise  
- User profile, app settings  

---

## 📱 App Screens Preview

| Crime Heatmap | Crime Trend Analyser | Patrol Planner | Women Safety |
|---------------|--------------------|----------------|--------------|
| ![](https://github.com/user-attachments/assets/c4cf2a8d-dabc-451a-84ee-59fde56ffb33) | ![](https://github.com/user-attachments/assets/2999cf4c-7201-471b-a45a-b9b937e01815) | ![](https://github.com/user-attachments/assets/0b9662ef-a325-4f93-ae9b-9d9eab3c2558) | ![](https://github.com/user-attachments/assets/3a0bda25-6882-44d2-b4d2-77020d3c812a) |



  <img src="https://github.com/user-attachments/assets/2999cf4c-7201-471b-a45a-b9b937e01815" width="300"/>
  <img src="https://github.com/user-attachments/assets/e369fa35-6ff5-4daf-b6c3-61aa4b205b65" width="300"/>

  <img src="https://github.com/user-attachments/assets/2518ea40-cde1-4e53-9c5d-5a44096a03ac" width="300"/>
  <img src="https://github.com/user-attachments/assets/6fa88faf-88f9-4dbe-92af-a076468e4d1a" width="300"/>
  <img src="https://github.com/user-attachments/assets/c77f2bf0-1ef9-4b6e-a5c7-0dfadd842c6a" width="300"/>
  <img src="https://github.com/user-attachments/assets/cae13d6b-ce25-4abc-89a9-6765919c871d" width="300"/>


---

## ⚙️ Tech Stack

- **Android (Kotlin + XML)** – Mobile UI  
- **Google Maps SDK** – Visualizations  
- **Firebase Firestore** – FIR & zone data backend  
- **Firebase Realtime Database** – Patrol system updates  
- **Firebase Auth** – Role-based login  
- **ML Model** – Crime prediction classifier  
- **Flask API** – Backend ML predictions  

---


## 📊 Data Disclaimer

> **This app is a research prototype.**  
> - FIR data is completely dummy and auto-generated.  
> - Delhi is divided into **60 virtual zones** for analysis.  
> - No connection to any live law enforcement or surveillance database.  
> - ML models trained on synthetic data mimicking realistic crime patterns.  

---

## 🧪 Steps to Run the App

Follow these steps to set up and run the Trinetra AI app locally:

1. **Clone the Repository**
   ```bash
   git clone https://github.com/yashendra767/Trinetra-AI.git
   cd Trinetra-AI
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Open the cloned project

3. **Firebase Setup**
   - Add your `google-services.json` file to the `/app` directory
   - Enable Firebase Firestore, Realtime Database, and Authentication from the Firebase Console

4. **Google Maps Integration**
   - Enable Google Maps SDK for Android in your Firebase project
   - Add your Maps API key to the `local.properties` or `AndroidManifest.xml`

5. **Build & Run**
   - Connect a physical Android device or emulator with Maps enabled
   - Sync Gradle and Run the app

---

## 🤝 Contributing

We welcome contributions from developers, designers, and domain experts.

### Ways to Contribute:
- 💡 Improve UI/UX design
- 🔍 Optimize ML prediction logic
- 🔌 Add real-time datasets or APIs
- 🐞 Report bugs or suggest new features

### To Contribute:
1. Fork the repository
2. Create your feature branch: `git checkout -b feature-name`
3. Commit your changes: `git commit -m "Add feature"`
4. Push to the branch: `git push origin feature-name`
5. Open a Pull Request

---



## 👨‍💻 Team

Built by:

- **Aryan Sharma** — [LinkedIn](https://www.linkedin.com/in/aryan-sharma-26276131a)  
- **Yashendra Awasthi** — [LinkedIn](https://www.linkedin.com/in/yashendra-awasthi-5800772b8)  
- **Aayush Nagar** — [LinkedIn](https://www.linkedin.com/in/aayush-nagar-gujjar-73a96131a)  
- **Happy Saxena** — [LinkedIn](https://www.linkedin.com/in/happy-saxena)

---

## 📬 Contact

📧 Aryan → aryan180906@gmail.com  
📧 Yashendra → yashendraawasthi@gmail.com  
📧 Aayush → aayushnagargujjar@gmail.com
📧 Happy → 24je0622@iitism.ac.in


GitHub Project: [Trinetra-AI](https://github.com/yashendra767/Trinetra-AI)  


---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
