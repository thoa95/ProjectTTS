CREATE TABLE seat_registration
(
     seat_registration_id BIGSERIAL NOT NULL,
     room_id BIGINT NOT NULL,
     person_id BIGINT NOT NULL,
     start_time TIMESTAMP NOT NULL,
     end_time TIMESTAMP NOT NULL,
     seat_registration_status INT NOT NULL,
     seat_registration_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     PRIMARY KEY (seat_registration_id),
     FOREIGN KEY (room_id) REFERENCES room (room_id),
     FOREIGN KEY (person_id) REFERENCES person (person_id)
);

COMMENT ON COLUMN seat_registration.seat_registration_id IS 'Table ID of the seat registration, primary key';
COMMENT ON COLUMN seat_registration.room_id IS 'The ID of the room. This is a foreign key that references the `room` table.';
COMMENT ON COLUMN seat_registration.person_id IS 'The ID of the person. This is a foreign key that references the `person` table.';
COMMENT ON COLUMN seat_registration.seat_registration_time IS 'The time of the seat registration.';
COMMENT ON COLUMN seat_registration.seat_registration_status IS 'The status of the seat registration. 0 - Canceled: Đã bị hủy, 1 - Registed: Đã đăng ký';
COMMENT ON COLUMN seat_registration.start_time IS 'Time to start using seats in the room';
COMMENT ON COLUMN seat_registration.end_time IS 'End time for using seats in the room';

