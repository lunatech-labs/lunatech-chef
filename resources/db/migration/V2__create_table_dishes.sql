CREATE TABLE dishes (
  uuid UUID NOT NULL,
  name TEXT NOT NULL,
  description TEXT,
  is_vegetarian BOOLEAN NOT NULL DEFAULT FALSE,
  has_seafood BOOLEAN NOT NULL DEFAULT FALSE,
  has_pork BOOLEAN NOT NULL DEFAULT FALSE,
  has_beef BOOLEAN NOT NULL DEFAULT FALSE,
  is_gluten_free BOOLEAN NOT NULL DEFAULT FALSE,
  has_lactose BOOLEAN NOT NULL DEFAULT FALSE,
  is_deleted Boolean DEFAULT false,
  CONSTRAINT dishes_pkey PRIMARY KEY (uuid)
);
