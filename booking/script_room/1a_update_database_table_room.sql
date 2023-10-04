-- Add the created_at column with a default value of the current timestamp
ALTER TABLE room
ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Add the updated_at column
ALTER TABLE room
ADD COLUMN updated_at TIMESTAMP;

-- Add the deleted_at column
ALTER TABLE room
ADD COLUMN deleted_at TIMESTAMP;

COMMENT ON COLUMN room.created_at IS 'The timestamp when the room was created';
COMMENT ON COLUMN room.updated_at IS 'The timestamp when the room was last updated';
COMMENT ON COLUMN room.deleted_at IS 'The timestamp when the room was deleted (if applicable)';
