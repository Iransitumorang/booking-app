-- Query untuk tambah data hotel manual (jika perlu)
-- Jalankan di psql/DBeaver setelah app jalan. Ganti UUID sesuai id hotel yang ada.
-- Atau pakai API: POST /hotels dengan Authorization: Bearer <token>
-- Body: {"name":"Hotel Baru","location":"Surabaya"}

-- Contoh insert via SQL (jika tabel kosong)
INSERT INTO "Hotel" (id, name, location) VALUES
  (gen_random_uuid(), 'Hotel Santai', 'Jakarta'),
  (gen_random_uuid(), 'Hotel Mewah', 'Bandung'),
  (gen_random_uuid(), 'Hotel Medan', 'Medan');

-- Tambah room (ganti <hotel_id> dengan UUID dari hotel di atas)
-- INSERT INTO "Room" (id, roomNumber, type, price, hotel_id) VALUES
--   (gen_random_uuid(), '101', 'STANDARD', 500000, '<hotel_id>');
