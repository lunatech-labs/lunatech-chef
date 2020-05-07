import * as ActionTypes from "./DishesActionTypes";
import { baseUrl } from "../../shared/baseUrl";

const axios = require("axios").default;

export const fetchDishes = () => (dispatch) => {
  dispatch(dishesLoading(true));

  axios
    .get(baseUrl + "/dishes")
    .then(function (response) {
      dispatch(showAllDishes(response));
    })
    .catch(function (error) {
      console.log("Failed loading dishes:" + error);
      dispatch(dishesFailed(error.message));
    });
};

export const dishesLoading = () => ({
  type: ActionTypes.DISHES_LOADING,
});

export const dishesFailed = (errmess) => ({
  type: ActionTypes.DISHES_LOADING_FAILED,
  payload: errmess,
});

export const showAllDishes = (dishes) => ({
  type: ActionTypes.SHOW_ALL_DISHES,
  payload: dishes,
});
