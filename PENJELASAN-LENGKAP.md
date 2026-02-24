# 📚 Penjelasan Lengkap Proyek Hotel Booking (Bahasa Bayi Edition)

Dokumen ini menjelaskan **semua** kode di proyek ini dengan bahasa sederhana, analogi, dan alasan kenapa pakai cara ini (bukan cara lain).

---

## 🎯 Analogi Besar: Proyek Ini Kayak Apa?

Bayangkan kita bikin **restoran**:

- **Database (PostgreSQL)** = Gudang tempat nyimpen bahan makanan
- **Entity** = Resep/resep masakan (struktur datanya)
- **Repository** = Tukang gudang yang ngambil/nyimpen barang
- **Service** = Koki yang masak (logic bisnis)
- **Resource** = Pelayan yang terima pesanan customer & kasih makanan
- **DTO** = Form pesanan yang customer isi

Customer pesan lewat pelayan (Resource) → Koki masak (Service) → Tukang gudang ambil bahan (Repository) → Data disimpan di gudang (Database).

---

# BAGIAN 1: STRUKTUR PROYEK

## 1.1 pom.xml – Daftar Bahan yang Kita Butuhkan

**Analogi:** Kayak daftar belanja. Kita bilang "aku butuh beras, minyak, garam" – Maven (tool) yang ngurus beli & nyiapin semuanya.

### Setiap Dependency (Bahan):

| Dependency | Fungsi | Kenapa Pakai Ini? | Alternatif? |
|------------|--------|-------------------|-------------|
| **quarkus-arc** | Dependency Injection – Quarkus yang otomatis "sambungin" class satu sama lain | Tanpa ini, kita harus manual `new HotelRepository()` di mana-mana. Ribet & gampang salah | Bisa manual new, tapi kode jadi berantakan |
| **quarkus-rest** | Bikin API REST (endpoint HTTP) | Standar untuk bikin backend API. `@GET`, `@POST` dll | Spring Web, Vert.x – Quarkus pilihannya untuk Java modern |
| **quarkus-rest-jackson** | Convert Java object ↔ JSON | Client kirim JSON, kita terima. Tanpa ini, error "no JSON extension" | quarkus-rest-jsonb – Jackson lebih populer |
| **quarkus-hibernate-validator** | Validasi input (wajib diisi, min/max, dll) | Cek data sebelum masuk logic. Satu tempat, pakai anotasi | Bisa if-else manual, tapi berulang & gampang lupa |
| **quarkus-hibernate-orm-panache** | ORM – Java object ↔ tabel database | Nulis `Hotel.findByName("x")` bukan SQL mentah. Panache = versi simpel Hibernate | JPA manual, JDBC – lebih ribet |
| **quarkus-jdbc-postgresql** | Driver koneksi ke PostgreSQL | Butuh "jembatan" Java ↔ PostgreSQL | H2, MySQL – BRD minta PostgreSQL |
| **quarkus-jdbc-h2** | Driver H2 (database in-memory) | Untuk testing tanpa perlu install PostgreSQL | Bisa hapus kalau ga dipake |
| **quarkus-smallrye-openapi** | Generate dokumentasi API (OpenAPI spec) | Otomatis bikin spec dari kode kita | Manual tulis YAML – ribet |
| **quarkus-swagger-ui** | UI untuk test API | Bisa coba API di browser tanpa Postman | Bisa pakai Postman aja |

---

## 1.2 application.properties – Konfigurasi

**Analogi:** Kayak pengaturan di HP. "WiFi nyala, volume 80%, bahasa Indonesia."

```
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=postgres
quarkus.datasource.password=postgres
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/hotel_db
```
= "Database kita PostgreSQL, user postgres, password postgres, alamat localhost port 5432, database namanya hotel_db"

```
quarkus.hibernate-orm.database.generation=update
```
= "Kalau tabel belum ada, bikin. Kalau udah ada tapi struktur berubah, update."  
**Alternatif:** `create` (hapus semua tiap restart), `create-drop` (hapus pas shutdown), `none` (manual bikin tabel). `update` paling aman untuk development.

```
quarkus.hibernate-orm.log.sql=true
```
= Tampilkan SQL yang dijalankan di log. Berguna untuk debug.

```
quarkus.swagger-ui.always-include=true
```
= Swagger UI tetap ada di production (biasanya cuma dev).

---

# BAGIAN 2: ENTITY (Model Database)

**Analogi:** Entity = Cetakan kue. Satu cetakan dipake buat bikin banyak kue (banyak baris di tabel).

