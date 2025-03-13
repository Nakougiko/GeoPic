# 📸 GeoPic - Application de photos géolocalisées  

GeoPic est une application Android qui permet de **prendre des photos et d'enregistrer automatiquement la localisation GPS** dans les métadonnées EXIF. L'utilisateur peut ensuite **visualiser ses photos sur une carte interactive**, avec un marqueur unique par emplacement.

## 📌 **Fonctionnalités principales**  

✅ **Prise de photos géolocalisées** : Les photos prises avec l'application enregistrent la latitude et la longitude directement dans les métadonnées EXIF.  
✅ **Carte interactive** : Affichage des photos sur **Google Maps**, avec un seul marqueur par emplacement regroupant plusieurs photos.  
✅ **Navigation entre les vues** : Une **Bottom Navigation Bar** permet de basculer entre la caméra et la carte.  
✅ **Affichage des photos via un Bottom Sheet** : Lorsqu'un marqueur est sélectionné, un panneau affiche les photos prises à cet endroit.  
✅ **Suppression des photos** : Option de suppression directe dans l’application, avec mise à jour dynamique des marqueurs.  
✅ **Changement de caméra** : Possibilité de **passer de la caméra avant à la caméra arrière**.  
✅ **Zoom automatique sur la dernière photo prise** pour une meilleure accessibilité.  
✅ **Ouverture des photos dans la galerie** en cliquant sur l’aperçu dans l’application.  

---

## 🛠️ **Technologies utilisées**  

| Technologie | Utilisation |
|-------------|------------|
| **Kotlin** | Langage principal de développement |
| **CameraX** | Gestion de la prise de photos |
| **FusedLocationProviderClient** | Récupération de la localisation GPS |
| **ExifInterface** | Stockage et récupération des coordonnées GPS dans les métadonnées EXIF |
| **Google Maps SDK** | Affichage de la carte interactive et gestion des marqueurs |
| **View Binding** | Manipulation efficace des vues |
| **BottomSheetDialogFragment** | Affichage des photos en fonction des marqueurs |
| **RecyclerView** | Affichage dynamique des photos prises à un emplacement donné |

---

## 📞 **Installation et exécution**  

### **Pré-requis**  
- Android Studio **Giraffe | 2022.3.1** ou supérieur  
- **SDK 24+ (Android 7.0)**  
- Permissions suivantes activées :  
  - 🎥 **CAMERA** (prise de photos)  
  - 🌍 **ACCESS_FINE_LOCATION** (géolocalisation précise)  
  - 🗂 **READ/WRITE_EXTERNAL_STORAGE** (stockage des photos)  

### **Installation**  
1. Clone le projet :  
   ```bash
   git clone https://github.com/Nakougiko/GeoPic.git
   cd GeoPic
   ```
2. Ouvre le projet avec **Android Studio**.  
3. Synchronise les dépendances **Gradle**.  
4. Ajoute ta **clé API Google Maps** dans `AndroidManifest.xml` :
   ```xml
   <meta-data
       android:name="com.google.android.geo.API_KEY"
       android:value="VOTRE_CLE_API_ICI" />
   ```
5. Lance l'application sur un **émulateur** ou un **appareil physique**.

---

## 🎮 **Utilisation**  

1️⃣ **Prendre une photo**  
- Ouvrir l'application et accéder à l'onglet **"Photo"**.  
- Capturer une photo en appuyant sur le bouton dédié.  
- La localisation sera enregistrée automatiquement dans les **EXIF**.  

2️⃣ **Afficher les photos sur la carte**  
- Naviguer vers l’onglet **"Carte"** via la Bottom Navigation Bar.  
- Les marqueurs affichent les photos regroupées par coordonnées GPS.  

3️⃣ **Explorer les photos prises**  
- Cliquer sur un marqueur pour afficher toutes les photos prises à cet endroit.  
- Sélectionner une photo pour l’ouvrir dans la galerie.  

4️⃣ **Supprimer une photo**  
- Depuis le **Bottom Sheet**, cliquer sur l’icône de suppression.  
- Si c’est la dernière photo associée à un marqueur, celui-ci disparaît.  

---

## 🚀 **Perspectives d’évolution**  

🎯 **Vers un réseau social de partage de photos géolocalisées**  
À l’avenir, l’application pourrait évoluer en une plateforme sociale où les utilisateurs pourraient **partager leurs photos avec leurs amis**, **liker et commenter** celles des autres, et recevoir des **notifications en temps réel** sur les nouvelles publications.  

📌 **Idées d’améliorations** :  
- 📤 **Partage des photos avec une liste d’amis**  
- 🔔 **Ajout de notifications en temps réel** lors du partage d’une nouvelle photo  
- 👥 **Création de groupes pour partager des photos à plusieurs**  

---

## 🌟 **Auteur**  

Développé par **Lukas Goulois**.  
📧 Contact : [goulois.lukas@gmail.com](mailto:goulois.lukas@gmail.com)  
🌐 **GitHub** : [Nakougiko](https://github.com/Nakougiko)  

---

## 📚 **Licence**  

Ce projet est sous licence **MIT** – voir le fichier [LICENSE](LICENSE) pour plus d’informations.
