INSERT INTO room (room_id, room_name, capacity, created_at, deleted_at, status_room)
VALUES (1, 'Room A', 3, '2023-09-20 14:00:00', null, 2),
       (2, 'Room B', 4, '2023-09-20 14:00:00', null, 2),
       (3, 'Room C', 5, '2023-09-20 14:00:00', '2023-08-12 14:00:00', 2),
       (4, 'Room D', 6, '2023-09-20 14:00:00', '2023-08-12 14:00:00', 2);

INSERT INTO person (person_id, age, fullname)
VALUES (1, 28, 'John');

INSERT INTO seat_registration (seat_registration_id, room_id, person_id, start_time, end_time, seat_registration_time,
                               seat_registration_status)
VALUES (100, 1, 1, '2023-09-25 15:30:00', DATEADD(HOUR, 2, current_timestamp()), '2023-09-18 04:10:13.054498', 1),
       (200, 1, 1, '2023-09-25 08:30:00', DATEADD(HOUR, 4, current_timestamp()), '2023-09-18 04:10:13.054498', 1),
       (300, 3, 1, '2023-09-25 08:30:00', DATEADD(HOUR, 6, current_timestamp()), '2023-09-18 04:10:13.054498', 1),
       (400, 4, 1, '2023-09-25 08:30:00', DATEADD(HOUR, 8, current_timestamp()), '2023-09-18 04:10:13.054498', 0);