## 2.1 Hotel.java

```java
@Entity
public class Hotel extends PanacheEntity {
    public String name;
    public String location;
}
```

- **@Entity** = "Ini tabel di database"
- **extends PanacheEntity** = Dapet `id` otomatis + method `findById()`, `listAll()`, `persist()`, dll
- **name, location** = Kolom di tabel

**Kenapa PanacheEntity?**  
Tanpa Panache, kita harus nulis `@Id @GeneratedValue Long id` dan bikin repository manual. Panache = kurang kode, lebih simpel.

**Alternatif?** Bisa pakai class biasa + JPA `@Entity` tanpa extend – tapi kode lebih banyak.

---

## 2.2 Room.java

```java
@ManyToOne
public Hotel hotel;
```

- **@ManyToOne** = Banyak Room bisa punya satu Hotel. Kayak banyak kamar di satu gedung hotel.
- Di database = kolom `hotel_id` (foreign key)

**Kenapa @ManyToOne?**  
Relasi: 1 Hotel punya banyak Room. Dari sisi Room = "aku (Room) punya 1 Hotel".  
**Alternatif @OneToMany?** Itu dari sisi Hotel ("aku punya banyak Room"). Kita butuh keduanya tergantung akses datanya dari mana.

---

## 2.3 Booking.java

```java
@ManyToOne
public Room room;
public String customerName;
public LocalDate checkInDate;
public LocalDate checkOutDate;
public String status; // BOOKED / CANCELLED
```

- **status** = Tracking: BOOKED (aktif) atau CANCELLED (batal)
- **LocalDate** = Tanggal tanpa jam (cukup untuk check-in/out)

**Kenapa status String bukan Enum?**  
Bisa pakai Enum. String lebih simpel untuk contoh. Di production Enum lebih rapi.

---

# BAGIAN 3: DTO (Data Transfer Object)

**Analogi:** DTO = Form yang customer isi. Kita ga langsung terima "benda asli" (Entity), tapi form dulu. Form ini yang kita validasi.

## 3.1 Kenapa Pakai DTO? Kenapa Ga Langsung Entity?

| Langsung Entity | Pakai DTO |
|-----------------|-----------|
| Client bisa kirim field aneh (id, createdAt) | Hanya field yang kita mau |
| Struktur Entity terikat ke database | Bisa beda (misal password ga dikirim) |
| Validasi campur dengan Entity | Validasi jelas di DTO |

**Analog:** Mau beli baju. Langsung Entity = kasih akses ke gudang. DTO = kasih form "ukuran, warna" aja.

---

## 3.2 BookingRequestDto

```java
public record BookingRequestDto(
    @NotNull(message = "roomId wajib diisi")
    Long roomId,
    @NotNull(message = "customerName wajib diisi")
    @Size(min = 1, max = 100)
    String customerName,
    ...
)
```

- **record** = Class khusus Java 16+ untuk data. Otomatis bikin constructor, getter, equals. Kurang boilerplate.
- **@NotNull** = Ga boleh null
- **@Size(min=1, max=100)** = Panjang 1–100 karakter
- **@FutureOrPresent** = Tanggal ga boleh kemarin

**Kenapa record?**  
Lebih ringkas dari class + constructor + getter manual.  
**Alternatif?** Class biasa – boleh, cuma lebih panjang.

---

## 3.3 PageResponse<T>

```java
public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages
)
```

- **<T>** = Generic. Bisa `PageResponse<Hotel>`, `PageResponse<Room>`, dll.
- **content** = Data di halaman ini
- **page, size** = Halaman keberapa, berapa item per halaman
- **totalElements, totalPages** = Total data & total halaman (untuk pagination di frontend)

**Kenapa Pagination?**  
Tanpa pagination, `GET /hotels` bisa return 10.000 hotel sekaligus → lambat & berat. Dengan pagination, ambil 20 aja per request.

---

# BAGIAN 4: REPOSITORY (Akses Database)

**Analogi:** Repository = Tukang gudang. Dia yang ngambil/nyimpen barang. Kita cuma bilang "ambil hotel id 5" – dia yang jalanin.

## 4.1 HotelRepository

```java
@ApplicationScoped
public class HotelRepository implements PanacheRepository<Hotel> {
    public List<Hotel> findAll(Page page) {
        return findAll().page(page).list();
    }
}
```

- **@ApplicationScoped** = Satu instance dipake bareng (singleton). Ga bikin baru tiap request.
- **implements PanacheRepository<Hotel>** = Dapet `findById()`, `persist()`, `deleteById()`, `count()`, dll gratis.
- **findAll(Page page)** = Ambil data dengan pagination.

