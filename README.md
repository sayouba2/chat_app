# Ping Me — Application de messagerie instantanée

**Ping Me** est une application Android de messagerie en temps réel, développée en Java avec Firebase comme backend. Elle permet d'échanger des messages texte et des photos, de gérer une liste d'amis, et de créer des groupes de discussion.

---

## Aperçu

| Connexion | Discussions | Chat | Amis |
|:---------:|:-----------:|:----:|:----:|
| Écran de connexion sécurisé | Liste des conversations en temps réel | Messagerie texte et photo | Gestion des demandes d'amis |

---

## Fonctionnalités

- **Authentification** — Inscription et connexion par email/mot de passe via Firebase Auth
- **Messagerie en temps réel** — Échange de messages texte avec indicateur « Envoyé / Vu »
- **Envoi de photos** — Sélection d'une image depuis la galerie, envoyée en Base64
- **Statut en ligne** — Indicateur de présence (en ligne / hors ligne) mis à jour en temps réel
- **Liste d'amis** — Envoi, annulation et acceptation de demandes d'amitié
- **Groupes** — Création de groupes avec photo et participants
- **Profil modifiable** — Modification du nom, du pseudo et de la photo de profil
- **Avatars prédéfinis** — Choix parmi une galerie d'avatars lors de l'inscription
- **Navigation latérale** — Menu drawer accessible sur tous les écrans principaux
- **Notifications push** — Intégration Firebase Cloud Messaging (FCM)

---

## Stack technique

| Catégorie | Technologie |
|---|---|
| Langage | Java |
| UI | Material Design 3, ConstraintLayout |
| Authentification | Firebase Authentication |
| Base de données | Cloud Firestore |
| Statut temps réel | Firebase Realtime Database |
| Stockage fichiers | Firebase Storage |
| Notifications | Firebase Cloud Messaging |
| Chargement d'images | Glide 4.16 |
| Avatar circulaire | CircleImageView 3.1 |
| minSdk | 28 (Android 9) |
| targetSdk | 35 (Android 15) |

---

## Architecture du projet

```
app/src/main/java/com/example/chat_app/
│
├── LoginActivity.java           # Connexion utilisateur
├── RegisterActivity.java        # Inscription + choix d'avatar
├── DiscussionActivity.java      # Liste des conversations
├── ChatActivity.java            # Écran de chat individuel
├── FindFriendsActivity.java     # Recherche et ajout d'amis
├── CreateGroupActivity.java     # Création de groupes
├── UserInformations.java        # Modification du profil
├── navbarActivity.java          # Activité de base (menu drawer)
│
├── ChatMessage.java             # Modèle d'un message
├── Discussion.java              # Modèle d'une conversation
│
├── ChatAdapter.java             # Adapter bulles de messages
├── DiscussionAdapter.java       # Adapter liste des discussions
├── FindFriendsAdapter.java      # Adapter recherche d'amis
└── SelectUserAdapter.java       # Adapter sélection de participants
```

---

## Structure Firestore

```
users/{uid}
  ├── name, pseudo, email, image
  ├── status ("online" | "offline")
  ├── fcmToken
  └── Friends/{friendUid}

Conversations/{uid}/chats/{otherUid}
  ├── name, lastMessage, imageUrl, timestamp
  └── type ("group" | null)

chats/{chatRoomId}/messages/{messageId}
  ├── senderId, receiverId, message
  ├── type ("text" | "image")
  ├── timestamp, isSeen

FriendRequests/{from_to}
  ├── from, to, status ("pending")

Groups/{groupId}
  ├── groupName, groupImage, createdBy
  ├── participants[], lastMessage, timestamp, type

UsersStatus/{uid}              ← Realtime Database
  ├── status, lastSeen
```

---

## Installation

### Prérequis

- Android Studio Hedgehog (2023.1) ou supérieur
- JDK 11+
- Un projet Firebase configuré

### Étapes

**1. Cloner le dépôt**
```bash
git clone https://github.com/sayouba2/chat_app.git
cd chat_app
```

**2. Configurer Firebase**

- Créez un projet sur [Firebase Console](https://console.firebase.google.com/)
- Ajoutez une application Android avec le package `com.example.chat_app`
- Téléchargez `google-services.json` et placez-le dans `app/`
- Activez dans la console Firebase :
  - Authentication (Email/Mot de passe)
  - Cloud Firestore
  - Realtime Database
  - Firebase Storage
  - Cloud Messaging

**3. Règles de sécurité Firestore** (développement)
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

**4. Lancer le projet**

Ouvrez le projet dans Android Studio, synchronisez Gradle, puis lancez sur un émulateur ou un appareil physique (API 28+).

---

## Points d'amélioration connus

| Priorité | Sujet |
|---|---|
| Moyen | Remplacer Base64 pour les images par Firebase Storage (limite Firestore 1 MB) |
| Moyen | Migrer `onActivityResult` vers `ActivityResultLauncher` (API dépréciée) |
| Faible | Externaliser toutes les chaînes dans `strings.xml` |
| Faible | Activer ProGuard en release (`isMinifyEnabled = true`) |

---

## Contributeurs

| Nom | GitHub |
|---|---|
| Sayouba | [@sayouba2](https://github.com/sayouba2) |

---

## Licence

Ce projet est développé à des fins éducatives. Aucune licence commerciale n'est accordée.
