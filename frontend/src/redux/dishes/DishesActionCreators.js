import * as ActionTypes from "./DishesActionTypes";
import { axiosInstance } from "../Axios";

export const fetchDishes = () => (dispatch) => {
  dispatch(dishesLoading(true));

  axiosInstance
    .get("/dishes")
    .then(function (response) {
      dispatch(showAllDishes(response));
    })
    .catch(function (error) {
      console.log("Failed loading dishes: " + error);
      dispatch(dishesLoadingFailed(error.message));
    });
};

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

  axiosInstance
    .post("/dishes", dishToAdd)
    .then((response) => {
      console.log("New dish added with response " + response);
      dispatch(fetchDishes());
    })
    .catch(function (error) {
      console.log("Failed adding dish: " + error);
      dispatch(dishAddingFailed(error.message));
    });
};

export const deleteDish = (dishUuid) => (dispatch) => {
  axiosInstance
    .delete("/dishes/" + dishUuid)
    .then((response) => {
      console.log("Dish deleted with response " + response);
      dispatch(fetchDishes());
    })
    .catch(function (error) {
      console.log("Failed removing dish: " + error);
      dispatch(dishDeletingFailed(error.message));
    });
};

export const dishesLoading = () => ({
  type: ActionTypes.DISHES_LOADING,
});

export const showAllDishes = (dishes) => ({
  type: ActionTypes.SHOW_ALL_DISHES,
  payload: dishes.data,
});

export const dishesLoadingFailed = (errmess) => ({
  type: ActionTypes.DISHES_LOADING_FAILED,
  payload: errmess,
});

export const dishAddingFailed = (errmess) => ({
  type: ActionTypes.ADD_NEW_DISH_FAILED,
  payload: errmess,
});

export const dishDeletingFailed = (errmess) => ({
  type: ActionTypes.DELETE_DISH_FAILED,
  payload: errmess,
});