**Kenapa PanacheRepository?**  
Tanpa Panache, kita nulis SQL/HQL manual. Panache bikin query dari method name.

---

## 4.2 BookingRepository – findActiveBookings

```java
return list(
    "room.id = ?1 and status = 'BOOKED' and " +
    "(checkInDate <= ?3 and checkOutDate >= ?2)",
    roomId, checkIn, checkOut
);
```

Ini cek: ada ga booking yang:
- room-nya sama
- status BOOKED
- tanggalnya bentrok (overlap)

**Logika overlap:**  
Booking A: 10–12 Maret. Customer mau: 11–13 Maret.  
Bentrok kalau: `checkInA <= checkOutCustomer` DAN `checkOutA >= checkInCustomer`  
= 10 <= 13 ✓ dan 12 >= 11 ✓ → bentrok.

**Kenapa query di Repository?**  
Ini akses data, bukan business rule. Repository = layer akses data.

---

# BAGIAN 5: SERVICE (Business Logic)

**Analogi:** Service = Koki. Dia yang nentuin "boleh ga pesanan ini", "gimana cara masaknya".

## 5.1 Kenapa Ada Service? Kenapa Ga di Resource Aja?

| Logic di Resource | Logic di Service |
|------------------|------------------|
| Resource jadi gemuk | Resource cuma terima request & return response |
| Susah dipake ulang | Bisa dipanggil dari Resource, Job, dll |
| Susah di-test | Service bisa di-test terpisah |

---

## 5.2 @Transactional

```java
@Transactional
public Booking createBooking(...) {
```

= "Semua operasi database dalam method ini = 1 transaksi. Kalau ada error di tengah, rollback semua."

**Analog:** Transfer uang: debit & kredit harus bareng. Kalau debit sukses tapi kredit gagal, uang hilang. Dengan transaction, kalau salah satu gagal, keduanya dibatalkan.

---

## 5.3 PESSIMISTIC_WRITE Lock

```java
Room room = entityManager.find(Room.class, roomId, LockModeType.PESSIMISTIC_WRITE);
```

**Masalah tanpa lock:**  
User A & B booking kamar yang sama, tanggal sama, bareng. Keduanya cek availability → keduanya dapat "available" → keduanya create booking → double booking.

**Dengan PESSIMISTIC_WRITE:**  
User A ambil Room dengan lock. User B nunggu. A selesai (commit/rollback), barulah B bisa akses. Saat B akses, data sudah update, availability cek lagi → dapat "not available".

**Alternatif?**  
- OPTIMISTIC: Cek versi, kalau berubah = conflict. Lebih ringan tapi bisa sering retry.
- Tanpa lock: Bisa double booking (race condition).

Kita pilih PESSIMISTIC karena booking critical – harus konsisten.

---

## 5.4 Validasi checkOut > checkIn

```java
if (checkOut.isBefore(checkIn) || checkOut.equals(checkIn)) {
    throw new WebApplicationException("checkOutDate harus setelah checkInDate", 400);
}
```

Bean Validation cek format & null. Tapi "checkOut > checkIn" itu logic bisnis, makanya di Service.

---

# BAGIAN 6: RESOURCE (REST API)

**Analogi:** Resource = Pelayan. Terima pesanan (HTTP request), panggil koki (Service), kasih hasil ke customer (HTTP response).

## 6.1 Dependency Injection lewat Constructor

```java
public HotelResource(HotelRepository hotelRepository, RoomRepository roomRepository) {
    this.hotelRepository = hotelRepository;
    this.roomRepository = roomRepository;
}
```

Quarkus (Arc) yang bikin object & inject. Kita ga `new HotelRepository()`.

**Kenapa constructor?**  
- Field jelas butuh apa
- Bisa `final` (immutable)
- Gampang di-test (bisa inject mock)

---

## 6.2 @Path, @GET, @POST, dll

```java
@Path("/hotels")
public class HotelResource {
    @GET
    public PageResponse<Hotel> getHotels(...)
    
    @GET
    @Path("/{id}")
    public Hotel getHotel(@PathParam("id") Long id)
    
    @POST
    public Hotel createHotel(@Valid HotelRequestDto dto)
```

- `@Path("/hotels")` = Base path
- `@GET` = HTTP GET
- `@Path("/{id}")` = Path parameter (e.g. `/hotels/5`)
- `@PathParam("id")` = Ambil value dari URL
- `@QueryParam("page")` = Ambil dari query (?page=0)
- `@Valid` = Jalankan validasi Bean Validation sebelum masuk method

