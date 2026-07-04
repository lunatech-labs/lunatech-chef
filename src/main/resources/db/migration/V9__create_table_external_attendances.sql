CREATE TABLE external_attendances
(
    uuid              UUID     NOT NULL,
    schedule_uuid     UUID     NOT NULL,
    attendances_count SMALLINT NOT NULL default 0,
    is_deleted        BOOLEAN  NOT NULL,
    CONSTRAINT externalAttendances_pkey PRIMARY KEY (uuid),
    CONSTRAINT externalAttendances_schedules_fkey FOREIGN KEY (schedule_uuid) REFERENCES schedules (uuid) MATCH FULL
);
