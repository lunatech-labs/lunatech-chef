import * as ActionTypes from "./UsersActionTypes";
import { axiosInstance } from "../Axios";
import { fetchDishes } from "../dishes/DishesActionCreators";
import {
    fetchLocations,
    locationsLoading,
    locationsLoadingFailed,
    showAllLocations
} from "../locations/LocationsActionCreators";
import { fetchMenus } from "../menus/MenusActionCreators";
import {
  fetchSchedules,
  fetchRecurrentSchedules,
  fetchSchedulesAttendance,
} from "../schedules/SchedulesActionCreators";
import { fetchAttendanceUser } from "../attendance/AttendanceActionCreators";
export const login = (token) => (dispatch) => {
  axiosInstance
    .get("/login/" + token)
    .then((response) => {
      const chefSession = response.headers?.chef_session;
      configureAxios(chefSession);
      dispatch(userLoggedIn(response.data));
      const userUuid = response.data.uuid;
      localStorage.setItem("userUuid", userUuid);
      localStorage.setItem('chef_session', chefSession);
      localStorage.setItem("userInfo", JSON.stringify(response.data));
      getInitialData(dispatch);
    })
    .catch(function (error) {
      console.log("Failed logging in user " + error);
      dispatch(userLoginError(error));
    });
};

export const generateToken = () => (dispatch) => {
    dispatch(tokenGeneratingLoading());

    axiosInstance
        .get("/users/token-generation")
        .then(function (response) {
            dispatch(tokenGenerated(response.data));
        })
        .catch(function (error) {
            console.log("Failed loading Locations: " + error);
            dispatch(tokenGenerationError(error.message));
        });
};

export const clearToken = () => (dispatch) => {
    dispatch(tokenCleared());
}

export const restoreSessionFromLocalStorage = () => (dispatch) => {
    const userUuid = localStorage.getItem("userUuid");
    const chefSession = localStorage.getItem("chef_session");
    const userInfo = JSON.parse(localStorage.getItem("userInfo"));
    if (userUuid && userInfo && chefSession) {
        configureAxios(chefSession);
        dispatch(userLoggedIn(userInfo));
        getInitialData(dispatch);
    }
}

export const removeExpiredSession = () => (dispatch) => {
    const chefSession = localStorage.getItem("chef_session");
    const userInfo = JSON.parse(localStorage.getItem("userInfo"));
    if (chefSession && userInfo) {
        const ttl = parseInt(userInfo.ttl);
        const ttlLimit = process.env.REACT_APP_TTL_LIMIT;
        const now = new Date().getTime();
        const duration = new Date(now - ttl).getMinutes();
        if (duration < 0 || duration > ttlLimit) {
            localStorage.removeItem("userUuid");
            localStorage.removeItem("chef_session");
            localStorage.removeItem("userInfo");
            dispatch(userLoggedOut());
        }

    }
}

const configureAxios = (chefSession) => {
  axiosInstance.interceptors.request.use(
    function (config) {
      config.headers = {
        ...config.headers,
        CHEF_SESSION: chefSession //response.headers.chef_session,
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
    hasHalalRestriction: userProfile.hasHalalRestriction,
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
      dispatch(userDataUpdayed(userProfileToSave));
    })
    .catch(function (error) {
      console.log("Failed saving user profile: " + error);
      dispatch(userProfileSaveError(error.message));
    });
};

const getInitialData = (dispatch) => {
  dispatch(fetchLocations());
  dispatch(fetchDishes());
  dispatch(fetchMenus());
  dispatch(fetchSchedules());
  dispatch(fetchRecurrentSchedules());
  dispatch(fetchAttendanceUser());
  dispatch(fetchSchedulesAttendance());
};

export const logout = () => (dispatch) => {
  dispatch(userLoggedOut());
  localStorage.clear();
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

export const tokenGeneratingLoading = () => ({
    type: ActionTypes.TOKEN_GENERATING,
});

export const tokenGenerationError = (errmess) => ({
    type: ActionTypes.TOKEN_GENERATION_ERROR,
    payload: errmess,
})

export const tokenGenerated = (data) => ({
    type: ActionTypes.TOKEN_GENERATED,
    payload: data,
})
export const tokenCleared = () => ({
    type: ActionTypes.TOKEN_CLEAR,
})
