import { createStore, combineReducers, applyMiddleware } from "redux";
import thunk from "redux-thunk";
import logger from "redux-logger";
import { Dishes } from "./Dishes";

export const ConfigureStore = () => {
  const store = createStore(
    combineReducers({
      dishes: Dishes,
    }),
    applyMiddleware(thunk, logger)
  );
  return store;
};
