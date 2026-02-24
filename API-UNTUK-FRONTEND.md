# 🔌 Panduan Integrasi API untuk Frontend

Dokumen ini berisi **semua info** yang frontend butuhkan untuk terhubung ke backend Hotel Booking API.

---

## 1. Info Dasar yang Harus Dikasih ke Frontend

| Info | Nilai | Keterangan |
|------|-------|-------------|
| **Base URL (dev)** | `http://localhost:8080` | **PENTING:** Backend di port 8080, bukan 3000 (frontend) |
| **Base URL (prod)** | `https://your-domain.com` | Sesuaikan saat deploy |
| **Content-Type** | `application/json` | Semua request & response JSON |
| **Dokumentasi interaktif** | `http://localhost:8080/q/swagger-ui` | Bisa test API langsung di browser |
| **OpenAPI spec** | `http://localhost:8080/q/openapi` | Bisa import ke Postman/Insomnia |

**⚠️ 401 Unauthorized?** Pastikan:
1. Base URL = `http://localhost:8080` (bukan localhost:3000)
2. User sudah login → dapat token dari `POST /auth/login`
3. Kirim header: `Authorization: Bearer <token>` di setiap request yang butuh auth (POST/PUT/DELETE hotel, room; POST booking)

---

## 1.1 Struktur Data (Hotel → Room → Booking)

- **Hotel:** punya `name`, `location`. Satu hotel punya banyak kamar.
- **Room:** punya `roomNumber`, `type`, `price`, dan relasi ke `hotelId`. Harga ada di sini.
- **Booking:** punya `roomId`, `checkInDate`, `checkOutDate`. Tanggal check-in/out ada di sini. `customerName` dari token.

---

## 2. CORS (Penting untuk Frontend)

Kalau frontend jalan di domain/port beda (misal React di `http://localhost:3000`), backend harus **allow CORS** dulu.

**Tambahkan di `application.properties`:**

```properties
quarkus.http.cors.enabled=true
quarkus.http.cors.origins=http://localhost:3000,http://localhost:5173
quarkus.http.cors.methods=GET,POST,PUT,DELETE,OPTIONS
quarkus.http.cors.headers=accept,content-type,authorization
```

- `origins` = URL frontend (Vite biasanya 5173, CRA 3000)
- `methods` = HTTP method yang diizinkan
- `headers` = Header yang boleh dikirim

---

## 3. Format Tanggal

| Field | Format | Contoh |
|-------|--------|--------|
| checkInDate, checkOutDate | `YYYY-MM-DD` (ISO 8601) | `"2026-03-15"` |

**Kirim:** `"2026-03-15"` (string)  
**Terima:** `"2026-03-15"` (string)  
**Jangan:** `"15/03/2026"` atau `"03-15-2026"`

---

## 4. Format Pagination

Semua endpoint list (GET yang return banyak data) pakai format pagination:

```json
{
  "content": [ /* array of items */ ],
  "page": 0,
  "size": 20,
  "totalElements": 45,
  "totalPages": 3
}
```

| Field | Tipe | Arti |
|-------|------|------|
| content | array | Data di halaman ini |
| page | number | Halaman keberapa (0-based) |
| size | number | Jumlah item per halaman |
| totalElements | number | Total semua data |
| totalPages | number | Total halaman |

**Query params:** `?page=0&size=20` (default: page=0, size=20)

---

## 5. Autentikasi (Login)

**Tanpa login:** Hanya bisa akses endpoint `@PermitAll` (lihat hotel, kamar, cek availability).

**Setelah login:** Kirim token di header setiap request:
```
Authorization: Bearer <token>
```

### 5.0 Auth Endpoints

| Method | Path | Deskripsi | Auth |
|--------|------|------------|------|
| POST | `/auth/login` | Login | Tidak perlu |
| POST | `/auth/register` | Daftar (jadi Customer) | Tidak perlu |

**Login Request:**
```json
{
  "username": "budi",
  "password": "rahasia123"
}
```

**Register Request:**
```json
{
  "username": "budi",
  "password": "rahasia123",
  "name": "Budi Santoso"
}
```

**Response (Login/Register):**
```json
{
  "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "budi",
  "name": "Budi Santoso",
  "role": "user"
}
```

**Akun Default (dibuat otomatis saat startup):**

| Role | Username | Password | Untuk |
|------|----------|----------|-------|
| Admin | `admin` | `admin123` | Kelola hotel, kelola kamar |
| Customer | `customer` | `customer123` | Booking kamar |

---

## 6. Daftar Endpoint Lengkap

### 6.1 Hotels

