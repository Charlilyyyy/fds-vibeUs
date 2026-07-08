-- Create logical databases for each VibeUs service.
-- Mounted into the Postgres container on first init only
-- (empty data volume). Existing volumes are not re-initialized.

CREATE DATABASE vibeus_auth_db;
CREATE DATABASE vibeus_user_db;
CREATE DATABASE vibeus_music_db;
