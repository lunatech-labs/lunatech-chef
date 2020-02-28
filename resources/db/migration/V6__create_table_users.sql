 CREATE TABLE users (
  uuid UUID NOT NULL,
  name TEXT NOT NULL,
  email_address TEXT NOT NULL,
  is_admin BOOLEAN NOT NULL DEFAULT FALSE,
  location UUID NOT NULL,
  is_inactive BOOLEAN NOT NULL DEFAULT FALSE,
  is_vegetarian BOOLEAN NOT NULL DEFAULT FALSE,
  has_nuts_restriction BOOLEAN NOT NULL DEFAULT FALSE,
  has_seafood_restriction BOOLEAN NOT NULL DEFAULT FALSE,
  has_pork_restriction BOOLEAN NOT NULL DEFAULT FALSE,
  has_beef_restriction BOOLEAN NOT NULL DEFAULT FALSE,
  is_gluten_intolerant BOOLEAN NOT NULL DEFAULT FALSE,
  is_lactose_intolerant BOOLEAN NOT NULL DEFAULT FALSE,
  other_restriction TEXT,
  is_deleted Boolean DEFAULT false,
  CONSTRAINT users_pkey PRIMARY KEY (uuid),
  CONSTRAINT users_locations FOREIGN KEY (location) REFERENCES locations (uuid) MATCH FULL
);

