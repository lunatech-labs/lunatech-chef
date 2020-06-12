import * as ActionTypes from "./DishesActionTypes";
import { axiosInstance } from "../Axios";

export const fetchDishes = () => (dispatch) => {
  dispatch(dishesLoading(true));

  axiosInstance
    .get("/dishes")
    .then(function (response) {
      console.log(JSON.stringify(response));
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

  axiosInstance
    .post("/dishes", dishToAdd)
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
  axiosInstance
    .delete("/dishes/" + dishUuid)
    .then((response) => {
      console.log("Dish deleted with response" + response);
      dispatch(fetchDishes());
    })
    .catch(function (error) {
      console.log("Failed removing dish: " + error);
      dispatch(dishesFailed(error));
    });
};
