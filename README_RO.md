# Pluginul JOSM MD-Buildings V2 🇲🇩

[English](README.md) | [Română/Moldovenească](README_RO.md) | [Русский](README_RU.md)

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

## ⚡ Pornire rapidă

1. Deschideți JOSM și încărcați zona pe care doriți să o mapați.
2. **Descărcați datele OSM** pentru acea zonă (`Ctrl+Shift+Down`).
3. Deschideți **panoul lateral MD-Buildings** făcând clic pe pictograma Red Aurochs din panoul din stânga.
4. Selectați **profilul** dorit (de exemplu, `GEODATA (Roofs)` sau `CADASTRU (Buildings)`).

#### Import clădire nouă
1.  Navigați către zona țintă de pe hartă.
2.  Apăsați **Ctrl + Shift + 1** pentru a descărca și importa clădiri din sursa de date activă.
3.  Clădirile noi vor fi adăugate în stratul activ cu etichetele implicite.

#### Înlocuire/Actualizare clădire existentă
1.  Selectați o cale de clădire existentă (opțional, dar recomandat).
2.  **Declanșator Tip A (Selecție)**: Cu o cale de clădire selectată, apăsați **Ctrl + Shift + 1**.
3.  **Declanșator Tip B (Clic spațial)**: Mențineți apăsată tasta **Ctrl** și faceți **Dublu clic** în interiorul perimetrului unei căi de clădire existente.
4.  Pluginul va identifica clădirea existentă, îi va actualiza geometria și va sincroniza etichetele conform profilului selectat.

---

## 📖 Prezentare generală a funcțiilor

| Funcție | Descriere |
|---|---|
| **Import direct WFS** | Preluare în timp real a amprentelor clădirilor de pe Geoserverele publice moldovenești (GEODATA, CADASTRU). |
| **Fuziune inteligentă** | Traduce automat numerele de casă CADASTRU și fuzionează nodurile de adresă interioare. |
| **Calculul înălțimii** | Utilizează API-ul DTM pentru a calcula înălțimea clădirii pe baza cotei acoperișului și a nivelului solului. |
| **Detectarea suprapunerilor** | Previne importurile duplicate și gestionează conflictele spațiale cu datele OSM existente. |
| **3 moduri de import** | Import complet, actualizare doar geometrie sau actualizare doar etichete. |
| **Rezoluția conflictelor de etichete** | Rezolvă conflictele de etichete între datele descărcate și obiectele OSM existente. |
| **Verificarea prefixului de ciclu de viață** | Avertizează dacă etichetele clădirii conțin prefixe de ciclu de viață (ex. `demolished:building`). |
| **Verificarea etichetelor neobișnuite** | Evidențiază etichetele non-standard care pot necesita revizuire manuală. |

---

## 📖 Ghid de utilizare detaliat

### 1. Declanșarea unui import de clădire

**Declanșator Tip A (Selecție)**:
- Selectați o cale de clădire existentă în JOSM.
- Apăsați **Ctrl + Shift + 1**. Pluginul va încerca să actualizeze clădirea selectată.

**Declanșator Tip B (Clic spațial)**:
- Mențineți apăsată tasta **Ctrl** și **faceți dublu clic** pe butonul stâng al mouse-ului oriunde pe vizualizarea hărții.
- Pluginul va interoga Geoserverul pentru cea mai apropiată clădire de poziția cursorului. Dacă există o clădire OSM sub cursor, va încerca să o actualizeze.

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

### 5. Profilul CADASTRU și eticheta NRCASA

Când utilizați profilul **CADASTRU (Buildings)**:

- Pluginul filtrează toate etichetele din răspunsul WFS, păstrând **doar `NRCASA`** (câmpul numărului de casă cadastral moldovenesc).
- Eticheta `NRCASA` este **redenumită automat** în `addr:housenumber` pentru a respecta standardul de etichetare OpenStreetMap.
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

### 8. Calculul automat al înălțimii

Când o amprentă a clădirii din sursa **GEODATA** include un atribut `z` (cota absolută a acoperișului), pluginul automat:

1. Interoghează un **API DTM (Digital Terrain Model)** pentru a obține cota solului în punctul central al clădirii.
2. Calculează **înălțimea relativă** ca: `height = z_roof - z_ground`.
3. Rotunjește rezultatul la cel mai apropiat 0,1 metri.
4. Aplică eticheta `height=<valoare>` clădirii.

Acest lucru vă oferă înălțimi precise și reale ale clădirilor fără niciun calcul manual.

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
