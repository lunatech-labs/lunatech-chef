import * as ActionTypes from "./MenusActionTypes";
import { axiosInstance } from "../Axios";
import {
  fetchSchedules,
  fetchSchedulesAttendance,
} from "../schedules/SchedulesActionCreators";
import { fetchAttendanceUser } from "../attendance/AttendanceActionCreators";

export const fetchMenus = () => (dispatch) => {
  dispatch(menusLoading(true));

  axiosInstance
    .get("/menusWithDishesInfo")
    .then(function (response) {
      dispatch(showAllMenus(response));
    })
    .catch(function (error) {
      console.log("Failed loading Menus: " + error);
      dispatch(menusLoadingFailed(error.message));
    });
};

export const addNewMenu = (newMenu) => (dispatch) => {
  const menuToAdd = {
    name: newMenu.name,
    dishesUuids: newMenu.dishesUuids,
  };

  axiosInstance
    .post("/menus", menuToAdd)
    .then((response) => {
      dispatch(fetchMenus());
    })
    .catch(function (error) {
      console.log("Failed adding Menu: " + error);
      dispatch(menuAddingFailed(error.message));
    });
};

export const editMenu = (editedMenu) => (dispatch) => {
  const menuToEdit = {
    name: editedMenu.name,
    dishesUuids: editedMenu.dishesUuids,
  };

  const userUuid = localStorage.getItem("userUuid");
  axiosInstance
    .put("/menus/" + editedMenu.uuid, menuToEdit)
    .then((response) => {
      dispatch(fetchMenus());
      dispatch(fetchSchedules());
      dispatch(fetchSchedulesAttendance());
      dispatch(fetchAttendanceUser(userUuid));
    })
    .catch(function (error) {
      console.log("Failed editing Menu: " + error);
      dispatch(menuEditingFailed(error.message));
    });
};

export const deleteMenu = (menuUuid) => (dispatch) => {
  axiosInstance
    .delete("/menus/" + menuUuid)
    .then((response) => {
      dispatch(fetchMenus());
    })
    .catch(function (error) {
      console.log("Failed removing Menu: " + error);
      dispatch(menuDeletingFailed(error.message));
    });
};

export const menusLoading = () => ({
  type: ActionTypes.MENUS_LOADING,
});

export const showAllMenus = (menus) => ({
  type: ActionTypes.SHOW_ALL_MENUS,
  payload: menus.data,
});

export const menusLoadingFailed = (errmess) => ({
  type: ActionTypes.MENUS_LOADING_FAILED,
  payload: errmess,
});

export const menuAddingFailed = (errmess) => ({
  type: ActionTypes.ADD_NEW_MENU_FAILED,
  payload: errmess,
});

export const menuEditingFailed = (errmess) => ({
  type: ActionTypes.EDIT_MENU_FAILED,
  payload: errmess,
});

export const menuDeletingFailed = (errmess) => ({
  type: ActionTypes.DELETE_MENU_FAILED,
  payload: errmess,
});
