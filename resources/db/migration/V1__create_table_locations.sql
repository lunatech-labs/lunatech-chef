CREATE TABLE locations (
  uuid UUID NOT NULL,
  city TEXT NOT NULL,
  country TEXT NOT NULL,
  is_deleted Boolean NOT NULL,
  CONSTRAINT locations_pkey PRIMARY KEY (uuid)
);
