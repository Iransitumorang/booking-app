# 📖 Siapa Punya Siapa? Room, Booking, Hotel – Penjelasan Lengkap dengan Analogi

Dokumen ini menjelaskan **siapa yang "punya" atau "mengelola"** setiap data di sistem, plus hubungan antar tabel dan kode. Pakai bahasa sederhana + analogi.

---

## 🏢 Analogi Besar: Sistem = Gedung Hotel Nyata

Bayangkan kita bikin **aplikasi untuk hotel beneran**:

| Di Dunia Nyata | Di Sistem Kita |
|----------------|----------------|
| Gedung hotel fisik | Tabel `hotel` |
| Kamar-kamar di dalam gedung | Tabel `room` |
| Buku reservasi di meja resepsionis | Tabel `booking` |
| Resepsionis / staff hotel | **Admin** |
| Tamu yang mau menginap | **Customer** |

---

# BAGIAN 1: SIAPA PUNYA SIAPA?

## 1.1 Hotel – Punya Siapa?

**Punya:** **Admin** (pengelola sistem / pemilik hotel)

**Analogi:**  
Hotel itu kayak **gedung** yang dibangun sama pemilik. Pemilik yang nentuin nama hotel, lokasi, mau buka atau tutup.

**Di sistem:**
- **Admin** yang **tambah** hotel baru (`POST /hotels`)
- **Admin** yang **ubah** data hotel (`PUT /hotels/{id}`)
- **Admin** yang **hapus** hotel (`DELETE /hotels/{id}`)
- **Customer** cuma bisa **lihat** daftar hotel (`GET /hotels`, `GET /hotels/{id}`)

**Kode:** `HotelResource.java`

```java
@POST
public Hotel createHotel(@Valid HotelRequestDto dto) {
    // Admin nambah hotel baru
}

@DELETE
@Path("/{id}")
public void deleteHotel(@PathParam("id") Long id) {
    // Admin hapus hotel
}
```

**Tabel `hotel` di database:**
| Kolom | Arti | Siapa yang isi? |
|-------|------|------------------|
| id | Nomor unik hotel | Sistem (otomatis) |
| name | Nama hotel (e.g. "Hotel Santai") | Admin |
| location | Lokasi (e.g. "Jakarta") | Admin |

---

## 1.2 Room (Kamar) – Punya Siapa?

**Punya:** **Admin** (dikelola oleh Admin, tapi secara data "milik" Hotel)

**Analogi:**  
Kamar itu kayak **ruangan di dalam gedung hotel**. Yang nentuin ada kamar apa aja, nomor kamar, tipe, harga = **pemilik/staff hotel (Admin)**. Tamu cuma **pilih** kamar yang udah ada.

**Di sistem:**
- **Admin** yang **tambah** kamar (`POST /rooms`)
- **Admin** yang **ubah** kamar (harga, tipe, dll) (`PUT /rooms/{id}`)
- **Admin** yang **hapus** kamar (`DELETE /rooms/{id}`)
- **Customer** cuma bisa **lihat** kamar (`GET /rooms`, `GET /hotels/{id}/rooms`) dan **cek ketersediaan** (`GET /rooms/{id}/availability`)

**Kode:** `RoomResource.java`

```java
@POST
public Room addRoom(@Valid RoomRequestDto dto) {
    // Admin nambah kamar baru ke hotel
    Hotel hotel = hotelRepository.findById(dto.hotelId());
    Room room = new Room();
    room.roomNumber = dto.roomNumber();
    room.type = dto.type();      // STANDARD, DELUXE, SUITE, dll
    room.price = dto.price();
    room.hotel = hotel;          // Kamar ini punya hotel mana
    roomRepository.persist(room);
    return room;
}
```

**Relasi:** Satu **Room** selalu punya satu **Hotel** (`hotel_id`). Banyak kamar bisa punya hotel yang sama.

**Tabel `room` di database:**
| Kolom | Arti | Siapa yang isi? |
|-------|------|------------------|
| id | Nomor unik kamar | Sistem |
| roomnumber | Nomor kamar (101, 202, dll) | Admin |
| type | Tipe (STANDARD, DELUXE, SUITE) | Admin |
| price | Harga per malam | Admin |
| hotel_id | Kamar ini di hotel mana | Admin (saat bikin kamar) |

---

## 1.3 Booking (Pemesanan) – Punya Siapa?

