CREATE TABLE users
(
    uuid                    UUID         NOT NULL,
    name                    VARCHAR(100) NOT NULL,
    email_address           VARCHAR(100) NOT NULL,
    location_uuid           UUID,
    is_vegetarian           BOOLEAN      NOT NULL DEFAULT FALSE,
    has_halal_restriction   BOOLEAN      NOT NULL DEFAULT FALSE,
    has_nuts_restriction    BOOLEAN      NOT NULL DEFAULT FALSE,
    has_seafood_restriction BOOLEAN      NOT NULL DEFAULT FALSE,
    has_pork_restriction    BOOLEAN      NOT NULL DEFAULT FALSE,
    has_beef_restriction    BOOLEAN      NOT NULL DEFAULT FALSE,
    is_gluten_intolerant    BOOLEAN      NOT NULL DEFAULT FALSE,
    is_lactose_intolerant   BOOLEAN      NOT NULL DEFAULT FALSE,
    other_restrictions      VARCHAR(100),
    is_inactive             BOOLEAN      NOT NULL DEFAULT FALSE,
    is_deleted              BOOLEAN      NOT NULL,
    CONSTRAINT users_pkey PRIMARY KEY (uuid),
    CONSTRAINT users_locations FOREIGN KEY (location_uuid) REFERENCES locations (uuid) MATCH FULL
);

