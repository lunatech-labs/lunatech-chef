CREATE TABLE attendances (
  uuid UUID NOT NULL,
  schedule_uuid UUID NOT NULL,
  user_uuid UUID NOT NULL,
  is_attending BOOLEAN NOT NULL,
  CONSTRAINT attendances_pkey PRIMARY KEY (uuid),
  CONSTRAINT attendances_schedules_fkey FOREIGN KEY (schedule_uuid) REFERENCES schedules (uuid) MATCH FULL,
  CONSTRAINT attendances_users_fkey FOREIGN KEY (user_uuid) REFERENCES users (uuid) MATCH FULL
);