**Punya:** **Customer** (tamu yang pesan)

**Analogi:**  
Booking itu kayak **tiket reservasi**. Yang bikin = **tamu (Customer)**. Tamu yang isi nama, pilih kamar, pilih tanggal. Sistem yang catat dan kasih nomor konfirmasi.

**Di sistem:**
- **Customer** yang **bikin** booking (`POST /bookings`)
- **Customer** yang **batalin** booking dia sendiri (`PUT /bookings/{id}/cancel`)
- **Customer** & **Admin** bisa **lihat** booking (`GET /bookings`, `GET /bookings/{id}`)
- **Admin** bisa batalin booking siapa aja (logic sama, beda akses nanti kalau ada auth)

**Kode:** `BookingResource.java` + `BookingService.java`

```java
@POST
public Booking createBooking(@Valid BookingRequestDto request) {
    // Customer (atau Admin atas nama customer) bikin booking
    return bookingService.createBooking(
        request.roomId(),      // Kamar mana yang dipesan
        request.customerName(), // Siapa yang pesan
        request.checkInDate(),
        request.checkOutDate()
    );
}

@PUT
@Path("/{id}/cancel")
public Booking cancelBooking(@PathParam("id") Long id) {
    // Customer/Admin batalkan booking
    return bookingService.cancelBooking(id);
}
```

**Tabel `booking` di database:**
| Kolom | Arti | Siapa yang isi? |
|-------|------|------------------|
| id | Nomor konfirmasi booking | Sistem |
| room_id | Kamar yang dipesan | Customer (pilih) |
| customername | Nama pemesan | Customer |
| checkindate | Tanggal masuk | Customer |
| checkoutdate | Tanggal keluar | Customer |
| status | BOOKED / CANCELLED | Sistem (awal BOOKED, cancel = CANCELLED) |

**Catatan status:**  
Di BRD kita pakai `BOOKED` dan `CANCELLED`. Di screenshot kamu ada `CONFIRMED`, `PENDING`, `CANCELLED` – itu variasi. Kita tetep pakai `BOOKED` = aktif, `CANCELLED` = batal.

---

# BAGIAN 2: HUBUNGAN ANTAR DATA (RELASI)

## 2.1 Diagram Sederhana

```
HOTEL (1)
   │
   │ punya banyak
   ▼
ROOM (banyak)  ◄──────  BOOKING (banyak)
   │                        │
   │                        │ dipesan oleh
   │                        ▼
   │                   CUSTOMER (nama di customername)
   │
   └── hotel_id di Room = "kamar ini di hotel mana"
```

**Baca:**
- 1 Hotel punya banyak Room
- 1 Room bisa punya banyak Booking (beda tanggal)
- 1 Booking punya 1 Room + 1 customerName

---

## 2.2 Di Kode – Relasi Entity

**Hotel.java:**
```java
@Entity
public class Hotel extends PanacheEntity {
    public String name;
    public String location;
    // Hotel ga simpan list Room di sini (bisa, tapi kita akses lewat Room.hotel)
}
```

**Room.java:**
```java
@Entity
public class Room extends PanacheEntity {
    public String roomNumber;
    public String type;
    public double price;

    @ManyToOne
    public Hotel hotel;  // ← Room "punya" 1 Hotel. Di DB = kolom hotel_id
}
```

**Booking.java:**
```java
@Entity
public class Booking extends PanacheEntity {
    @ManyToOne
    public Room room;    // ← Booking "punya" 1 Room. Di DB = kolom room_id

    public String customerName;
    public LocalDate checkInDate;
    public LocalDate checkOutDate;
    public String status;
}
```

**@ManyToOne** = "banyak ke satu". Banyak Room ke 1 Hotel. Banyak Booking ke 1 Room.

---

# BAGIAN 3: DTO vs ENTITY – Siapa Isi Apa?

## 3.1 Request DTO = Form yang Diisi User

| DTO | Dipakai untuk | Diisi oleh |
|-----|---------------|------------|
| **HotelRequestDto** | Create/Update hotel | Admin |
| **RoomRequestDto** | Create/Update room | Admin |
| **BookingRequestDto** | Create booking | Customer |

**BookingRequestDto** isinya:
- `roomId` – Customer pilih kamar mana
- `customerName` – Customer isi nama
- `checkInDate`, `checkOutDate` – Customer pilih tanggal

