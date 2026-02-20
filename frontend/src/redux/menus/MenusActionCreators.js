import { allMenusLoading, allMenusShown, allMenusLoadingFailed, menuAddedFailed, menuEditedFailed, menuDeletedFailed } from "./MenusSlice";
import { axiosInstance } from "../Axios";
import {
  fetchSchedules,
  fetchSchedulesAttendance,
} from "../schedules/SchedulesActionCreators";
import { fetchAttendanceUser } from "../attendance/AttendanceActionCreators";
import { STORAGE_USER_UUID } from "../LocalStorageKeys";

export const fetchMenus = () => (dispatch) => {
  dispatch(allMenusLoading(true));

  axiosInstance
    .get("/menusWithDishesInfo")
    .then(function (response) {
      dispatch(allMenusShown(response.data));
    })
    .catch(function (error) {
      console.log("Failed loading Menus: " + error);
      dispatch(allMenusLoadingFailed(error.message));
    });
};

export const addNewMenu = (newMenu) => (dispatch) => {
  const menuToAdd = {
    name: newMenu.name,
    dishesUuids: newMenu.dishesUuids,
  };

  axiosInstance
    .post("/menus", menuToAdd)
    .then(() => {
      dispatch(fetchMenus());
    })
    .catch(function (error) {
      console.log("Failed adding Menu: " + error);
      dispatch(menuAddedFailed(error.message));
    });
};

export const editMenu = (editedMenu) => (dispatch) => {
  const menuToEdit = {
    name: editedMenu.name,
    dishesUuids: editedMenu.dishesUuids,
  };

  const userUuid = localStorage.getItem(STORAGE_USER_UUID);
  axiosInstance
    .put("/menus/" + editedMenu.uuid, menuToEdit)
    .then(() => {
      dispatch(fetchMenus());
      dispatch(fetchSchedules());
      dispatch(fetchSchedulesAttendance());
      dispatch(fetchAttendanceUser(userUuid));
    })
    .catch(function (error) {
      console.log("Failed editing Menu: " + error);
      dispatch(menuEditedFailed(error.message));
    });
};

export const deleteMenu = (menuUuid) => (dispatch) => {
  const userUuid = localStorage.getItem(STORAGE_USER_UUID);
  axiosInstance
    .delete("/menus/" + menuUuid)
    .then(() => {
      dispatch(fetchMenus());
      dispatch(fetchSchedules());
      dispatch(fetchSchedulesAttendance());
      dispatch(fetchAttendanceUser(userUuid));
    })
    .catch(function (error) {
      console.log("Failed removing Menu: " + error);
      dispatch(menuDeletedFailed(error.message));
    });
};
