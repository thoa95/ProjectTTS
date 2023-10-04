CREATE TABLE  meeting_schedule
(
    meeting_id BIGSERIAL NOT NULL ,
    room_id BIGINT NOT NULL,
    person_id  BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status_meeting INT NOT NULL ,
    reservation_time TIMESTAMP  NOT NULL,
    PRIMARY KEY (meeting_id),
    FOREIGN KEY (room_id) REFERENCES room (room_id),
    FOREIGN KEY (person_id) REFERENCES person (person_id)
);

COMMENT ON COLUMN meeting_schedule.meeting_id IS 'Table Id, primary key';
COMMENT ON COLUMN meeting_schedule.room_id IS 'The ID of the room. This is a foreign key that references the `room` table.';
COMMENT ON COLUMN meeting_schedule.person_id IS 'The ID of the user. This is a foreign key that references the `person` table.';
COMMENT ON COLUMN meeting_schedule.title IS 'The title of the meeting';
COMMENT ON COLUMN meeting_schedule.start_time IS 'The start time of the meeting';
COMMENT ON COLUMN meeting_schedule.start_time IS 'The end time of the meeting';
COMMENT ON COLUMN meeting_schedule.status_meeting IS 'The status of the meeting, 0 - Cancelled: Đã bị hủy, 1 - Scheduled: Đã lên lịch';
COMMENT ON COLUMN meeting_schedule.reservation_time IS 'The time of the reservation.';

