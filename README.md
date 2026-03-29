# MD-Buildings JOSM Plugin V2 🇲🇩

[English](README.md) | [Română/Moldovenească](README_RO.md) | [Русский](README_RU.md)


[![JOSM Plugin](https://img.shields.io/badge/JOSM-Plugin-blue.svg)](https://josm.openstreetmap.de/wiki/Plugins)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-red.svg)](LICENSE)
[![Author: Oni_DOS](https://img.shields.io/badge/Author-Oni__DOS-green.svg)](#)
[![Original Author: Paweł Raszuk](https://img.shields.io/badge/Original_Author-Paweł_Raszuk-green.svg)](#)
[![VirusTotal Scan](https://img.shields.io/badge/VirusTotal-Checked%20%E2%9C%94-brightgreen?logo=virustotal&logoColor=white)](https://github.com/Oni-DOS/josm-mdbuildings-plugin/blob/main/.github/virustotal/latest_scan_url.txt)

A high-performance **JOSM** plugin designed for the precision import and synchronization of building data from Moldovan public datasets. **Version 2** is a restructured, lean implementation focusing on core stability and essential features.

---

## 📋 Table of Contents

- [Key Improvements in V2](#-key-improvements-in-v2)
- [Features Overview](#-features-overview)
- [Build & Installation](#-build--installation)
- [Quick Start](#-quick-start)
- [Detailed Usage Guide](#-detailed-usage-guide)
- [Preferences & Configuration](#-preferences--configuration)
- [Data Sources](#-data-sources)
- [Tips & Best Practices](#-tips--best-practices)
- [Troubleshooting](#-troubleshooting)
- [Repository Maintenance](#-repository-maintenance)
- [License](#-license)

---

## 🚀 Key Improvements in V2
- **Lean Codebase**: Remapped and refactored to include only strictly necessary components.
- **Independent Build**: Fully standalone Gradle project within its own directory.
- **Improved Performance**: Optimized data handling and UI response times.
- **Stabilized V2 Interface**: Updated naming and version tracking for better clarity.

---

## 🛠 Build & Installation

### Requirements
- Java 11 JDK
- JOSM (latest stable)

### Build from Source
To build the plugin independently, navigate to this directory and run:
```bash
export JAVA_HOME=/path/to/java-11
./gradlew build
```
The generated JAR will be located at `build/libs/mdbuildings-v2-2.0.0-SNAPSHOT.jar`.

### Manual Installation
1. Copy the generated `.jar` file to your JOSM plugins directory:
   - **Linux/macOS**: `~/.local/share/JOSM/plugins/`
   - **Windows**: `%APPDATA%\JOSM\plugins\`
2. Restart JOSM.

---

## ⚡ Quick Start

1. Open JOSM and load the area you want to map.
2. **Download OSM data** for that area (`Ctrl+Shift+Down`).
3. Open the **MD-Buildings sidebar** by clicking the Red Aurochs icon in the left panel.
4. Select your desired **profile** (e.g. `GEODATA (Roofs)` or `CADASTRU (Buildings)`).

#### Import New Building
1.  Navigate to your target area on the map.
2.  Press **Ctrl + Shift + 1** to download and import buildings from the active data source.
3.  New buildings will be added to the active layer with default tags.

#### Replace/Update Existing Building
1.  Select an existing building way (optional, but recommended).
2.  **Trigger Type A (Selection)**: With a building way selected, press **Ctrl + Shift + 1**.
3.  **Trigger Type B (Spatial Click)**: Hold **Ctrl** and **Double Click** inside the perimeter of an existing building way.
4.  The plugin will match the existing building, update its geometry, and sync tags according to your selected profile.

---

## 📖 Features Overview

| Feature | Description |
|---|---|
| **Direct WFS Import** | Fetches building footprints live from Moldovan public Geoservers (GEODATA, CADASTRU). |
| **Intelligent Merging** | Automatically translates CADASTRU house numbers and merges interior address nodes. |
| **Height Calculation** | Uses DTM API to calculate building height based on roof elevation and ground level. |
| **Overlap Detection** | Prevents duplicate imports and manages spatial conflicts with existing OSM data. |
| **3 Import Modes** | Full import, geometry-only update, or tags-only update. |
| **Tag Conflict Resolution** | Resolves tag conflicts between the downloaded data and existing OSM objects. |
| **Lifecycle Prefix Check** | Warns if building tags contain lifecycle prefixes (e.g. `demolished:building`). |
| **Uncommon Tags Check** | Highlights non-standard tags that may require manual review. |

---

## 📖 Detailed Usage Guide

### 1. Triggering a Building Import

**Trigger Type A (Selection)**:
- Select an existing building way in JOSM.
- Press **Ctrl + Shift + 1**. The plugin will attempt to update the selected building.

**Trigger Type B (Spatial Click)**:
- Hold **Ctrl** and **double-click** the left mouse button anywhere on the map view.
- The plugin will query the Geoserver for the nearest building at your cursor position. If an existing OSM building is under the cursor, it will attempt to update it.

---

### 2. Import Modes

The plugin supports three distinct import strategies, configurable from the **Toggle Dialog sidebar** or **Preferences**:

| Mode | What It Does |
|---|---|
| **Full Import** | Imports both the geometry (shape) and all tags from the source data. If there's a matching building nearby, it replaces it entirely (geometry + tags). |
| **Geometry Update** | Updates only the *shape* of the selected existing building, leaving all existing OSM tags untouched. Ideal when a building boundary has changed. |
| **Tags Update** | Updates only the *tags* of the selected existing building, leaving its geometry untouched. Useful when you know the footprint is correct but tags are outdated. |

> [!TIP]
> Use **Full Import** when mapping new buildings. Use **Geometry Update** or **Tags Update** when improving already-mapped buildings.

---

### 3. Toggle Dialog Sidebar

Click the **MD-Buildings** icon (Red Aurochs logo) in the left JOSM panel to open the sidebar.

The sidebar provides:
- **Profile Selector** — choose your active data source profile (e.g. `GEODATA (Roofs)`, `CADASTRU (Buildings)`).
- **Import Mode Selector** — switch between Full, Geometry, and Tags modes.
- **Status Display** — shows the result of the last import (`DONE`, `NO_DATA`, `CONNECTION_ERROR`, etc.).
- **Quick Stats** — total buildings imported in this session.

---

### 4. Data Sources & Profiles

The plugin connects to two official Moldovan Geoservers out of the box:

| Server | URL | Description |
|---|---|---|
| **GEODATA** | `https://geodata.gov.md/geoserver/maps/wfs` | National geoportal — provides roof-level building footprints |
| **CADASTRU** | `https://cadastru.md/geoserver/w_cbi/wfs` | Cadastral registry — provides building outlines with house numbers |

**Profiles** define which WFS layer to use and what tags to import/exclude:

| Profile | Server | Layer | Tags Included | Notes |
|---|---|---|---|---|
| **GEODATA (Roofs)** | GEODATA | `maps:lm17_area_roof_area` | All except `layer_name`, `layer`, `medium`, `source:building` | Best for roof geometry |
| **CADASTRU (Buildings)** | CADASTRU | `w_cbi:cad_cladiri` | Only `NRCASA` (translated to `addr:housenumber`) | Best for address data |

You can add, edit, or remove servers and profiles in **Preferences → MD-BUILDINGS → Data Sources**.

---

### 5. CADASTRU Profile & NRCASA Tag

When using the **CADASTRU (Buildings)** profile:

- The plugin filters all tags from the WFS response, keeping **only `NRCASA`** (the Moldovan cadastral house number field).
- The `NRCASA` tag is **automatically renamed** to `addr:housenumber` to comply with the OpenStreetMap tagging standard.
- The `building=yes` and `source=AGCC/Linemap2017` tags are always added automatically.

---

### 6. Interior Address Node Merging

This feature automatically finds **existing OSM nodes** located *inside* the boundary of the newly imported building and **transfers their `addr:*` tags** directly onto the building.

**Enabling this feature:**
1. Open **Preferences** (`F12`) → **MD-BUILDINGS** → **Data Sources** tab.
2. Tick the checkbox: **"Merge interior address nodes into building"**.
3. Click OK and start importing.

> [!NOTE]
> This feature is **disabled by default**. Enable it only when working in areas where address nodes already exist in OSM and you want to consolidate them onto building polygons.

---

### 7. Tag Conflict Resolution

When a building already exists in OSM and the plugin imports newer data from the Geoserver, tag conflicts may arise. The plugin handles these automatically with the following logic:

- **Building tag** (`building=*`): The existing value is preserved if it's more specific than `yes` (e.g. `building=residential` is kept instead of being overwritten with `building=yes`).
- **All other tags**: New tags from the Geoserver are applied on top of existing tags. Existing tags not present in the new data are generally preserved.

---

### 8. Automated Height Calculation

When a building footprint from the **GEODATA** source includes a `z` attribute (absolute elevation of the roof), the plugin automatically:

1. Queries a **DTM (Digital Terrain Model) API** to get the ground elevation at the building's center point.
2. Calculates the **relative height** as: `height = z_roof - z_ground`.
3. Rounds the result to the nearest 0.1 meter.
4. Applies the `height=<value>` tag to the building.

This gives you accurate, real-world building heights without any manual calculation.

---

## ⚙️ Preferences & Configuration

Open Preferences with `F12`, then navigate to the **MD-BUILDINGS** section. The preferences window has the following tabs:

- **Data Sources Tab**: Manage WFS Geoserver connections and import profiles.
- **Stats Tab**: View and reset your import statistics.
- **Notifications Tab**: Configure which notifications and alerts are shown.
- **Uncommon Tags Tab**: Manage the list of "common" building tag values.
- **Auto-remove Source Tags Tab**: Manage `source` tag values automatically removed during updates.

---

## 🗄 Data Sources

### GEODATA — National Geoportal
- **URL**: `https://geodata.gov.md/geoserver/maps/wfs`  
- **Layer**: `maps:lm17_area_roof_area`  
- **Best for**: Geometry accuracy, height calculation, general building shapes  

### CADASTRU — Cadastral Registry  
- **URL**: `https://cadastru.md/geoserver/w_cbi/wfs`  
- **Layer**: `w_cbi:cad_cladiri`  
- **Best for**: House numbers (`addr:housenumber`), legal building boundaries  

---

## 💡 Tips & Best Practices

1. **Use GEODATA for geometry, CADASTRU for addresses.**
2. **Enable "Merge interior address nodes"** in urban areas with existing address nodes.
3. **Always check the status indicator** in the sidebar after each import (`DONE`, `NO_DATA`, etc.).
4. **Use "Geometry Update" mode** when footprints change but tags should be preserved.
5. **Review the uncommon tags dialog carefully.** It catches incorrect data from the source.

---

## 🛠 Troubleshooting

### JOSM Help Browser Crash
If JOSM crashes when you open the help menu on Java 16+, add this flag to your JOSM execution command:
```bash
--add-opens java.desktop/javax.swing.text.html=ALL-UNNAMED
```

---

## 🧹 Repository Maintenance

To keep the repository lean and easier to maintain:
- Test fixtures are stored under `test/data/` and consumed directly by automated tests.
- Legacy scratch files and duplicated test copies should not be kept in separate top-level folders.

---

## 📜 License

This project is licensed under the **GNU General Public License v3.0** — see the [LICENSE](LICENSE) file for full details.

---

**Developed with ❤️ for the Moldovan OpenStreetMap Community.**  
*Based on the original [PL-Buildings plugin](https://github.com/praszuk/josm-plbuildings-plugin) by Paweł Raszuk.*

For more details, visit the [Official Repository](https://github.com/Oni-DOS/josm-mdbuildings-plugin).
