-- Fix sequence out of sync (run sekali saat error duplicate key)
-- psql -U postgres -d hotel_db -f fix-room-sequence.sql
DO $$
DECLARE
  seq_name text;
  maxid bigint;
  tbl text := 'Room';
BEGIN
  seq_name := pg_get_serial_sequence('"' || tbl || '"', 'id');
  IF seq_name IS NULL THEN
    seq_name := pg_get_serial_sequence(tbl, 'id');
  END IF;
  IF seq_name IS NULL THEN
    seq_name := 'room_id_seq';
  END IF;
  EXECUTE format('SELECT COALESCE(MAX(id), 1) FROM "%s"', tbl) INTO maxid;
  EXECUTE format('SELECT setval(%L::regclass, %s)', seq_name, maxid);
  RAISE NOTICE 'Sequence % set to %', seq_name, maxid;
END $$;
