CREATE TABLE attendance (
  uuid UUID NOT NULL,
  schedule_uuid UUID NOT NULL,
  user_uuid UUID NOT NULL,
  is_attending BOOLEAN DEFAULT false,
  CONSTRAINT attendance_pkey PRIMARY KEY (uuid),
  CONSTRAINT attendance_schedules_fkey FOREIGN KEY (schedule_uuid) REFERENCES schedules (uuid) MATCH FULL,
  CONSTRAINT attendance_users_fkey FOREIGN KEY (user_uuid) REFERENCES users (uuid) MATCH FULL
);
