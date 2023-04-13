CREATE TABLE menus
(
    uuid       UUID         NOT NULL,
    name       VARCHAR(100) NOT NULL,
    is_deleted BOOLEAN      NOT NULL,
    CONSTRAINT menus_pkey PRIMARY KEY (uuid)
);
