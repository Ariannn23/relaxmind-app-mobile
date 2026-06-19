# Firebase setup for RelaxMind

## Firebase Console

1. Create a Firebase project, for example `relaxmind-app`.
2. Register an Android app with package name `com.relaxmind.app`.
3. Download the real `google-services.json`.
4. Replace `app/google-services.json` locally. Do not commit it.
5. Enable Authentication provider: Email/Password.
6. Create Cloud Firestore in production mode.
7. Create Cloud Storage if avatars/files will be tested.
8. Replace `REPLACE_WITH_FIREBASE_PROJECT_ID` in `.firebaserc` with the real project id.

## Deploy rules

Install and log in with Firebase CLI, then run:

```powershell
firebase login
firebase use <project-id>
firebase deploy --only firestore:rules,firestore:indexes,storage
```

## Android verification

After replacing `app/google-services.json`, run:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:installDebug
```

If registration still fails with `API key not valid`, the Android app in Firebase was created with the wrong package name or the downloaded `google-services.json` is not from that Firebase project.
