import { allMenusLoading, allMenusShown, allMenusLoadingFailed, menuAddedFailed, menuEditedFailed, menuDeletedFailed } from "./MenusSlice";
import { axiosInstance } from "../Axios";
import {
  fetchSchedules,
  fetchSchedulesAttendance,
} from "../schedules/SchedulesActionCreators";
import { fetchAttendanceUser } from "../attendance/AttendanceActionCreators";

export const fetchMenus = () => (dispatch) => {
  dispatch(allMenusLoading(true));

  axiosInstance
    .get("/menusWithDishesInfo")
    .then(function (response) {
      dispatch(allMenusShown(response));
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
    .then((response) => {
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
      dispatch(menuEditedFailed(error.message));
    });
};

export const deleteMenu = (menuUuid) => (dispatch) => {
  const userUuid = localStorage.getItem("userUuid");
  axiosInstance
    .delete("/menus/" + menuUuid)
    .then((response) => {
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
