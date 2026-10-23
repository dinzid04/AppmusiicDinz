# Rencana Perbaikan Force Close Saat Aplikasi Dibuka & Default Liquid Glass OFF

Berdasarkan investigasi kode pada alur peluncuran utama (`MainActivity`, `AppSettings`, `ListenNowScreen`, dan `YtMusicScraper`), ditemukan **3 penyebab utama** mengapa aplikasi langsung mengalami *force close* saat dibuka di halaman utama:

1. **Efek Shader AGSL `LiquidGlass` Aktif Secara Default Saat Pertama Kali Dibuka**:
   - Di `AppSettings.kt`, nilai awal `_liquidGlass` diatur ke `isLiquidGlassSupported` (`true` pada Android 13+ / emulator preview). Akibatnya, sejak frame pertama aplikasi dibuka, seluruh halaman utama dibungkus oleh `layerBackdrop(navBackdrop)` dan merender `GlassNavBar` menggunakan shader AGSL `RuntimeShader` (`refractionWithDispersion`, `blur`, dan `colorControls`) yang membebani/memicu crash pipeline grafis emulator saat inisialisasi layer.
2. **Duplikasi Key pada `LazyColumn` dan `LazyRow` di Halaman Utama (`IllegalArgumentException: Key "..." was already used`)**:
   - Di `ListenNowScreen.kt` dan `ListenNowComponents.kt`, daftar rak (*shelves*) dan lagu dari YouTube Music menggunakan `key = { "section_${section.id}" }` atau `key = { "${section.id}_${item.id}" }` tanpa indeks. Ketika `YtMusicScraper` mengembalikan dua seksi dengan judul serupa atau item lagu yang muncul lebih dari sekali di dalam rak yang sama, Jetpack Compose langsung melempar `IllegalArgumentException` dan membuat aplikasi *force close* seketika di halaman utama.
3. **Potensi *Race Condition* Inisialisasi `SharedPreferences` (`UninitializedPropertyAccessException: lateinit property prefs has not been initialized`)**:
   - `AppSettings.init()`, `AuthStore.init()`, dan `RemoteArtworkStore.init()` sebelumnya baru dipanggil di dalam `MusicHomeViewModel.init`, bukan di awal `MainActivity.onCreate()` sebelum UI Compose dirender. Selain itu, beberapa fungsi di `AuthStore` dan `RemoteArtworkStore` mengakses `prefs` tanpa pengecekan `::prefs.isInitialized`.

---

## Perubahan yang Akan Dilakukan

### 1. Mengubah Default `LiquidGlass` Menjadi `OFF` (`false`) & Menambahkan Pengaman Shader
- **`app/src/main/java/com/example/data/settings/AppSettings.kt`**:
  - Mengubah nilai default `_liquidGlass` dari `MutableStateFlow(isLiquidGlassSupported)` menjadi `MutableStateFlow(false)`.
  - Mengubah pembacaan preferensi di `init()` menjadi `prefs.getBoolean("liquid_glass", false) && isLiquidGlassSupported`, sehingga efek Liquid Glass **selalu nonaktif (`OFF`) secara default** dan hanya aktif apabila pengguna menyalakannya sendiri melalui menu **Settings**.
- **`app/src/main/java/com/example/ui/liquidglass/backdrop/DrawBackdropModifier.kt` & `GlassSelectionPill.kt`**:
  - Menambahkan pengaman (`runCatching`) pada eksekusi `RuntimeShader` dan perekaman `GraphicsLayer` agar jika pengguna mengaktifkan Liquid Glass secara manual di perangkat/emulator tertentu, kegagalan kompilasi shader tidak akan menyebabkan *force close*.

### 2. Mencegah Crash *Duplicate Key* di Halaman Utama & Seluruh Carousel
- **`app/src/main/java/com/example/ui/home/ListenNowScreen.kt`**:
  - Mengubah `items(items = uiState.carouselSections, key = ...)` menjadi `itemsIndexed` dengan key unik berbasis indeks (`"section_${idx}_${section.id}"`).
- **`app/src/main/java/com/example/ui/components/ListenNowComponents.kt`**:
  - Mengubah seluruh `LazyRow` (`FavoritesForeverRow`, `RockClassicsRow`, `DynamicCarouselSection`, dll.) agar menggunakan `itemsIndexed` dengan key `"${section.id}_${idx}_${item.id}"` sehingga lagu atau album yang muncul ganda dari API tidak pernah memicu crash Compose.
- **`app/src/main/java/com/example/data/ytmusic/YtMusicScraper.kt`**:
  - Memastikan setiap `HomeSection` yang di-parse memiliki `id` unik dengan menyertakan indeks seksi jika terdapat judul rak yang sama.
- **`app/src/main/java/com/example/ui/components/ShelfRow.kt`, `ExploreScreen.kt`, `SearchPlaceholderScreen.kt`, & `ArtistProfileScreen.kt`**:
  - Menyeragamkan penggunaan `itemsIndexed` pada daftar horizontal/vertikal untuk mencegah crash serupa saat berpindah tab.

### 3. Inisialisasi Lebih Awal di `MainActivity.onCreate()` & Guard `SharedPreferences`
- **`app/src/main/java/com/example/MainActivity.kt`**:
  - Memanggil `AppSettings.init(applicationContext)`, `AuthStore.init(applicationContext)`, dan `RemoteArtworkStore.init(applicationContext)` di baris paling atas `onCreate()` sebelum `setContent` dijalankan.
- **`app/src/main/java/com/example/data/auth/AuthStore.kt` & `app/src/main/java/com/example/data/RemoteArtworkStore.kt`**:
  - Menambahkan pengecekan `if (!::prefs.isInitialized) return` sebelum setiap operasi `prefs.edit()` dan pembacaan state.

---

## Rencana Verifikasi
1. **Kompilasi Aplikasi**: Menjalankan `compile_applet` untuk memastikan seluruh perubahan kode dikompilasi tanpa error.
2. **Pengujian Peluncuran Halaman Utama (Robolectric JVM Test)**: Menjalankan pengujian unit/Robolectric (`gradle :app:testDebugUnitTest`) yang membuka `MainActivity`, memverifikasi halaman utama (`ListenNowScreen`) tampil tanpa *force close*, serta memastikan status default `AppSettings.liquidGlass.value` adalah `false`.