---

## 6.3 @DefaultValue

```java
@QueryParam("page") @DefaultValue("0") int page
```

Kalau user ga kirim `?page=`, pakai 0. Supaya ga error.

---

## 6.4 WebApplicationException

```java
throw new WebApplicationException("Hotel not found", Response.Status.NOT_FOUND);
```

Return HTTP 404 + message. JAX-RS otomatis convert ke response.

---

## 6.5 Booking: Kenapa Service, Bukan Langsung Repository?

Create booking punya logic: cek availability, lock, validasi. Itu business logic → masuk Service.  
Hotel/Room CRUD simpel (ambil/simpan) → bisa langsung Repository di Resource.  
Booking lebih kompleks → wajib lewat Service.

---

# BAGIAN 7: ALTERNATIF & KEPUTUSAN

## Bisa Ga Pakai X? Kenapa Ga?

| Yang Kita Pakai | Alternatif | Kenapa Pilih Ini |
|-----------------|------------|------------------|
| Quarkus | Spring Boot | Quarkus lebih ringan, startup cepat, cocok cloud |
| Panache | JPA manual | Panache lebih sedikit kode |
| PostgreSQL | MySQL, H2 | Sesuai BRD |
| DTO | Langsung Entity | Keamanan & validasi lebih jelas |
| Record | Class | Lebih ringkas |
| PESSIMISTIC lock | OPTIMISTIC / tanpa lock | Booking butuh konsistensi kuat |
| Repository pattern | Logic di Resource | Separation of concern, lebih rapi |
| Bean Validation | If-else manual | Deklaratif, ga berulang |

---

# BAGIAN 8: ALUR REQUEST (End-to-End)

Contoh: `POST /bookings` dengan body JSON.

1. **HTTP Request** masuk ke Quarkus
2. **Jackson** (quarkus-rest-jackson) convert JSON → `BookingRequestDto`
3. **Bean Validation** cek DTO (@Valid). Kalau gagal → 400
4. **BookingResource.createBooking()** terima DTO
5. Resource panggil **BookingService.createBooking()**
6. Service pakai **EntityManager** + lock ambil Room
7. Service panggil **BookingRepository.findActiveBookings()** cek konflik
8. Kalau aman, Service bikin **Booking** entity & **persist** lewat Repository
9. **@Transactional** commit
10. Return **Booking** entity
11. **Jackson** convert → JSON response
12. HTTP 200 + body JSON ke client

---

# BAGIAN 9: CHECKLIST FILE & FUNGSI

| File | Fungsi |
|------|--------|
| **Hotel.java** | Tabel hotel (id, name, location) |
| **Room.java** | Tabel room (id, roomNumber, type, price, hotel_id) |
| **Booking.java** | Tabel booking (id, room_id, customerName, checkIn, checkOut, status) |
| **HotelRequestDto** | Input create/update hotel |
| **RoomRequestDto** | Input create/update room |
| **BookingRequestDto** | Input create booking |
| **PageResponse** | Wrapper response pagination |
| **HotelRepository** | CRUD + findAll pagination |
| **RoomRepository** | CRUD + findByHotel + countByHotel |
| **BookingRepository** | findActiveBookings, isRoomAvailable, findAll |
| **BookingService** | createBooking (dengan lock), cancelBooking |
| **HotelResource** | CRUD hotel + get rooms by hotel |
| **RoomResource** | CRUD room + availability check |
| **BookingResource** | CRUD booking + cancel |

---

# BAGIAN 10: KONSEP PENTING (Glosarium)

- **CRUD** = Create, Read, Update, Delete
- **REST** = Cara komunikasi client-server lewat HTTP (GET, POST, PUT, DELETE)
- **JSON** = Format data (key-value) untuk kirim data
- **ORM** = Object-Relational Mapping – Java object ↔ tabel database
- **Transaction** = Sekumpulan operasi yang harus berhasil semua atau gagal semua
- **Pagination** = Data dibagi per halaman (20 per request, bukan 10.000)
- **DTO** = Object untuk transfer data (bukan entity database)
- **Dependency Injection** = Framework yang inject object yang dibutuhkan (ga kita yang new)
- **Validation** = Cek data sebelum diproses
- **Lock** = Kunci akses supaya ga race condition

---

*Dokumen ini dibuat untuk pemula. Kalau ada yang masih belum jelas, tanya aja!*
