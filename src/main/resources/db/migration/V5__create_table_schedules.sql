 CREATE TABLE schedules (
  uuid UUID NOT NULL,
  menu_uuid UUID NOT NULL,
  date Date NOT NULL,
  location_uuid UUID NOT NULL,
  is_deleted BOOLEAN NOT NULL,
  CONSTRAINT schedules_pkey PRIMARY KEY (uuid),
  CONSTRAINT schedules_locations FOREIGN KEY (location_uuid) REFERENCES locations (uuid) MATCH FULL,
  CONSTRAINT schedules_menus_fkey FOREIGN KEY (menu_uuid) REFERENCES menus (uuid) MATCH FULL
);
