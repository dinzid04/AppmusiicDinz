# Implementation Plan: Home & Lyrics UI Redesign with Settings Toggles

Berdasarkan analisis tangkapan layar referensi (`Screenshot_20261023_*`) dan jawaban preferensi Anda, berikut adalah rencana lengkap untuk merombak tata letak Beranda dan Halaman Lirik, dengan opsi sakelar tampilan mandiri di menu Pengaturan Tampilan (Settings > Appearance).

---

## 1. Perubahan Tata Letak Beranda (Home Screen)

### A. Pintasan Cepat (Quick Shortcuts)
- **Desain Baru (Sesuai `Screenshot_20261023_094346_Auralys_bbc82374.jpg`)**:
  - Format grid terstruktur 3 kolom x 3 baris per halaman kartu.
  - Gambar thumbnail kotak dengan rounded corner presisi, judul lagu tebal, dan nama artis di bawahnya.
  - Indikator halaman titik (dot page indicator) di bawah grid untuk navigasi halaman pintasan yang rapi dan tidak berantakan.
- **Dukungan Toggle**: Dapat dialihkan antara layout baru 3x3 atau layout default horizontal list.

### B. Bagian Rekomendasi Beranda ("Pilihan Cepat", "Cover dan Remix", "Lagu Trending Untuk Kamu")
- **Desain Baru (Sesuai `Screenshot_20261023_095215_Auralys_1912bead.jpg`)**:
  - Kolom vertikal berisi 4 lagu per halaman geser, dengan thumbnail persegi, judul, artis/detail rilis, dan tombol menu opsi 3-titik di sisi kanan setiap item.
  - **Tombol "PUTAR SEMUA"**:
    - Format teks murni tanpa latar belakang pil/overlay (clean uppercase text).
    - **Fungsi Pemutaran**: Memutar langsung lagu pertama di kategori tersebut, dan secara otomatis memasukkan sisa lagu dalam daftar tersebut ke antrean berikutnya (next in queue) secara berurutan.
- **Dukungan Toggle**: Tersedia opsi toggle terpisah di Pengaturan untuk mengaktifkan/menonaktifkan desain baru ini.

### C. Bagian "Dari Komunitas" (Community Playlist Shelf)
- **Desain Baru (Sesuai `Screenshot_20261023_100118_Auralys_5823218e.jpg`)**:
  - Kartu kontainer bergradasi lembut dengan artwork kolase 2x2 di bagian atas, judul playlist tebal, dan nama kurator/pembuat.
  - Daftar 3 lagu teratas berpenomoran urut (1, 2, 3) lengkap dengan artwork mini, judul lagu, dan artis.
  - Baris aksi bawah (bottom action row):
    - Tombol `PUTAR` (ikon Play + teks).
    - Tombol radio `((o))` untuk memulai stasiun radio berdasarkan playlist.
    - Tombol simpan/tambah `(+)` untuk menyimpan playlist ke pustaka.
- **Dukungan Toggle**: Dapat diaktifkan atau dikembalikan ke format default melalui Pengaturan.

---

## 2. Pengaturan Tampilan (Settings > Appearance)

Penambahan 3 sakelar mandiri (independent toggle switches) yang disimpan secara persisten di `AppSettings`:
1. **Tampilan Desain "Dari Komunitas"**: Mengaktifkan tampilan kartu kolase 2x2 + daftar bernomor vs tampilan default.
2. **Tampilan Desain "Pintasan Cepat"**: Mengaktifkan tampilan grid 3x3 rapi vs tampilan default.
3. **Tampilan Desain "Beranda & Rekomendasi"**: Mengaktifkan layout 4-item dengan teks PUTAR SEMUA vs tampilan default.

---

## 3. Redesign Halaman Lirik (Lyrics Screen)

### A. Header Minimalis (Sesuai `Screenshot_20261023_101030_BitChord_e9e68f01.jpg` & `Screenshot_20261023_101144_BitChord_8a70e124.jpg`)
- Bagian atas hanya menampilkan:
  - Thumbnail lagu (persegi berujung tumpul).
  - Judul lagu & nama artis di sampingnya.
  - Tombol favorit (hati / love).
  - Tombol menu 3-titik (menampung fitur-fitur tambahan seperti Sleep Timer, Audio Quality, dsb.).

### B. Bar Kontrol Lirik Bawah
- Ikon bola dunia (Globe) untuk mengaktifkan/menonaktifkan mode teks Romaji (romanisasi kanji/karakter).
- Ikon terjemahan (Translate) untuk beralih bahasa terjemahan lirik.
- Teks `"Lirik oleh [Nama Provider]"` yang dapat diklik untuk membuka lembar pemilihan penyedia lirik (*Provider Selector*).

### C. Modal "Pilih Penyedia Lirik" (Sesuai `Screenshot_20261023_101818_BitChord_216a3ead.jpg`)
- Tampilan bottom sheet terorganisir dengan status ikon untuk masing-masing penyedia:
  - Ikon centang hijau/aktif untuk penyedia yang sedang aktif digunakan.
  - Ikon kaca pembesar (search) untuk penyedia yang tersedia dicari liriknya.
  - Ikon silang (x) untuk penyedia yang tidak menemukan lirik untuk lagu saat ini.

### D. Modal "Lyrics Offset" (Sesuai `Screenshot_20261023_101809_BitChord_baba1885.jpg`)
- Judul *"Lyrics offset"* dengan subjudul *"Positive values show lyrics later"*.
- Angka nilai offset besar di tengah (contoh: `0,0 s` atau `-1,5 s`).
- Slider offset interaktif diapit oleh tombol langkah `-` dan `+`.
- Tombol *"Reset"* di bagian bawah untuk mengembalikan offset ke 0 detik.

---

## 4. Tipografi SF Pro Display
- Penggunaan tipografi sans-serif modern berbobot bold (SF Pro Display style) untuk semua judul header, lirik lagu, teks tombol PUTAR SEMUA, dan label provider secara konsisten dan aman tanpa risiko crash binary font.

---

## 5. Rencana Pengujian & Verifikasi
1. Verifikasi kompilasi build Android melalui `compile_applet`.
2. Validasi persistensi sakelar Settings saat diubah On/Off.
3. Uji fungsi antrean tombol teks "PUTAR SEMUA" untuk memastikan lagu langsung diputar dan sisa antrean tersusun rapi.
4. Uji interaksi pemilihan penyedia lirik dan slider lirik offset.
