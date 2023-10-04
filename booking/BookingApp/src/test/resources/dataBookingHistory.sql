INSERT INTO room (room_id, room_name, status_room, capacity, created_at, deleted_at)
VALUES (1, 'Room A', 2, 30, '2023-09-20 14:00:00', null),
       (2, 'Room B', 2, 40, '2023-09-20 14:00:00', null);

INSERT INTO person (person_id, age, fullname)
VALUES (1, 28, 'John');

INSERT INTO meeting_schedule
(meeting_id, room_id, person_id, title, start_time, end_time, reservation_time, status_meeting)
VALUES (100, 1, 1, 'Meeting 1', '2023-09-22 10:00:00', DATEADD(HOUR, 2, current_timestamp()), '2023-05-12 09:00:00', 1),
       (200, 2, 1, 'Meeting 2', '2023-09-19 14:00:00', '2023-09-19 15:00:00', '2023-05-12 13:00:00', 0),
       (300, 2, 1, 'Meeting 3', '2023-08-18 16:00:00', '2023-08-18 17:00:00', '2023-05-12 15:30:00', 1);
