CREATE TABLE room
(
    room_id SERIAL NOT NULL PRIMARY KEY ,
    room_name VARCHAR(255) NOT NULL,
    capacity INT NOT NULL,
    status_room INT NOT NULL
);

COMMENT ON COLUMN room.room_id IS 'Table Id, primary key, auto increment';
COMMENT ON COLUMN room.room_name IS 'The name of the room. Must not be null';
COMMENT ON COLUMN room.capacity IS  'The capacity of the room, in units';
COMMENT ON COLUMN room.status_room IS 'The status of the room, 0 - Cancelled, 1 - Unvailable, 2 - Available'

