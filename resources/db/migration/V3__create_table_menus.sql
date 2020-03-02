CREATE TABLE menus (
  uuid UUID NOT NULL,
  name TEXT NOT NULL,
  is_deleted Boolean NOT NULL,
  CONSTRAINT menus_pkey PRIMARY KEY (uuid)
);
