import { createStore, combineReducers, applyMiddleware } from "redux";
import thunk from "redux-thunk";
import logger from "redux-logger";
import { Dishes } from "./Dishes";
import { Locations } from "./Locations";

export const ConfigureStore = () => {
  const store = createStore(
    combineReducers({
      locations: Locations,
      dishes: Dishes,
    }),
    applyMiddleware(thunk, logger)
  );
  return store;
};
