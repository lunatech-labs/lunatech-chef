CREATE TABLE dishes_on_menus (
  uuid UUID NOT NULL,
  menu_uuid UUID NOT NULL,
  dish_uuid UUID NOT NULL,
  is_deleted BOOLEAN NOT NULL,
  CONSTRAINT dishes_on_menus_pkey PRIMARY KEY (uuid),
  CONSTRAINT dishes_on_menus_menus_fkey FOREIGN KEY (menu_uuid) REFERENCES menus (uuid) MATCH FULL,
  CONSTRAINT dishes_on_menus_dishes_fkey FOREIGN KEY (dish_uuid) REFERENCES dishes (uuid) MATCH FULL
);
