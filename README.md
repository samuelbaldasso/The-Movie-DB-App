# TMDB App — Native Android

A Kotlin and Jetpack Compose application for exploring popular movies, searching titles, viewing movie details, and saving favorites on the device. This portfolio project focuses on pagination, local persistence, and reactive data flows.

## Screenshots

<table>
  <tr>
    <th align="center" width="33%">Popular movies</th>
    <th align="center" width="33%">Search</th>
    <th align="center" width="33%">Details</th>
  </tr>
  <tr>
    <td align="center" valign="top"><img src="screenshots/home.png" alt="Popular movies" width="240" /></td>
    <td align="center" valign="top"><img src="screenshots/search_results.png" alt="Search results" width="240" /></td>
    <td align="center" valign="top"><img src="screenshots/details.png" alt="Movie details" width="240" /></td>
  </tr>
</table>

These screenshots show the interface before favorites were added and card contrast was improved.

## Features

- Popular movie catalog with pagination and pull-to-refresh.
- Paginated search for queries of at least three characters, with a 500 ms debounce and cancellation when the query changes.
- Movie details with synopsis, rating, release date, and artwork.
- Add or remove favorites from the details screen; open the favorites list using the heart icon on the home screen.
- Locally persisted favorites that remain available after closing and reopening the app.
- Loading, error, empty, and retry states.
- Light/dark themes and grids that adapt to the available width.

## Data and offline behavior

The popular movie list is read from Room and updated by a `RemoteMediator`. A successful refresh replaces only the popular catalog and its pagination keys. If the update fails, previously cached content remains visible.

Details and favorites use independent tables. Viewing a search result does not add that movie to the popular list. To open details offline, the repository checks the details cache, favorites, and finally the saved catalog. New searches and pages that have not been loaded require an internet connection. Images rely on Coil's cache and may be unavailable offline.

The database is at version 3, with an explicit migration from version 2 that preserves the existing catalog. Automatic cache expiration, database encryption, and cross-device favorites synchronization are not implemented.

## Project structure

A single `app` module organized into layers:

```text
com.sbaldasso.tmdbapp/
├── data/          # Retrofit, Room, mappers, pagination, and repository implementation
├── domain/        # Models, repository contract, and use cases
├── presentation/  # Compose screens, ViewModels, state, components, and navigation
├── di/            # Hilt modules
└── ui/theme/      # Material theme
```

The UI depends on ViewModels; use cases access the `MovieRepository` contract; the implementation coordinates networking and persistence. The domain uses `PagingData`, so it is not completely independent of Android Jetpack. Layers are separated by packages rather than Gradle modules.

## Tech stack

Kotlin 1.9.22, Compose + Material 3, Navigation Compose, Hilt, Coroutines/Flow, Retrofit/OkHttp, Kotlinx Serialization, Room, Paging 3, and Coil. Tests use JUnit, MockK, Coroutines Test, Paging Testing, and Robolectric.

The dependencies actually used are declared in `app/build.gradle.kts`; the project does not use its version catalog to resolve them.

## Getting started

Requirements: JDK 17, an Android Studio version compatible with AGP 8.3.0, and Android SDK 34. The app supports Android 7.0 (API 24) and later. The Gradle Wrapper is included.

1. Clone the repository and open it in Android Studio.
2. Configure the SDK through Android Studio or the `ANDROID_HOME` environment variable.
3. Create an untracked `local.properties` file in the project root:

```properties
sdk.dir=/path/to/Android/sdk
TMDB_API_KEY=your_v3_api_key
```

You can also provide `TMDB_API_KEY` as an environment variable; it takes precedence over the local file. Obtain a v3 key from the [TMDB API settings](https://www.themoviedb.org/settings/api).

```bash
./gradlew assembleDebug
./gradlew installDebug
```

Builds and tests can run without a key. In that case, online catalog requests display a configuration error. There is no fallback API key in the source code. Because the key is embedded in the APK through `BuildConfig`, it should not be treated as a protected secret on the device.

## Validation

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Tests verify:

- Movie content emitted by the popular movies use case and `HomeViewModel`.
- Search debounce, normalization, cancellation, and result clearing.
- Details loading, errors, retries, and failures when saving favorites.
- Independent details caching, offline fallback, and cancellation propagation.
- End-of-pagination behavior and paginated search errors.
- Favorites persistence and removal after reopening the database.
- Refresh preserving details and favorites, network failures preserving popular movies, and migration from version 2 to 3 with Room schema validation.

Database tests use SQLite with Robolectric on the JVM. The sample instrumented test does not provide coverage of UI flows. No coverage percentage or APK size, memory, or startup benchmarks are claimed.

The [Android checks](.github/workflows/android.yml) workflow runs tests, lint, and an APK build on pushes and pull requests to `main`, without TMDB credentials, and uploads reports as artifacts.

## Technical decisions and next steps

- Favorites store a copy of the movie data so they survive catalog refreshes.
- Search cancels the previous debounce and collection when the query changes; short queries clear results.
- Compose state collection follows the lifecycle.
- Coroutine cancellation propagates without becoming an error or triggering a cache fallback.
- Cards use a dark gradient and white titles to improve readability over posters.

Next steps include Compose tests for complete user flows, accessibility and large-font reviews, text internationalization, a planned toolchain upgrade, and performance measurements. The current target SDK is 34; distribution requirements should be checked before publishing.

## Author and license

[Samuel Baldasso](https://github.com/samuelbaldasso). Licensed under the MIT License; see [LICENSE](LICENSE).

This project uses the TMDB API but is not endorsed or certified by TMDB.
