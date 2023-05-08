CREATE TABLE dishes
(
    uuid            UUID         NOT NULL,
    name            VARCHAR(100) NOT NULL,
    description     VARCHAR(200) NOT NULL,
    is_vegetarian   BOOLEAN      NOT NULL,
    is_halal        BOOLEAN      NOT NULL,
    has_nuts        BOOLEAN      NOT NULL,
    has_seafood     BOOLEAN      NOT NULL,
    has_pork        BOOLEAN      NOT NULL,
    has_beef        BOOLEAN      NOT NULL,
    is_gluten_free  BOOLEAN      NOT NULL,
    is_lactose_free BOOLEAN      NOT NULL,
    is_deleted      BOOLEAN      NOT NULL,
    CONSTRAINT dishes_pkey PRIMARY KEY (uuid)
);
