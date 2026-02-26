-- WAJIB jalankan ini agar room/booking/hotel pakai UUID (schema baru)
-- Tabel lama pakai integer id & hotel_id → tidak cocok dengan backend UUID
-- psql -U postgres -d hotel_db -f migrate-to-uuid.sql
DROP TABLE IF EXISTS booking CASCADE;
DROP TABLE IF EXISTS room CASCADE;
DROP TABLE IF EXISTS hotel CASCADE;
DROP TABLE IF EXISTS "Booking" CASCADE;
DROP TABLE IF EXISTS "Room" CASCADE;
DROP TABLE IF EXISTS "Hotel" CASCADE;
DROP TABLE IF EXISTS "booking" CASCADE;
DROP TABLE IF EXISTS "room" CASCADE;
DROP TABLE IF EXISTS "hotel" CASCADE;
DROP TABLE IF EXISTS users CASCADE;
