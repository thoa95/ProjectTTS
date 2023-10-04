CREATE TABLE person
(
    person_id BIGSERIAL NOT NULL PRIMARY KEY,
    fullname VARCHAR(255) NOT NULL,
    age INT NOT NULL,
);

COMMENT ON COLUMN person.person_id IS 'Table Id, primary key, auto increment';
COMMENT ON COLUMN person.fullname IS 'The full name of the person';
COMMENT ON COLUMN person.age IS 'The age of the person';
