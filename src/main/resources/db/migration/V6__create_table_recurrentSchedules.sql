CREATE TABLE recurrent_schedules
(
    uuid            UUID    NOT NULL,
    menu_uuid       UUID    NOT NULL,
    location_uuid   UUID    NOT NULL,
    repetition_days INT     NOT NULL,
    next_date       Date    NOT NULL,
    is_deleted      BOOLEAN NOT NULL,
    CONSTRAINT recurrentSchedules_pkey PRIMARY KEY (uuid),
    CONSTRAINT recurrentSchedules_menu FOREIGN KEY (menu_uuid) REFERENCES menus (uuid) MATCH FULL,
    CONSTRAINT recurrentSchedules_locations FOREIGN KEY (location_uuid) REFERENCES locations (uuid) MATCH FULL
)
