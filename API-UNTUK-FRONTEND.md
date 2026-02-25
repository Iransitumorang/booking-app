# 🔌 Panduan Integrasi API untuk Frontend

Dokumen ini berisi **semua info** yang frontend butuhkan untuk terhubung ke backend Hotel Booking API.

---

## 0. Alur CRUD yang Benar (PENTING)

### Admin – Tambah/Edit/Hapus Hotel & Room
1. **Login** → `POST /auth/login` dengan `{"username":"admin","password":"admin123"}`
2. **Simpan token** dari response (`token` field) – localStorage/sessionStorage/state
3. **Setiap request CRUD** (POST/PUT/DELETE hotel, room) **wajib** kirim header:
   ```
   Authorization: Bearer <token>
   ```
4. **Base URL** = `http://localhost:8080` (bukan 3000). Jika pakai proxy, pastikan `/hotels` dan `/rooms` ter-forward ke backend.

### Customer – Booking
1. **Login** → `POST /auth/login` dengan `{"username":"customer","password":"customer123"}`
2. **Simpan token**
3. **POST /bookings** wajib kirim header `Authorization: Bearer <token>`

### Verifikasi Token
- Panggil `GET /auth/me` dengan header `Authorization: Bearer <token>`
- Jika `groups` berisi `["Admin"]` → siap untuk CRUD hotel/room
- Jika `groups` berisi `["User"]` → siap untuk booking

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
3. Kirim header: `Authorization: Bearer <token>` di setiap request yang butuh auth
4. **Token tanpa tanda kutip** – nilai header harus `Bearer eyJ0eXA...` (token mentah), bukan `Bearer "eyJ0eXA..."` (dengan kutip). Di Swagger UI "Authorize", paste token tanpa kutip.

**⚠️ 403 Forbidden?** (mis. admin tambah hotel dapat 403)
1. **Login ulang** sebagai admin → dapat token baru
2. Pastikan token **disimpan** dan **dikirim** di header: `Authorization: Bearer <token>`
3. Cek `GET /auth/me` → `groups` harus `["Admin"]` untuk CRUD hotel/room
4. Base URL = `http://localhost:8080` (proxy harus forward ke sini)

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
| GET | `/auth/me` | Cek user, role, dan groups dari JWT | Perlu token |

