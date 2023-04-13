CREATE TABLE offices
(
    uuid       UUID        NOT NULL,
    city       VARCHAR(50) NOT NULL,
    country    VARCHAR(50) NOT NULL,
    is_deleted BOOLEAN     NOT NULL,
    CONSTRAINT offices_pkey PRIMARY KEY (uuid)
);