| Method | Path | Deskripsi | Query/Body |
|--------|------|-----------|------------|
| GET | `/hotels` | Daftar hotel (paginated) | `?page=0&size=20` |
| GET | `/hotels/{id}` | Detail hotel | - |
| POST | `/hotels` | Tambah hotel | Body: HotelRequestDto |
| PUT | `/hotels/{id}` | Update hotel | Body: HotelRequestDto |
| DELETE | `/hotels/{id}` | Hapus hotel | - |
| GET | `/hotels/{hotelId}/rooms` | Daftar kamar per hotel | `?page=0&size=20` |

**HotelRequestDto (POST/PUT):**
```json
{
  "name": "Hotel Santai",
  "location": "Jakarta"
}
```

**Response Hotel:**
```json
{
  "id": 1,
  "name": "Hotel Santai",
  "location": "Jakarta"
}
```

---

### 6.2 Rooms

| Method | Path | Deskripsi | Query/Body |
|--------|------|-----------|------------|
| GET | `/rooms` | Daftar semua kamar | `?page=0&size=20` |
| GET | `/rooms/{id}` | Detail kamar | - |
| POST | `/rooms` | Tambah kamar | Body: RoomRequestDto |
| PUT | `/rooms/{id}` | Update kamar | Body: RoomRequestDto |
| DELETE | `/rooms/{id}` | Hapus kamar | - |
| GET | `/rooms/{id}/availability` | Cek ketersediaan | `?checkIn=2026-03-01&checkOut=2026-03-05` |

**RoomRequestDto (POST/PUT):**
```json
{
  "roomNumber": "101",
  "type": "DELUXE",
  "price": 500000,
  "hotelId": 1
}
```

**Response Room:**
```json
{
  "id": 1,
  "roomNumber": "101",
  "type": "DELUXE",
  "price": 500000,
  "hotel": {
    "id": 1,
    "name": "Hotel Santai",
    "location": "Jakarta"
  }
}
```

**Response Availability:**
```json
{
  "available": true
}
```

---

### 6.3 Bookings

| Method | Path | Deskripsi | Query/Body |
|--------|------|-----------|------------|
| GET | `/bookings` | Daftar booking | `?page=0&size=20&customerName=Budi` |
| GET | `/bookings/{id}` | Detail booking | - |
| POST | `/bookings` | Buat booking | Body: BookingRequestDto |
| PUT | `/bookings/{id}/cancel` | Batalkan booking | - |

**BookingRequestDto (POST):** *Perlu login sebagai user*
```json
{
  "roomId": 1,
  "checkInDate": "2026-03-10",
  "checkOutDate": "2026-03-12"
}
```
*customerName diambil dari token (username yang login)*

**Response Booking:**
```json
{
  "id": 1,
  "room": {
    "id": 1,
    "roomNumber": "101",
    "type": "DELUXE",
    "price": 500000,
    "hotel": {
      "id": 1,
      "name": "Hotel Santai",
      "location": "Jakarta"
    }
  },
  "customerName": "Budi",
  "checkInDate": "2026-03-10",
  "checkOutDate": "2026-03-12",
  "status": "BOOKED"
}
```

---

## 7. Matrix Akses (User vs Admin)

| Endpoint | User (Customer) | Admin |
|----------|-----------------|-------|
| GET /hotels, /hotels/{id}, /hotels/{id}/rooms | ✓ | ✓ |
| POST/PUT/DELETE /hotels | ✗ | ✓ |
| GET /rooms, /rooms/{id}, /rooms/{id}/availability | ✓ | ✓ |
| POST/PUT/DELETE /rooms | ✗ | ✓ |
| GET /bookings | Hanya milik sendiri | Semua (+ filter customerName) |
| GET /bookings/{id} | Hanya milik sendiri | Semua |
| POST /bookings | ✓ | ✗ |
| PUT /bookings/{id}/cancel | Hanya milik sendiri | Semua |

---

## 8. Error Response

Saat error, backend return HTTP status + message:

| Status | Arti | Contoh |
|--------|------|--------|
| 401 | Unauthorized (belum login / token tidak dikirim) | Pastikan kirim `Authorization: Bearer <token>` |
| 400 | Bad Request (validasi gagal) | `{"message": "Room already booked for selected dates"}` |
| 404 | Not Found | `{"message": "Hotel not found"}` |
| 500 | Server Error | Internal error |

**Format validasi:** Saat Bean Validation gagal (400), response bisa berisi detail error per field.

---

## 9. Kolom Wajib & Validasi Request

### Hotel (POST/PUT /hotels) – butuh login Admin
| Kolom | Tipe | Wajib | Contoh |
|-------|------|-------|--------|
| name | string | ✓ | "Hotel Santai" |
| location | string | ✓ | "Jakarta" |