Ga ada `id` atau `status` – itu yang sistem yang set.

---

## 3.2 Response / Entity = Data yang Dikembalikan

Saat `GET /bookings/1`, response-nya object **Booking** lengkap:
- `id` – dari sistem
- `room` – object Room (bisa nested)
- `customerName`, `checkInDate`, `checkOutDate`, `status`

---

# BAGIAN 4: ALUR LENGKAP – Siapa Ngapain?

## 4.1 Setup Awal (Admin)

1. Admin `POST /hotels` → bikin Hotel Santai, Hotel Mewah, dll
2. Admin `POST /rooms` → bikin kamar 101, 202, dll per hotel

**Hasil:** Tabel `hotel` dan `room` terisi.

---

## 4.2 Customer Mau Booking

1. Customer `GET /hotels` → lihat daftar hotel
2. Customer `GET /hotels/1` → lihat detail Hotel Santai
3. Customer `GET /hotels/1/rooms` → lihat kamar di Hotel Santai
4. Customer `GET /rooms/5` → lihat detail kamar 5
5. Customer `GET /rooms/5/availability?checkIn=2026-03-01&checkOut=2026-03-03` → cek kosong atau enggak
6. Kalau available, Customer `POST /bookings` dengan body:
   ```json
   {
     "roomId": 5,
     "customerName": "Iran",
     "checkInDate": "2026-03-01",
     "checkOutDate": "2026-03-03"
   }
   ```
7. Sistem bikin baris baru di tabel `booking`, status `BOOKED`
8. Customer `GET /bookings?customerName=Iran` → lihat booking dia
9. Kalau mau batal, Customer `PUT /bookings/1/cancel` → status jadi `CANCELLED`

---

## 4.3 Admin Ngapain?

- Kelola Hotel & Room (CRUD)
- Lihat semua booking (`GET /bookings`)
- Bisa batalin booking (pakai endpoint yang sama `PUT /bookings/{id}/cancel`)

*Catatan: Sekarang belum ada login/role. Semua endpoint bisa diakses siapa aja. Auth & role (Admin vs Customer) biasanya ditambah belakangan.*

---

# BAGIAN 5: RINGKASAN TABEL

| Data | "Punya" / Dikelola | Create | Read | Update | Delete |
|------|--------------------|--------|------|--------|--------|
| **Hotel** | Admin | Admin | Semua | Admin | Admin |
| **Room** | Admin (milik Hotel) | Admin | Semua | Admin | Admin |
| **Booking** | Customer (yang pesan) | Customer | Semua | Cancel: Customer/Admin | - (kita ga delete, cuma cancel) |

---

# BAGIAN 6: KONEKSI KE GAMBAR / SCREENSHOT KAMU

## 6.1 Tabel Hotel

- `id=1, name=Hotel Santai, location=Jakarta` → Admin yang input
- Ini data yang Customer lihat pas `GET /hotels`

## 6.2 Tabel Room

- `id=1, roomnumber=101, type=DELUXE, price=500000, hotel_id=1`
- `hotel_id=1` = kamar ini di Hotel Santai
- Admin yang bikin kamar ini lewat `POST /rooms`

## 6.3 Tabel Booking

- `id=1, customername=Iran, room_id=1, checkindate=24-02-2026, checkoutdate=26-02-2026, status=CONFIRMED`
- **Iran (Customer)** yang bikin booking ini
- **room_id=1** = kamar yang dipesan
- **status** = sistem yang set (BOOKED/CANCELLED di kode kita)

## 6.4 API Endpoints di Swagger

- **Bookings** → Customer bikin/lihat/batal booking
- **Hotels** → Admin kelola hotel, Customer cuma lihat
- **Rooms** → Admin kelola kamar, Customer lihat + cek availability

---

# BAGIAN 7: GLOSARIUM SINGKAT

| Istilah | Arti |
|---------|------|
| **Admin** | Pengelola sistem / staff hotel |
| **Customer** | Tamu yang booking kamar |
| **Entity** | Class yang map ke tabel database |
| **DTO** | Object untuk kirim/terima data (request/response) |
| **CRUD** | Create, Read, Update, Delete |
| **@ManyToOne** | Relasi "banyak ke satu" (banyak Room ke 1 Hotel) |

---

*Kalau masih ada yang belum jelas, tanya aja bagian mana.*
