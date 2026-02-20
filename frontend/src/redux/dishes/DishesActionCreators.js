import { allDishesLoading, allDishesShown, allDishesLoadingFailed, dishAddedFailed, dishEditedFailed, dishDeletedFailed } from "./DishesSlice";
import { axiosInstance } from "../Axios";
import { fetchMenus } from "../menus/MenusActionCreators";
import { fetchAttendanceUser } from "../attendance/AttendanceActionCreators";
import { STORAGE_USER_UUID } from "../LocalStorageKeys";

export const fetchDishes = () => (dispatch) => {
  dispatch(allDishesLoading());

  axiosInstance
    .get("/dishes")
    .then(function (response) {
      dispatch(allDishesShown(response.data));
    })
    .catch(function (error) {
      console.log("Failed loading dishes: " + error);
      dispatch(allDishesLoadingFailed(error.message));
    });
};

export const addNewDish = (newDish) => (dispatch) => {
  let dishToAdd = {
    name: newDish.name,
    description: newDish.description,
    isVegetarian: newDish.isVegetarian,
    isHalal: newDish.isHalal,
    hasNuts: newDish.hasNuts,
    hasSeafood: newDish.hasSeafood,
    hasPork: newDish.hasPork,
    hasBeef: newDish.hasBeef,
    isGlutenFree: newDish.isGlutenFree,
    isLactoseFree: newDish.isLactoseFree,
  };

  axiosInstance
    .post("/dishes", dishToAdd)
    .then(() => {
      dispatch(fetchDishes());
    })
    .catch(function (error) {
      console.log("Failed adding dish: " + error);
      dispatch(dishAddedFailed(error.message));
    });
};

export const editDish = (editedDish) => (dispatch) => {
  const sishToEdit = {
    name: editedDish.name,
    description: editedDish.description,
    isVegetarian: editedDish.isVegetarian,
    isHalal: editedDish.isHalal,
    hasNuts: editedDish.hasNuts,
    hasSeafood: editedDish.hasSeafood,
    hasPork: editedDish.hasPork,
    hasBeef: editedDish.hasBeef,
    isGlutenFree: editedDish.isGlutenFree,
    isLactoseFree: editedDish.isLactoseFree,
  };

  const userUuid = localStorage.getItem(STORAGE_USER_UUID);
  axiosInstance
    .put("/dishes/" + editedDish.uuid, sishToEdit)
    .then(() => {
      dispatch(fetchDishes());
      dispatch(fetchMenus());
      dispatch(fetchAttendanceUser(userUuid));
    })
    .catch(function (error) {
      console.log("Failed editing Dish: " + error);
      dispatch(dishEditedFailed(error.message));
    });
};

export const deleteDish = (dishUuid) => (dispatch) => {
  const userUuid = localStorage.getItem(STORAGE_USER_UUID);
  axiosInstance
    .delete("/dishes/" + dishUuid)
    .then(() => {
      dispatch(fetchDishes());
      dispatch(fetchMenus());
      dispatch(fetchAttendanceUser(userUuid));
    })
    .catch(function (error) {
      console.log("Failed removing dish: " + error);
      dispatch(dishDeletedFailed(error.message));
    });
};
