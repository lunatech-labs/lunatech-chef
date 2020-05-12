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

export const addNewDish = (newDish) => (dispatch) => {
  let dishToAdd = {
    name: newDish.name,
    description: newDish.description,
    isVegetarian: newDish.isVegetarian,
    hasNuts: newDish.hasNuts,
    hasSeafood: newDish.hasSeafood,
    hasPork: newDish.hasPork,
    hasBeef: newDish.hasBeef,
    isGlutenFree: newDish.isGlutenFree,
    hasLactose: newDish.hasLactose,
  };

  axios
    .post(baseUrl + "/dishes", dishToAdd)
    .then((response) => {
      console.log("New dish added with response " + response);
      dispatch(fetchDishes());
    })
    .catch(function (error) {
      console.log("Failed adding dish: " + error);
      dispatch(dishesFailed(error));
    });
};

export const deleteDish = (dishUuid) => (dispatch) => {
  axios
    .delete(baseUrl + "/dishes/" + dishUuid)
    .then((response) => {
      console.log("Dish deleted with response" + response);
      dispatch(fetchDishes());
    })
    .catch(function (error) {
      console.log("Failed removing dish: " + error);
      dispatch(dishesFailed(error));
    });
};
