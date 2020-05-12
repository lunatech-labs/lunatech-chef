import * as ActionTypes from "./MenusActionTypes";
import { baseUrl } from "../../shared/baseUrl";

const axios = require("axios").default;

export const fetchMenus = () => (dispatch) => {
  dispatch(menusLoading(true));

  axios
    .get(baseUrl + "/menusWithDishesNames")
    .then(function (response) {
      dispatch(showAllMenus(response));
    })
    .catch(function (error) {
      console.log("Failed loading Menus:" + error);
      dispatch(loadingMenusFailed(error.message));
    });
};

export const menusLoading = () => ({
  type: ActionTypes.MENUS_LOADING,
});

export const loadingMenusFailed = (errmess) => ({
  type: ActionTypes.MENUS_LOADING_FAILED,
  payload: errmess,
});

export const showAllMenus = (menus) => ({
  type: ActionTypes.SHOW_ALL_MENUS,
  payload: menus,
});

export const addNewMenu = (newMenu) => (dispatch) => {
  const menuToAdd = {
    name: newMenu.name,
    dishes: newMenu.dishes,
  };

  axios
    .post(baseUrl + "/menus", menuToAdd)
    .then((response) => {
      console.log("New Menu added with response " + response);
      dispatch(fetchMenus());
    })
    .catch(function (error) {
      console.log("Failed adding Menu: " + error);
      dispatch(loadingMenusFailed(error.message));
    });
};

export const deleteMenu = (menuUuid) => (dispatch) => {
  axios
    .delete(baseUrl + "/menus/" + menuUuid)
    .then((response) => {
      console.log("Menu deleted with response" + response);
      dispatch(fetchMenus());
    })
    .catch(function (error) {
      console.log("Failed removing Menu: " + error);
      dispatch(loadingMenusFailed(error.message));
    });
};
