# Modern Android Music Player (Android 14 / Kotlin / Jetpack Compose / Media3 / Hilt)

A feature-rich, high-performance local music player application built with modern Android development best practices, supporting Android 14 (API 34) and downward to Min SDK 24.

---

## Architecture & Tech Stack

- **Language & UI:** Kotlin, Jetpack Compose, Material3
- **Dependency Injection:** Dagger Hilt
- **Concurrency:** Kotlin Coroutines & Flow
- **Media Playback:** AndroidX Media3 (ExoPlayer, MediaSessionService)
- **Local Storage & Database:** Room DB, MediaStore ContentResolver
- **Home Screen Widgets:** Jetpack Glance
- **Permissions:** Accompanist Permissions
- **CI/CD Automation:** GitHub Actions

---

## Project Structure

```text
com.example.musicplayer
│
├── data/
│   ├── local/          # Room DB (Playlists, Favorites, MusicDatabase, DAOs)
│   └── repository/     # AudioRepositoryImpl (MediaStore Scanner, LRC lyrics parser)
├── di/                 # Hilt Modules (AppModule, ServiceModule, DatabaseModule)
├── domain/
│   ├── model/          # Data models (AudioItem, Lyrics, PlayerState, Result)
│   └── repository/     # AudioRepository interface
├── service/            # MusicService (MediaSessionService, ExoPlayer error handling)
├── ui/
│   ├── components/     # MiniPlayer, LyricsView, etc.
│   ├── permissions/    # PermissionScreen (Accompanist)
│   ├── screens/        # DashboardScreen, FullPlayerScreen
│   └── viewmodel/      # MusicViewModel
├── widget/             # Jetpack Glance App Widget (MusicWidget)
└── MainActivity.kt     # Entry point tying UI, ViewModel, and MediaService
```

---

## Getting Started & GitHub CI/CD

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/music-player.git
   cd music-player
   ```

2. **Open in Android Studio:**
   Ensure you have Android Studio Iguana or newer with Kotlin 1.9.22 and JDK 17 configured.

3. **Automated GitHub Actions Build:**
   Pushing your code to the `main` or `master` branch triggers the GitHub Actions workflow (`.github/workflows/android.yml`). 
   Once the build completes successfully, you can download the compiled `app-debug.apk` directly from the **GitHub Actions > Workflow Run > Artifacts** section.