**Response GET /auth/me:**
```json
{
  "username": "admin",
  "name": "Administrator",
  "role": "admin",
  "groups": ["Admin"]
}
```
- `groups` = nilai di JWT untuk RBAC (sesuai [Quarkus JWT guide](https://quarkus.io/guides/security-jwt)). Admin → `["Admin"]`, Customer → `["User"]`

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

**BookingRequestDto (POST):** *Perlu login (customer atau admin)*
```json
{
  "roomId": 1,
  "checkInDate": "2026-03-10",
  "checkOutDate": "2026-03-12",
  "customerName": "budi"
}
```
- **Customer:** tidak kirim `customerName` → otomatis pakai username yang login
- **Admin:** kirim `customerName` (username customer) untuk booking atas nama customer

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
| POST /bookings | ✓ (untuk diri sendiri) | ✓ (bisa untuk customer, kirim customerName) |
| PUT /bookings/{id}/cancel | Hanya milik sendiri | Semua |

---

## 8. Error Response

Saat error, backend return HTTP status + message:

| Status | Arti | Contoh |
|--------|------|--------|
| 401 | Unauthorized (belum login / token tidak dikirim) | Pastikan kirim `Authorization: Bearer <token>` |
| 403 | Forbidden (token valid tapi role tidak cukup) | Login ulang dengan akun yang benar, cek dengan GET /auth/me |
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

### Booking (POST /bookings) – butuh login (customer atau admin)
| Kolom | Tipe | Wajib | Contoh |
|-------|------|-------|--------|
| roomId | number | ✓ | 1 |
| checkInDate | string | ✓ | "2026-03-10" (YYYY-MM-DD) |
| checkOutDate | string | ✓ | "2026-03-12" (YYYY-MM-DD) |
| customerName | string | Opsional | "budi" (hanya admin, untuk booking atas nama customer) |

### Validasi Detail
- **HotelRequestDto:** name 1–100 karakter, location 1–200 karakter
- **RoomRequestDto:** roomNumber 1–20 karakter, type 1–50 karakter, price ≥ 0, hotelId harus ada
- **BookingRequestDto:** checkIn/checkOut format YYYY-MM-DD, checkOut > checkIn, tidak boleh tanggal lalu

---

## 9.5 Pencegahan Double Booking (Sudah Diimplementasi)

Backend **sudah mencegah** double booking. Room yang sudah dibooking tidak bisa dibooking lagi untuk tanggal yang bertumpuk.

**Mekanisme:**
- `GET /rooms/{id}/availability?checkIn=...&checkOut=...` → cek dulu sebelum tampilkan tombol booking
- `POST /bookings` → backend cek lagi; jika bentrok return **400** dengan pesan `"Room already booked for selected dates"`

**Instruksi untuk Frontend:**

1. **Sebelum user submit booking** → panggil `GET /rooms/{id}/availability?checkIn=...&checkOut=...`
   - Response `{"available": true}` → tampilkan tombol/form booking
   - Response `{"available": false}` → **disable tombol booking**, tampilkan pesan: *"Kamar tidak tersedia untuk tanggal ini. Pilih tanggal lain."*

2. **Saat user submit** → tetap kirim `POST /bookings`. Jika dapat **400**:
   - Parse message dari response (bisa `"Room already booked for selected dates"`)
   - Tampilkan toast/alert: *"Kamar sudah dibooking orang lain. Silakan pilih tanggal atau kamar lain."*
   - *(Race condition: 2 user bisa cek availability bersamaan; backend tetap tolak yang kedua)*

3. **Setiap kali user ganti tanggal** → panggil ulang `GET /rooms/{id}/availability` untuk update status.

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
   Response: `{"available": true}` atau `{"available": false}`

6. **Jika `available: true`** → tampilkan form konfirmasi & tombol booking. **Jika `available: false`** → disable tombol, tampilkan "Kamar tidak tersedia untuk tanggal ini"

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

5. **Role:** Customer dan admin bisa POST booking. Admin bisa kirim `customerName` untuk booking atas nama customer.

---

## 10.2 Troubleshooting: 403 Saat Admin Tambah Hotel

**Penyebab:** Token tidak dikirim, token salah, atau user bukan admin.

**Yang harus dicek frontend:**

1. **Login sebagai admin** (`admin` / `admin123`) sebelum tambah hotel
2. **Simpan token** dari response login (field `token`) – localStorage/sessionStorage
3. **Kirim header di SETIAP request CRUD** – pastikan `fetchApi` / axios menambahkan:
   ```js
   headers: {
     'Content-Type': 'application/json',
     'Authorization': `Bearer ${token}`  // token dari storage, TANPA tanda kutip
   }
   ```
4. **Cek fetchApi/booking.js** – untuk POST/PUT/DELETE hotel & room, wajib include header Authorization. Token diambil dari storage (setelah login).
5. **Verifikasi** dengan `GET /auth/me` + Authorization header → harus `groups: ["Admin"]` atau `["admin"]`

**Contoh fetchApi yang benar (untuk CRUD hotel):**
```js
// Simpan token setelah login
localStorage.setItem('token', response.token);

// Di fetchApi – untuk request yang butuh auth
const token = localStorage.getItem('token');
const headers = {
  'Content-Type': 'application/json',
  ...(token && { 'Authorization': `Bearer ${token}` })
};
fetch('http://localhost:8080/hotels', {
  method: 'POST',
  headers,
  body: JSON.stringify({ name: 'peninsula', location: 'medan' })
});
```

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

---

## 12. Detail Request/Response per Endpoint (Value & Payload)

### Auth

**POST /auth/login**
- Request:
```json
{
  "username": "admin",
  "password": "admin123"
}
```
- Response 200:
```json
{
  "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cG4iOiJhZG1pbiIsImdyb3VwcyI6WyJBZG1pbiJdLCJuYW1lIjoiQWRtaW5pc3RyYXRvciIsInVzZXJJZCI6MSwiaWF0IjoxNzA4NjAwMDAwLCJleHAiOjE3MDg2ODY0MDB9.xxx",
  "username": "admin",
  "name": "Administrator",
  "role": "admin"
}
```

**POST /auth/register**
- Request:
```json
{
  "username": "budi",
  "password": "rahasia123",
  "name": "Budi Santoso"
}
```
- Response 200: sama seperti login (termasuk token)

**GET /auth/me** (header: `Authorization: Bearer <token>`)
- Response 200:
```json
{
  "username": "admin",
  "name": "Administrator",
  "role": "admin",
  "groups": ["Admin"]
}
```

---

### Hotels

**GET /hotels?page=0&size=20**
- Response 200:
```json
{
  "content": [
    { "id": 1, "name": "Hotel Santai", "location": "Jakarta" },
    { "id": 2, "name": "Hotel Mewah", "location": "Bandung" }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 2,
  "totalPages": 1
}
```

**GET /hotels/1**
- Response 200:
```json
{
  "id": 1,
  "name": "Hotel Santai",
  "location": "Jakarta"
}
```

**POST /hotels** (header: `Authorization: Bearer <token>`)
- Request:
```json
{
  "name": "Hotel Peninsula",
  "location": "Jakarta"
}
```
- Response 200:
```json
{
  "id": 3,
  "name": "Hotel Peninsula",
  "location": "Jakarta"
}
```

**PUT /hotels/1** (header: `Authorization: Bearer <token>`)
- Request:
```json
{
  "name": "Hotel Santai Baru",
  "location": "Jakarta Selatan"
}
```
- Response 200: object hotel yang di-update

**DELETE /hotels/1** (header: `Authorization: Bearer <token>`)
- Response: 204 No Content (body kosong)

**GET /hotels/1/rooms?page=0&size=20**
- Response 200:
```json
{
  "content": [
    {
      "id": 1,
      "roomNumber": "101",
      "type": "DELUXE",
      "price": 500000,
      "hotel": { "id": 1, "name": "Hotel Santai", "location": "Jakarta" }
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

---

### Rooms

**GET /rooms?page=0&size=20**
- Response 200: format sama seperti GET /hotels/{id}/rooms (content array Room)

**GET /rooms/1**
- Response 200:
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

**POST /rooms** (header: `Authorization: Bearer <token>`)
- Request:
```json
{
  "roomNumber": "202",
  "type": "SUITE",
  "price": 1200000,
  "hotelId": 1
}
```
- Response 200: object room yang dibuat (termasuk nested hotel)

**PUT /rooms/1** (header: `Authorization: Bearer <token>`)
- Request: sama seperti POST (roomNumber, type, price, hotelId)
- Response 200: object room yang di-update

**DELETE /rooms/1** (header: `Authorization: Bearer <token>`)
- Response: 204 No Content

**GET /rooms/1/availability?checkIn=2026-03-10&checkOut=2026-03-12**
- Response 200:
```json
{
  "available": true
}
```

---

### Bookings

**GET /bookings?page=0&size=20** (Customer: otomatis filter milik sendiri; Admin: tambah `&customerName=budi` untuk filter)
- Response 200:
```json
{
  "content": [
    {
      "id": 1,
      "room": {
        "id": 1,
        "roomNumber": "101",
        "type": "DELUXE",
        "price": 500000,
        "hotel": { "id": 1, "name": "Hotel Santai", "location": "Jakarta" }
      },
      "customerName": "budi",
      "checkInDate": "2026-03-10",
      "checkOutDate": "2026-03-12",
      "status": "BOOKED"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

**POST /bookings** (header: `Authorization: Bearer <token>`)
- Request (Customer):
```json
{
  "roomId": 1,
  "checkInDate": "2026-03-10",
  "checkOutDate": "2026-03-12"
}
```
- Request (Admin untuk customer):
```json
{
  "roomId": 1,
  "checkInDate": "2026-03-10",
  "checkOutDate": "2026-03-12",
  "customerName": "budi"
}
```
- Response 200: object booking lengkap (room, customerName, checkInDate, checkOutDate, status)

**PUT /bookings/1/cancel** (header: `Authorization: Bearer <token>`)
- Response 200: object booking dengan `status: "CANCELLED"`

---

## 13. Tampilan Web yang Perlu Dibuat

### Halaman Umum (Tanpa Login)

| Halaman | Deskripsi | Komponen/Element |
|---------|-----------|------------------|
| **Landing / Home** | Daftar hotel | Card/list hotel (nama, lokasi), tombol "Lihat Kamar", pagination |
| **Detail Hotel** | Info hotel + daftar kamar | Nama hotel, lokasi, tabel/list kamar (no kamar, tipe, harga), tombol "Pesan" per kamar |
| **Detail Kamar** | Info kamar + form cek ketersediaan | No kamar, tipe, harga, nama hotel; input tanggal check-in & check-out; tombol "Cek Ketersediaan"; jika available → tombol "Booking" |
| **Login** | Form login | Input username, password; tombol "Login"; link "Daftar" |
| **Register** | Form daftar | Input username, password, name; tombol "Daftar" |

### Halaman Customer (Setelah Login)

| Halaman | Deskripsi | Komponen/Element |
|---------|-----------|------------------|
| **Form Booking** | Konfirmasi booking | Tampilkan: hotel, kamar, tanggal; tombol "Konfirmasi Booking" |
| **Riwayat Booking** | Daftar booking milik sendiri | Tabel: hotel, kamar, tanggal, status; tombol "Batalkan" (jika BOOKED) |
| **Detail Booking** | Detail 1 booking | Info lengkap, tombol "Batalkan" jika masih BOOKED |

### Halaman Admin (Setelah Login)

| Halaman | Deskripsi | Komponen/Element |
|---------|-----------|------------------|
| **Kelola Hotel** | CRUD hotel | Tabel daftar hotel; tombol "Tambah"; form tambah/edit (nama, lokasi); tombol Simpan, Batal, Hapus |
| **Kelola Kamar** | CRUD kamar per hotel | Pilih hotel; tabel kamar; tombol "Tambah"; form (no kamar, tipe, harga); tombol Simpan, Batal, Hapus |
| **Kelola Booking** | Lihat semua booking | Tabel: customer, hotel, kamar, tanggal, status; filter by customerName (opsional); tombol "Batalkan" |
| **Buat Booking untuk Customer** | Admin buat booking atas nama customer | Pilih customer (username), pilih kamar, pilih tanggal; tombol "Simpan" |

### Komponen Navigasi

| Element | Deskripsi |
|---------|-----------|
| **Header/Navbar** | Logo, menu (Home, Login/Logout), nama user + role jika login |
| **Role-based menu** | Customer: Home, Riwayat Booking; Admin: Kelola Hotel, Kelola Kamar, Kelola Booking |
| **Redirect** | Setelah login → redirect sesuai role (Customer: home/booking; Admin: kelola hotel) |

### Validasi UI yang Disarankan

| Field | Validasi |
|-------|----------|
| Tanggal | Format YYYY-MM-DD; checkOut > checkIn; tidak boleh tanggal lalu |
| Hotel name | 1–100 karakter |
| Location | 1–200 karakter |
| Room number | 1–20 karakter |
| Room type | 1–50 karakter (bisa dropdown: STANDARD, DELUXE, SUITE) |
| Price | Angka ≥ 0 |

### Ringkasan: Endpoint mana yang butuh header Authorization?

| Butuh Token? | Endpoint |
|--------------|----------|
| Tidak | GET /hotels, GET /hotels/{id}, GET /hotels/{id}/rooms, GET /rooms, GET /rooms/{id}, GET /rooms/{id}/availability, POST /auth/login, POST /auth/register |
| Ya | GET /auth/me, POST/PUT/DELETE /hotels, POST/PUT/DELETE /rooms, GET/POST /bookings, PUT /bookings/{id}/cancel |
