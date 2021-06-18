import * as ActionTypes from "./UsersActionTypes";
import { axiosInstance } from "../Axios";
import { fetchDishes } from "../dishes/DishesActionCreators";
import { fetchLocations } from "../locations/LocationsActionCreators";
import { fetchMenus } from "../menus/MenusActionCreators";
import {
  fetchSchedules,
  fetchSchedulesAttendance,
} from "../schedules/SchedulesActionCreators";
import { fetchAttendanceUser } from "../attendance/AttendanceActionCreators";

export const login = (token) => (dispatch) => {
  axiosInstance
    .get("/login/" + token)
    .then((response) => {
      configureAxios(response);
      dispatch(userLoggedIn(response.data));

      const userUuid = response.data.uuid;
      getInitalData(dispatch, userUuid);
    })
    .catch(function (error) {
      console.log("Failed logging in user " + error);
      dispatch(userLoginError(error));
    });
};

const configureAxios = (response) => {
  axiosInstance.interceptors.request.use(
    function (config) {
      config.headers = {
        ...config.headers,
        CHEF_SESSION: response.headers.chef_session,
      };
      return config;
    },
    function (error) {
      return Promise.reject(error);
    }
  );
};

export const saveUserProfile = (userUuid, userProfile) => (dispatch) => {
  const userProfileToSave = {
    locationUuid: userProfile.locationUuid,
    isVegetarian: userProfile.isVegetarian,
    hasNutsRestriction: userProfile.hasNutsRestriction,
    hasSeafoodRestriction: userProfile.hasSeafoodRestriction,
    hasPorkRestriction: userProfile.hasPorkRestriction,
    hasBeefRestriction: userProfile.hasBeefRestriction,
    isGlutenIntolerant: userProfile.isGlutenIntolerant,
    isLactoseIntolerant: userProfile.isLactoseIntolerant,
    otherRestrictions: userProfile.otherRestrictions,
  };

  axiosInstance
    .put("/users/" + userUuid, userProfileToSave)
    .then((response) => {
      console.log("User profile saved with response " + response);
      dispatch(userDataUpdayed(userProfileToSave));
    })
    .catch(function (error) {
      console.log("Failed saving user profile: " + error);
      dispatch(userProfileSaveError(error.message));
    });
};

const getInitalData = (dispatch, userUuid) => {
  dispatch(fetchLocations());
  dispatch(fetchDishes());
  dispatch(fetchMenus());
  dispatch(fetchSchedules());
  dispatch(fetchAttendanceUser(userUuid));
  dispatch(fetchSchedulesAttendance());
};

export const logout = () => (dispatch) => {
  dispatch(userLoggedOut());
};

export const userLoggedIn = (data) => ({
  type: ActionTypes.USER_LOGIN,
  payload: data,
});

export const userDataUpdayed = (data) => ({
  type: ActionTypes.UPDATE_USER_PROFILE,
  payload: data,
});

export const userLoginError = (errmess) => ({
  type: ActionTypes.USER_LOGIN_ERROR,
  payload: errmess,
});

export const userLoggedOut = () => ({
  type: ActionTypes.USER_LOGOUT,
});

export const userProfileSaveError = (errmess) => ({
  type: ActionTypes.USER_PROFILE_SAVE_ERROR,
  payload: errmess,
});
