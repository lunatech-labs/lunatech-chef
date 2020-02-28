CREATE TABLE menus (
  uuid UUID NOT NULL,
  name TEXT NOT NULL,
  is_deleted Boolean DEFAULT false,
  CONSTRAINT menus_pkey PRIMARY KEY (uuid)
);
