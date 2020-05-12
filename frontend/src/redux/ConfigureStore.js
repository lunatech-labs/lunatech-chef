import { createStore, combineReducers, applyMiddleware } from "redux";
import { createForms } from "react-redux-form";
import thunk from "redux-thunk";
import logger from "redux-logger";
import { DishesReducer } from "./dishes/DishesReducer";
import { LocationsReducer } from "./locations/LocationsReducer";
import { MenusReducer } from "./menus/MenusReducer";
import { EmptyLocation } from "./locations/LocationForm";
import { EmptyDish } from "./dishes/DishForm";
import { EmptyMenu } from "./menus/MenuForm";

export const ConfigureStore = () => {
  const store = createStore(
    combineReducers({
      locations: LocationsReducer,
      dishes: DishesReducer,
      menus: MenusReducer,
      ...createForms({
        newLocation: EmptyLocation,
        newDish: EmptyDish,
        newMenu: EmptyMenu,
      }),
    }),
    applyMiddleware(thunk, logger)
  );
  return store;
};
