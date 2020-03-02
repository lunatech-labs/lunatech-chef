CREATE TABLE dishes (
  uuid UUID NOT NULL,
  name TEXT NOT NULL,
  description TEXT,
  is_vegetarian BOOLEAN NOT NULL,
  has_seafood BOOLEAN NOT NULL,
  has_pork BOOLEAN NOT NULL,
  has_beef BOOLEAN NOT NULL,
  is_gluten_free BOOLEAN NOT NULL,
  has_lactose BOOLEAN NOT NULL,
  is_deleted Boolean NOT NULL,
  CONSTRAINT dishes_pkey PRIMARY KEY (uuid)
);
