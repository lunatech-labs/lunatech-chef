import { createStore, combineReducers, applyMiddleware } from "redux";
import { createForms } from "react-redux-form";
import thunk from "redux-thunk";
import logger from "redux-logger";
import { DishesReducer } from "./dishes/DishesReducer";
import { LocationsReducer } from "./locations/LocationsReducer";
import { EmptyLocation } from "./locations/LocationForm";
import { EmptyDish } from "./dishes/DishForm";

export const ConfigureStore = () => {
  const store = createStore(
    combineReducers({
      locations: LocationsReducer,
      dishes: DishesReducer,
      ...createForms({ newLocation: EmptyLocation, newDish: EmptyDish }),
    }),
    applyMiddleware(thunk, logger)
  );
  return store;
};
