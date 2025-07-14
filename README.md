# Trinetra-AI: Intelligent Crime Mapping and Predictive Policing Platform

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**Trinetra-AI** is a smart crime-mapping and behavioral analysis platform that visualizes, predicts, and assists law enforcement in planning patrols and identifying high-risk zones. Developed as part of a smart policing initiative, the system processes FIR and public datasets to generate real-time crime heatmaps, patrol routes, women safety areas and risk assessments.
The current version uses dummy FIR and zone data from New Delhi and is designed to scale seamlessly when nourished with real time and larger datasets.
---

**Trinetra AI - Crime Mapping**

Trinetra-AI aims to provide:

- A visual interface linking FIR data to generate dynamic hotspots depending on the crime type.
- Color-coded heatmaps based on parameters like crime category, date, time, and severity.
- Behavior pattern analysis of criminals using past FIRs and seasonal trends.
- Automatic patrol route generation based on historical crime patterns and severity.
- Special emphasis on women safety through predictive patrol in vulnerable zones.

---

## 🔍 Key Features

- 🔴 **Crime Heatmaps**: Real-time mapping of crime zones filtered by IPC/NDPS/Arms Act sections.
- 🔄 **Dynamic Filters**: View crime hotspots based on date, time, season, and location.
- 🧠 **Behavioral Analysis**: Predict repeat offense patterns and seasonal crime surges.
- 🚔 **Auto Patrol Routing**: Generates gasht routes for seasonal and shift-based patrolling.
- 🚨 **Women Safety Zones**: Identify and monitor vulnerable areas based on past FIR data.
- 🤖 **Dedicated Systems**: Dedicated patrol route suggestions to ensure women safety in high risk zones.
- 🔒 **Role-Based Interface**: Tailored dashboards for Police Admin, Patrol Unit.

---

## 🛠️ Tech Stack

- **Android (Kotlin + XML)** — Mobile interface for law enforcement teams
- **Google Maps SDK** — Heatmap and route visualization
- **Firebase Firestore** — Real-time backend database for FIRs, alerts, and users
- **Firebase Cloud Messaging** - Real time notifications for downloading the data of zones and FIRs
- **GeoSpatial Clustering** — Custom zone creation for intelligent grouping
- **ML Module (WIP)** — Crime type prediction and seasonal behavior modeling
- **Firebase Authentication** — Sign up using seamless firebase authentication
- **Firebase Realtime Database** — For live updates in patrol/route system
- **Custom Flask API** — AI-based crime prediction and patrol zone analysis
 
---

## 🧪 Crime Categories Supported

Mapped to these acts:

- **Indian Penal Code**: Sections 121, 141, 144, 146–148, 151, 153-A, 302, 304-B, 307, 354, 376, 498-A, etc.
- **NDPS Act**: Sections 20–22
- **Arms Act**: Section 25
- **Excise Act**: Sections 60, 60(2), 72
- **SC/ST Act**, **Gambling**, **Cow Protection**, **Goonda Act**, etc.

---

## 🧭 Modules Overview

- **FIR Parser**: Imports dummy or real FIRs and maps crimes to zones
- **Hotspot Generator**: Clusters FIRs to define severity zones
- **Patrol Planner**: Suggests balanced patrolling routes across crime-heavy zones
- **Women Safety Mode**: Dedicated tab with alerts and pink-patrol suggestions
- **Admin Panel**: Controls user access, assigns zones, and monitors activity

---

## 🖥️ Screenshots 

- Crime Zone Heatmap
  ![IMG-20250714-WA0007](https://github.com/user-attachments/assets/c4cf2a8d-dabc-451a-84ee-59fde56ffb33)
- FIR List and Crime Classification
  ![IMG-20250714-WA0005](https://github.com/user-attachments/assets/2999cf4c-7201-471b-a45a-b9b937e01815)
  ![IMG-20250714-WA0012](https://github.com/user-attachments/assets/e369fa35-6ff5-4daf-b6c3-61aa4b205b65)
- Patrol Route Generator
  ![IMG-20250714-WA0006](https://github.com/user-attachments/assets/0b9662ef-a325-4f93-ae9b-9d9eab3c2558)
- Women Safety
  ![IMG-20250714-WA0004](https://github.com/user-attachments/assets/3a0bda25-6882-44d2-b4d2-77020d3c812a)
- Other functions
  ![IMG-20250714-WA0011](https://github.com/user-attachments/assets/2518ea40-cde1-4e53-9c5d-5a44096a03ac)
  ![IMG-20250714-WA0010](https://github.com/user-attachments/assets/6fa88faf-88f9-4dbe-92af-a076468e4d1a)
  ![IMG-20250714-WA0009](https://github.com/user-attachments/assets/c77f2bf0-1ef9-4b6e-a5c7-0dfadd842c6a)
  ![IMG-20250714-WA0008](https://github.com/user-attachments/assets/cae13d6b-ce25-4abc-89a9-6765919c871d)

---

## 🚀 Getting Started

1. Clone this repo:

```bash
git clone https://github.com/yashendra767/Trinetra-AI.git
cd Trinetra-AI
```

2. Open in Android Studio.
3. Connect Firebase and enable Maps API key.
4. Run on Android Emulator or physical device with Maps access.

---

## 📂 Data Sources

- **Dummy FIR Data** (simulated)

---

## 🤝 Contribution

We welcome law enforcement personnel, developers, and researchers to contribute. Submit issues or PRs for:

- New data integrations
- Real time dataset (of Govt.)
- Better classification algorithms
- UI improvements

---

## 📄 License

MIT License - see [`LICENSE`](LICENSE) file for details.

---

## 🙋 Contact

Developed with ❤️ by **Yashendra Awasthi**, **Aayush Gujjar**, **Aryan Sharma**
GitHub: [@yashendra767](https://github.com/yashendra767)
Email: Aryan-> [aryan180906@gmail.com]
       Aayush-> [aayushnagargujjar@gmail.com]
       Yashendra-> [yashendraawasthi@gmail.com]

Open for collaboration with police forces, gov bodies, or researchers in smart policing and public safety AI.