*Hotel tidak punya checkIn/checkOut/harga – itu di Room & Booking.*

### Room (POST/PUT /rooms) – butuh login Admin
| Kolom | Tipe | Wajib | Contoh |
|-------|------|-------|--------|
| roomNumber | string | ✓ | "101" |
| type | string | ✓ | "STANDARD", "DELUXE", "SUITE" |
| price | number | ✓ | 500000 |
| hotelId | number | ✓ | 1 |

*1 hotel punya banyak kamar. Harga ada di Room.*

### Booking (POST /bookings) – butuh login Customer
| Kolom | Tipe | Wajib | Contoh |
|-------|------|-------|--------|
| roomId | number | ✓ | 1 |
| checkInDate | string | ✓ | "2026-03-10" (YYYY-MM-DD) |
| checkOutDate | string | ✓ | "2026-03-12" (YYYY-MM-DD) |

*customerName diambil dari token (username yang login).*

### Validasi Detail
- **HotelRequestDto:** name 1–100 karakter, location 1–200 karakter
- **RoomRequestDto:** roomNumber 1–20 karakter, type 1–50 karakter, price ≥ 0, hotelId harus ada
- **BookingRequestDto:** checkIn/checkOut format YYYY-MM-DD, checkOut > checkIn, tidak boleh tanggal lalu

---

## 10. Contoh Alur Frontend (Customer Flow)

1. **Tampilkan daftar hotel**
   ```
   GET /hotels?page=0&size=20
   ```

2. **User klik hotel → tampilkan detail**
   ```
   GET /hotels/1
   ```

3. **Tampilkan kamar di hotel**
   ```
   GET /hotels/1/rooms?page=0&size=20
   ```

4. **User pilih kamar**
   ```
   GET /rooms/5
   ```

5. **User pilih tanggal → cek availability**
   ```
   GET /rooms/5/availability?checkIn=2026-03-10&checkOut=2026-03-12
   ```

6. **Jika available → tampilkan form konfirmasi**

7. **User login/register** (jika belum)
   ```
   POST /auth/login
   ```
   Simpan token di localStorage/cookie.

8. **User submit → create booking** (dengan header `Authorization: Bearer <token>`)
   ```
   POST /bookings
   Content-Type: application/json
   Authorization: Bearer <token>
   ```
   ```json
   {
     "roomId": 5,
     "checkInDate": "2026-03-10",
     "checkOutDate": "2026-03-12"
   }
   ```

9. **Tampilkan booking history**
   ```
   GET /bookings?customerName=Budi
   ```

10. **User cancel**
   ```
   PUT /bookings/1/cancel
   ```

---

## 10.1 Troubleshooting: 401 Saat POST /bookings (Konfirmasi Booking)

**Penyebab:** Customer belum login atau token tidak dikirim di request.

**Yang harus dicek frontend:**

1. **Base URL benar**
   - Backend: `http://localhost:8080`
   - Jika pakai proxy (mis. `"proxy": "http://localhost:8080"` di package.json), pastikan proxy aktif dan request ke `/bookings` ter-forward ke backend.

2. **User harus login dulu** sebelum booking
   - `POST /auth/login` dengan `username` + `password` customer
   - Simpan `token` dari response (localStorage/sessionStorage/state).

3. **Kirim header di setiap request ke /bookings**
   ```
   Authorization: Bearer <token>
   ```
   Contoh axios:
   ```js
   axios.post('http://localhost:8080/bookings', body, {
     headers: { 'Authorization': `Bearer ${token}` }
   })
   ```

4. **Format tanggal:** `checkInDate` dan `checkOutDate` harus `YYYY-MM-DD` (bukan DD/MM/YYYY).
   - UI tampil "25/02/2026" → kirim ke API: `"2026-02-25"`

5. **Role:** Hanya user dengan role `user` (customer) yang bisa POST booking. Admin tidak bisa.

---

## 11. Ringkasan untuk Frontend

**Yang harus dikasih ke frontend:**

1. **Base URL** (dev + prod)
2. **File ini** atau link ke Swagger UI
3. **CORS config** (sudah dijelaskan di atas – backend harus di-set)
4. **Format tanggal:** `YYYY-MM-DD`
5. **Format pagination:** `content`, `page`, `size`, `totalElements`, `totalPages`

**Opsi:** Bisa share link **Swagger UI** (`http://localhost:8080/q/swagger-ui`) – frontend bisa lihat & test semua endpoint di sana.

**Opsi:** Bisa export **OpenAPI spec** dari `/q/openapi` dan import ke Postman/Insomnia untuk koleksi API.
