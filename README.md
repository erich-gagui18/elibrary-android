# E-Library Android

Android client for the E-Library AI project (IPT102 Final Practical Examination). It talks to the shared Laravel API in [`elibrary-api`](https://github.com/erich-gagui18/elibrary-api) and works alongside the website in [`elibrary-web`](https://github.com/erich-gagui18/elibrary-web).

Books come from Project Gutenberg (public domain), so every download is legal.

## What works in Phase 1

- Guest session on first launch (a server-issued token, sent as `X-Guest-Token`)
- Register, log in and log out (Laravel Sanctum token, sent as a Bearer token)
- Book catalog with debounced search, category chips and infinite scroll
- Download button that calls the API
- Live "downloads left" counter (guest limit 5, member limit 10)
- The API's own message when the limit is reached (HTTP 429)
- Black-and-white Material 3 theme with grayscale covers
- Bottom navigation: Catalog, My Library, Account
- My Library (members): save books from the catalog, favorite, remove, download; synced with the website
- Account tab: signed-in user, downloads left, log in / sign up / log out

The app never counts downloads itself. The backend enforces the limits and the app only displays what the API returns.

Not yet built: the embedded AI agent (Phase 2 and 3).

## Tech stack

- Kotlin, XML layouts with ViewBinding
- Jetpack Navigation (fragments)
- Retrofit, OkHttp and Gson for the API
- Coil for book covers
- Min SDK 24, compile and target SDK 36
- Gradle 8.13

## Run it

1. Start the API (see the `elibrary-api` README): start MySQL in XAMPP, then run `php artisan serve` in the API folder. It listens on `http://127.0.0.1:8000`.
2. Open this folder in Android Studio and let Gradle sync.
   - Gradle 8.13 supports Java 8 to 23. If Android Studio picks Java 25, change **Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK** to JetBrains Runtime 21 or Temurin 21.
3. Start an emulator (a Pixel with API 34 or 35 works) and press **Run**.

### API address

The app reads the API address from `api.base.url` in `local.properties` (this file is not committed). Without it, the emulator address `http://10.0.2.2:8000/api/` is used. `10.0.2.2` is how the Android emulator reaches your PC's localhost. After changing `local.properties`, run **Sync Project with Gradle Files** and run the app again.

**Real phone over USB (most reliable)**

1. Turn on USB debugging on the phone and plug it in.
2. Run `adb reverse tcp:8000 tcp:8000` (`adb` is in the Android SDK `platform-tools` folder).
3. In `local.properties` add: `api.base.url=http://127.0.0.1:8000/api/`
4. Start the API with `php artisan serve`.

**Real phone on the same Wi-Fi**

1. Start the API with `php artisan serve --host=0.0.0.0`.
2. Allow port 8000 through Windows Firewall.
3. In `local.properties` add: `api.base.url=http://<your PC's LAN IP>:8000/api/`
4. Check that `http://<your PC's LAN IP>:8000/api/health` opens in the phone's browser.

The manifest allows plain HTTP (`usesCleartextTraffic`) for development. Remove it and switch `BASE_URL` to an `https` address once the API is deployed.

## Build an APK

In Android Studio: **Build > Build Bundle(s) / APK(s) > Build APK(s)**. The debug APK is written to `app/build/outputs/apk/debug/`.

## Project structure

```
app/src/main/java/com/erich/elibrary/
  ELibraryApp.kt        App start, session setup
  MainActivity.kt       Single activity, navigation host
  ApiClient.kt          Retrofit and OkHttp setup, token headers
  ApiService.kt         API endpoints
  Models.kt             Response and request classes
  Repo.kt               Shared state: current user and download status
  Session.kt            Token and guest token storage (SharedPreferences)
  Util.kt               Helpers
  ui/
    BooksFragment.kt    Catalog, search, categories, counter
    BookAdapter.kt      Book list items
    AuthFragment.kt     Log in and register
app/src/main/res/       Layouts, navigation graph, theme, drawables
```

## API endpoints used

| Screen | Endpoint |
| --- | --- |
| App start | `POST /guest/start` |
| Register, log in, log out | `POST /register`, `POST /login`, `POST /logout` |
| Session check | `GET /me` |
| Catalog | `GET /books?q=&category=&page=&per_page=` |
| Category chips | `GET /categories` |
| Download | `GET /books/{id}/download` (429 at the limit) |
| Counter | `GET /downloads/status` |
| My Library | `GET/POST /library`, `PATCH /library/{bookId}/favorite`, `DELETE /library/{bookId}` |

## Phase 2 plan

- ~~My Library and favorites on Android, synced with the website~~ (done)
- Embedded AI agent chat, connected to the API's agent endpoint
- ~~Bottom navigation~~ (done)

## Author

Erich Gagui, BSIT 4th Year, University of Batangas, Lipa City.
