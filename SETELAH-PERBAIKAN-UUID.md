# Setelah Perbaikan ClassCastException (Long → UUID)

## Yang sudah dilakukan

1. **`database.generation=drop-and-create`** di `application.properties`  
   Saat startup, Hibernate akan **DROP** tabel lama lalu **CREATE** ulang dengan kolom **id UUID** (bukan BIGINT).

2. **AdminSeeder** akan mengisi ulang: user admin/customer + hotel + room.

## Yang harus kamu lakukan

1. **Restart backend** (`quarkus dev`).
2. Cek: GET /rooms dan GET /bookings harus bisa (tanpa ClassCastException).
3. **Penting:** Setelah yakin jalan normal, buka `src/main/resources/application.properties` dan ubah:
   ```properties
   quarkus.hibernate-orm.database.generation=update
   ```
   Supaya data **tidak hilang** setiap kali restart.

## Kalau tetap pakai `drop-and-create`

- Setiap restart = semua tabel di-drop dan dibuat ulang, data hilang.
- Hanya cocok untuk development kalau memang mau reset terus.

## Ringkasan

| Masalah | Penyebab | Solusi |
|--------|----------|--------|
| `ClassCastException: Long to UUID` | Kolom `id` di DB masih BIGINT | `database.generation=create` 1x → tabel pakai UUID |
| Data hilang tiap restart | Lupa ganti ke `update` | Ganti ke `quarkus.hibernate-orm.database.generation=update` |
