# Pluginul JOSM MD-Buildings V2 🇲🇩

[🇬🇧](README.md) | [🇲🇩](README_RO.md) | [🇷🇺](README_RU.md)

[![JOSM Plugin](https://img.shields.io/badge/JOSM-Plugin-blue.svg)](https://josm.openstreetmap.de/wiki/Plugins)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-red.svg)](LICENSE)
[![Author: Oni_DOS](https://img.shields.io/badge/Author-Oni__DOS-green.svg)](#)
[![Original Author: Paweł Raszuk](https://img.shields.io/badge/Original_Author-Paweł_Raszuk-green.svg)](#)
[![VirusTotal Scan](https://img.shields.io/badge/VirusTotal-Checked%20%E2%9C%94-brightgreen?logo=virustotal&logoColor=white)](https://github.com/Oni-DOS/josm-mdbuildings-plugin/blob/main/.github/virustotal/latest_scan_url.txt)

Un plugin **JOSM** de înaltă performanță proiectat pentru importul și sincronizarea precisă a datelor despre clădiri din seturile de date publice moldovenești. **Versiunea 2** este o implementare restructurată, simplificată, axată pe stabilitatea nucleului și pe caracteristicile esențiale.

---

## 📋 Cuprins

- [Îmbunătățiri cheie în V2](#-îmbunătățiri-cheie-în-v2)
- [Prezentare generală a funcțiilor](#-prezentare-generală-a-funcțiilor)
- [Construire și instalare](#-construire-și-instalare)
- [Pornire rapidă](#-pornire-rapidă)
- [Ghid de utilizare detaliat](#-ghid-de-utilare-detaliat)
- [Preferințe și configurare](#-preferințe-și-configurare)
- [Surse de date](#-surse-de-date)
- [Sfaturi și bune practici](#-sfaturi-și-bune-practici)
- [Depanare](#-depanare)
- [Întreținerea depozitului](#-întreținerea-depozitului)
- [Licență](#-licență)

---

## 🚀 Îmbunătățiri cheie în V2
- **Cod simplificat**: Remapat și refăcut pentru a include doar componentele strict necesare.
- **Construire independentă**: Proiect Gradle complet de sine stătător în propriul său director.
- **Performanță îmbunătățită**: Gestionare optimizată a datelor și timpi de răspuns ai UI îmbunătățiți.
- **Interfață V2 stabilizată**: Denumire și urmărire a versiunilor actualizate pentru o mai bună claritate.

---

## 🛠 Construire și instalare

### Cerințe
- Java 11 JDK
- JOSM (ultima versiune stabilă)

### Construire din sursă
Pentru a construi pluginul independent, navigați în acest director și rulați:
```bash
export JAVA_HOME=/calea/catre/java-11
./gradlew build
```
JAR-ul generat va fi situat la `build/libs/mdbuildings-v2-2.0.0-SNAPSHOT.jar`.

### Instalare manuală
1. Copiați fișierul `.jar` generat în directorul de pluginuri JOSM:
   - **Linux/macOS**: `~/.local/share/JOSM/plugins/`
   - **Windows**: `%APPDATA%\JOSM\plugins\`
2. Reporniți JOSM.

---

## ⚡ Pornire rapidă și flux de lucru pas cu pas

1. Deschideți JOSM și încărcați zona pe care doriți să o mapați.
2. **Descărcați datele OSM** pentru acea zonă (`Ctrl+Shift+Down`).
3. Deschideți **panoul lateral MD-Buildings** făcând clic pe pictograma Red Aurochs din panoul din stânga.
4. Selectați **profilul** dorit (de exemplu, `GEODATA (Roofs)` sau `CADASTRU (Buildings)`).

### 🛠 Flux de lucru ideal de mapare

Pentru a profita la maximum de sistemul cu sursă dublă al pluginului, urmați acest proces optim pas cu pas pentru o sesiune obișnuită de cartografiere:

#### Pasul 1: Geometrie și Înălțime (GEODATA)
1. În panoul lateral, selectați profilul **`GEODATA (Roofs)`**. Setați modul pe **Import complet** (Full Import).
2. Găsiți o clădire nemapată pe imaginile din satelit.
3. Plasați cursorul mouse-ului în interiorul amprentei clădirii și faceți **Ctrl + Dublu clic** (2 clicuri stânga).
4. Pluginul va prelua conturul precis al acoperișului și va calcula automat înălțimea reală a clădirii prin intermediul API-ului DTM.

#### Pasul 2: Date de Adresă (CADASTRU)
1. Acum, comutați profilul din panoul lateral pe **`CADASTRU (Buildings)`**. 
2. Schimbați modul de import la **Actualizare etichete** (Tags Update) (acest lucru va păstra geometria exactă pe care tocmai ați importat-o la Pasul 1).
3. Plasați cursorul mouse-ului în interiorul noii clădiri și faceți **Ctrl + Dublu clic**.
4. Pluginul va interoga registrul cadastral, va igieniza numele orașului și ale străzilor (eliminând prefixele ca mun./or. etc.), va corecta orice diacritice românești greșite, și va injecta etichetele `addr:*` direct pe clădire.

#### Pasul 3: Rezolvarea conflictelor și Validare
1. Dacă actualizați o zonă deja mapată ce conține etichete învechite, poate apărea dialogul de conflict al etichetelor (Tag Conflict Dialog). Selectați datele sursă cele mai exacte pentru a le fuziona.
2. Atenție la dialogul de avertizare "Etichete Neobișnuite" (Uncommon Tags) care identifică datele greșite existente în OpenStreetMap.
3. Rulați JOSM Validator, verificați geometria obținută și faceți clic pe **Încărcare** (Upload)!

---

## 📖 Prezentare generală a funcțiilor

| Funcție | Descriere |
|---|---|
| **Import direct WFS** | Preluare în timp real a amprentelor clădirilor de pe Geoserverele publice moldovenești (GEODATA, CADASTRU). |
| **Fuziune inteligentă** | Traduce automat numerele de casă CADASTRU și fuzionează nodurile de adresă interioare. |
| **Sanitizare adrese** | Normalizează diacriticele românești (Ş/Ţ în Ș/Ț) și elimină prefixele localităților (mun., or.) pentru valori impecabile ale etichetelor. |
| **Calcul avansat al înălțimii** | Interogare stratificată DTM cu rezervă (fallback) pentru a calcula înălțimile clădirilor, cu posibilitatea de activare/dezactivare. Elimină automat etichetele `z=*` brute dacă DTM este omis. |
| **Detectarea suprapunerilor** | Previne importurile duplicate și gestionează conflictele spațiale cu datele OSM existente. |
| **3 moduri de import** | Import complet, actualizare doar geometrie sau actualizare doar etichete. |
| **Rezoluția conflictelor de etichete** | Rezolvă conflictele de etichete între datele descărcate și obiectele OSM existente. |
| **Verificarea prefixului de ciclu de viață** | Avertizează dacă etichetele clădirii conțin prefixe de ciclu de viață (ex. `demolished:building`). |
| **Verificarea etichetelor neobișnuite** | Evidențiază etichetele non-standard care pot necesita revizuire manuală. |

---

## 📖 Ghid de utilizare detaliat

### 1. Declanșarea unui import de clădire

- Mențineți apăsată tasta **Ctrl** și **faceți dublu clic** (butonul stâng al mouse-ului) oriunde pe vizualizarea hărții.
- Pluginul va interoga Geoserverul pentru cea mai apropiată clădire de poziția cursorului. 
- Dacă există o clădire OSM sub cursor, va încerca să o actualizeze conform modului de import activ.

---

### 2. Moduri de import

Pluginul acceptă trei strategii de import distincte, configurabile din **panoul lateral** sau din **Preferințe**:

| Mod | Ce face |
|---|---|
| **Import complet** | Importă atât geometria (forma), cât și toate etichetele din sursa de date. Dacă există o clădire potrivită în apropiere, o înlocuiește complet (geometrie + etichete). |
| **Actualizare geometrie** | Actualizează doar *forma* clădirii existente selectate, lăsând toate etichetele OSM existente neschimbate. Ideal când conturul unei clădiri s-a schimbat. |
| **Actualizare etichete** | Actualizează doar *etichetele* clădirii existente selectate, lăsând geometria neschimbată. Util când știți că amprenta este corectă, dar etichetele sunt învechite. |

> [!TIP]
> Utilizați **Import complet** când mapați clădiri noi. Utilizați **Actualizare geometrie** sau **Actualizare etichete** când îmbunătățiți clădiri deja mapate.

---

### 3. Panoul lateral (Toggle Dialog)

Faceți clic pe pictograma **MD-Buildings** (logo-ul Red Aurochs) din panoul din stânga JOSM pentru a deschide panoul lateral.

Panoul lateral oferă:
- **Selector de profil** — alegeți profilul sursei de date active (ex. `GEODATA (Roofs)`, `CADASTRU (Buildings)`).
- **Selector mod de import** — comutați între modurile Complet, Geometrie și Etichete.
- **Afișare stare** — arată rezultatul ultimului import (`DONE`, `NO_DATA`, `CONNECTION_ERROR` etc.).
- **Statistici rapide** — numărul total de clădiri importate în această sesiune.

---

### 4. Surse de date și profiluri

Pluginul se conectează direct la două Geoservere oficiale moldovenești:

| Server | URL | Descriere |
|---|---|---|
| **GEODATA** | `https://geodata.gov.md/geoserver/maps/wfs` | Geoportal național — oferă amprentele clădirilor la nivel de acoperiș |
| **CADASTRU** | `https://cadastru.md/geoserver/w_cbi/wfs` | Registrul cadastral — oferă contururile clădirilor cu numere de casă |

**Profilurile** definesc ce strat WFS să fie utilizat și ce etichete să fie importate/excluse:

| Profil | Server | Strat | Etichete incluse | Note |
|---|---|---|---|---|
| **GEODATA (Roofs)** | GEODATA | `maps:lm17_area_roof_area` | Toate cu excepția `layer_name`, `layer`, `medium`, `source:building` | Cel mai bun pentru geometria acoperișului |
| **CADASTRU (Buildings)** | CADASTRU | `w_cbi:cad_cladiri` | Doar `NRCASA` (tradus în `addr:housenumber`) | Cel mai bun pentru datele de adresă |

Puteți adăuga, edita sau elimina servere și profiluri în **Preferințe → MD-BUILDINGS → Data Sources**.

---

### 5. Profilul CADASTRU și procesarea adreselor

Când utilizați profilul **CADASTRU (Buildings)**:

- Pluginul filtrează toate etichetele din răspunsul WFS, mapând câmpurile cadastrale la etichete OSM (de exemplu, `NRCASA` la `addr:housenumber`, `STREET` la `addr:street`, `CITY` la `addr:city`).
- **Sanitizarea adreselor**: Prefixele tipului de așezare (de exemplu, *mun., or., c., s.*) sunt eliminate automat din numele orașelor, lăsând formatul curat al numelor localităților.
- **Normalizarea diacriticelor**: Diacriticele românești vechi sau greșite (cum ar fi *Ş/ş, Ţ/ţ*) sunt convertite automat la caracterele corecte cu virgulă dedesubt (*Ș/ș, Ț/ț*).
- Etichetele `building=yes` și `source=AGCC/Linemap2017` sunt întotdeauna adăugate automat.

---

### 6. Fuziunea nodurilor de adresă interioare

Această funcție găsește automat **nodurile OSM existente** situate *în interiorul* conturului clădirii nou importate și **transferă etichetele lor `addr:*`** direct pe clădire.

**Activarea acestei funcții:**
1. Deschideți **Preferințe** (`F12`) → **MD-BUILDINGS** → fila **Data Sources**.
2. Bifați caseta: **"Merge interior address nodes into building"**.
3. Faceți clic pe OK și începeți importul.

> [!NOTE]
> Această funcție este **dezactivată implicit**. Activați-o doar când lucrați în zone unde nodurile de adresă există deja în OSM și doriți să le consolidați pe poligoanele clădirilor.

---

### 7. Rezoluția conflictelor de etichete

Când o clădire există deja în OSM și pluginul importă date mai noi de pe Geoserver, pot apărea conflicte de etichete. Pluginul le gestionează automat cu următoarea logică:

- **Eticheta clădirii** (`building=*`): Valoarea existentă este păstrată dacă este mai specifică decât `yes` (ex. `building=residential` este păstrată în loc să fie suprascrisă cu `building=yes`).
- **Toate celelalte etichete**: Noile etichete de pe Geoserver sunt aplicate peste etichetele existente. Etichetele existente care nu sunt prezente în noile date sunt în general păstrate.

---

### 8. Calculul avansat automat al înălțimii

Când o amprentă a clădirii din sursa **GEODATA** include un atribut `z` (cota absolută a acoperișului), pluginul poate calcula automat înălțimea relativă a clădirii:

1. **Interogare DTM stratificată**: Interoghează API-uri DTM (Digital Terrain Model) de la mai multe adrese URL Geoserver (cu logică integrată de reîncercare și rezervă) pentru a obține cota solului în punctul central al clădirii.
2. Calculează **înălțimea relativă** ca: `height = z_roof - z_ground`.
3. Rotunjește rezultatul la cel mai apropiat 0,1 metri.
4. Aplică eticheta `height=<valoare>` clădirii.

> [!TIP]
> Această funcție poate fi activată/dezactivată din **Preferințe**. Dacă calculul DTM este dezactivat sau indisponibil, etichetele brute `z=*` obținute de la Geoserver sunt eliminate automat pentru a preveni poluarea datelor OSM cu valori absolute de elevație în loc de înălțimea relativă a clădirii.

---

### 9. Rază dinamică de căutare BBOX

Pluginul utilizează o variabilă ajustabilă `BBOX_OFFSET` pentru a interoga elegant zonele WFS, permițând utilizatorilor să recupereze precis date relevante (precum adresele CADASTRU) în jurul unei clădiri active selectate sau la poziția cursorului. Acest lucru previne omisiunile în timpul interogărilor de date pe topologii mai extinse de clădiri.

---

## ⚙️ Preferințe și configurare

Deschideți Preferințele cu `F12`, apoi navigați la secțiunea **MD-BUILDINGS**. Fereastra de preferințe are următoarele file:

- **Fila Data Sources**: Gestionați conexiunile WFS Geoserver și profilurile de import.
- **Fila Stats**: Vizualizați și resetați statisticile de import.
- **Fila Notifications**: Configurați ce notificări și alerte sunt afișate.
- **Fila Uncommon Tags**: Gestionați lista de valori "comune" pentru etichetele clădirilor.
- **Fila Auto-remove Source Tags**: Gestionați valorile etichetelor `source` eliminate automat în timpul actualizărilor.

---

## 🗄 Surse de date

### GEODATA — Geoportalul Național
- **URL**: `https://geodata.gov.md/geoserver/maps/wfs`  
- **Strat**: `maps:lm17_area_roof_area`  
- **Cel mai bun pentru**: Precizia geometriei, calculul înălțimii, formele generale ale clădirilor  

### CADASTRU — Registrul Cadastral  
- **URL**: `https://cadastru.md/geoserver/w_cbi/wfs`  
- **Strat**: `w_cbi:cad_cladiri`  
- **Cel mai bun pentru**: Numere de casă (`addr:housenumber`), limite legale ale clădirilor  

---

## 💡 Sfaturi și bune practici

1. **Utilizați GEODATA pentru geometrie, CADASTRU pentru adrese.**
2. **Activați "Merge interior address nodes"** în zonele urbane cu noduri de adresă existente.
3. **Verificați întotdeauna indicatorul de stare** din panoul lateral după fiecare import (`DONE`, `NO_DATA` etc.).
4. **Utilizați modul "Geometry Update"** când amprentele se schimbă, dar etichetele trebuie păstrate.
5. **Revizuiți cu atenție dialogul pentru etichete neobișnuite.** Acesta surprinde datele incorecte din sursă.

---

## 🛠 Depanare

### Crash al Browserului de Ajutor JOSM
Dacă JOSM se blochează când deschideți meniul de ajutor pe Java 16+, adăugați acest steag la comanda de execuție JOSM:
```bash
--add-opens java.desktop/javax.swing.text.html=ALL-UNNAMED
```

---

## 🧹 Întreținerea depozitului

Pentru a menține depozitul simplificat și mai ușor de întreținut:
- Fișierele de testare sunt stocate sub `test/data/` și consumate direct de testele automate.
- Fișierele temporare vechi și copiile de testare duplicate nu ar trebui păstrate în dosare separate la nivel superior.

---

## 📜 Licență

Acest proiect este licențiat sub **GNU General Public License v3.0** — consultați fișierul [LICENSE](LICENSE) pentru detalii complete.

---

**Dezvoltat cu ❤️ pentru Comunitatea OpenStreetMap din Moldova.**  
*Bazat pe pluginul original [PL-Buildings](https://github.com/praszuk/josm-plbuildings-plugin) de Paweł Raszuk.*

Pentru mai multe detalii, vizitați [Depozitul Oficial](https://github.com/Oni-DOS/josm-mdbuildings-plugin).
