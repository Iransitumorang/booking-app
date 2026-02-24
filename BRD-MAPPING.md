# BRD Mapping - Simple Hotel Booking System

Dokumen ini menjelaskan bagaimana kode kita memenuhi Business Requirements Document (BRD) dan alur bisnis yang diminta.

---

## 1. Ringkasan BRD

**Tujuan:** Sistem booking hotel sederhana (seperti Agoda/Booking.com) yang memungkinkan customer:
- Melihat daftar hotel & kamar
- Mengecek ketersediaan kamar
- Membuat booking
- Membatalkan booking

**Kunci:** Tidak boleh double booking, data harus konsisten, riwayat booking tersimpan.

---

## 2. Business Process Flow → API Mapping

| Step | Deskripsi BRD | API Endpoint | Penjelasan |
|------|---------------|--------------|------------|
| 1 | Customer views hotel list | `GET /hotels?page=0&size=20` | Customer melihat daftar hotel dengan pagination |
| 2 | Customer selects hotel | `GET /hotels/{id}` | Customer klik hotel → ambil detail hotel (nama, lokasi) |
| 3 | Customer views available rooms | `GET /hotels/{hotelId}/rooms?page=0&size=20` | Tampilkan kamar-kamar di hotel tersebut |
| 4 | Customer selects room | `GET /rooms/{id}` | Customer klik kamar → ambil detail kamar (nomor, tipe, harga) |
| 5 | Customer enters booking dates | - | Frontend form: checkIn, checkOut |
| 6 | System checks availability | `GET /rooms/{id}/availability?checkIn=2025-03-01&checkOut=2025-03-05` | Cek apakah kamar kosong di rentang tanggal tersebut |
| 7 | If available → create booking | `POST /bookings` | Body: roomId, customerName, checkInDate, checkOutDate |
| 8 | If not available → reject | Response 400 | "Room already booked for selected dates" |

---

## 3. CRUD Lengkap per Entity

### Hotel
| Operasi | Method | Endpoint | Keterangan |
|---------|--------|----------|------------|
| Create | POST | `/hotels` | Tambah hotel baru (Admin) |
| Read (list) | GET | `/hotels?page=&size=` | Daftar hotel dengan pagination |
| Read (by id) | GET | `/hotels/{id}` | Detail hotel |
| Update | PUT | `/hotels/{id}` | Ubah nama/lokasi hotel |
| Delete | DELETE | `/hotels/{id}` | Hapus hotel |

### Room
| Operasi | Method | Endpoint | Keterangan |
|---------|--------|----------|------------|
| Create | POST | `/rooms` | Tambah kamar (butuh hotelId) |
| Read (list) | GET | `/rooms?page=&size=` | Semua kamar |
| Read (by hotel) | GET | `/hotels/{hotelId}/rooms?page=&size=` | Kamar per hotel |
| Read (by id) | GET | `/rooms/{id}` | Detail kamar |
| Update | PUT | `/rooms/{id}` | Ubah data kamar |
| Delete | DELETE | `/rooms/{id}` | Hapus kamar |
| **Availability** | GET | `/rooms/{id}/availability?checkIn=&checkOut=` | Cek ketersediaan |

### Booking
| Operasi | Method | Endpoint | Keterangan |
|---------|--------|----------|------------|
| Create | POST | `/bookings` | Buat booking baru |
| Read (list) | GET | `/bookings?page=&size=&customerName=` | Riwayat booking (bisa filter by customer) |
| Read (by id) | GET | `/bookings/{id}` | Detail booking |
| Update (cancel) | PUT | `/bookings/{id}/cancel` | Batalkan booking |

---

## 4. Pemenuhan BRD Requirements

| Requirement | Implementasi |
|-------------|--------------|
| **Rooms cannot be double-booked** | `BookingRepository.findActiveBookings()` cek konflik tanggal. `BookingService.createBooking()` pakai `PESSIMISTIC_WRITE` lock pada Room untuk mencegah race condition saat 2 request bersamaan. |
| **Booking data accurate and consistent** | `@Transactional` di service, validasi input (Bean Validation), validasi checkOut > checkIn. |
| **Users can cancel bookings** | `PUT /bookings/{id}/cancel` mengubah status jadi CANCELLED. |
| **System maintains booking history** | Semua booking disimpan di DB. `GET /bookings` untuk lihat riwayat. Status: BOOKED atau CANCELLED. |

---

## 5. Struktur Kode (Untuk Pemula)

```
src/main/java/org/acme/
├── entity/          → Model database (Hotel, Room, Booking)
├── dto/              → Data Transfer Object untuk request/response
├── repository/       → Akses database (Panache)
├── service/          → Business logic (BookingService)
├── resource/         → REST API endpoints
└── ...
```

**Alur request:**
1. **Resource** terima HTTP request
2. **DTO** + **Validation** cek input
3. **Service** jalankan logic bisnis (cek availability, lock, dll)
4. **Repository** akses database
5. **Entity** disimpan/dibaca dari DB
6. Response dikirim ke client

---

## 6. Contoh Alur Customer (End-to-End)

1. `GET /hotels` → dapat list hotel
2. `GET /hotels/1` → detail Hotel #1
3. `GET /hotels/1/rooms` → kamar di Hotel #1
4. `GET /rooms/5` → detail Room #5
5. `GET /rooms/5/availability?checkIn=2025-03-10&checkOut=2025-03-12` → `{"available": true}`
6. `POST /bookings` dengan body:
   ```json
   {
     "roomId": 5,
     "customerName": "Budi",
     "checkInDate": "2025-03-10",
     "checkOutDate": "2025-03-12"
   }
   ```
7. `GET /bookings?customerName=Budi` → lihat booking Budi
8. `PUT /bookings/1/cancel` → batalkan booking

---

## 7. Teknologi yang Dipakai

- **Quarkus** – framework Java
- **PostgreSQL** – database
- **Hibernate Panache** – ORM
- **Bean Validation** – validasi input
- **REST (JAX-RS)** – API
- **Swagger UI** – dokumentasi & testing API di `/q/swagger-ui`
