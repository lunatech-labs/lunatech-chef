import * as ActionTypes from "./UsersActionTypes";
import { axiosInstance } from "../Axios";
import { fetchDishes } from "../dishes/DishesActionCreators";
import { fetchLocations } from "../locations/LocationsActionCreators";
import { fetchMenus } from "../menus/MenusActionCreators";

export const login = (token) => (dispatch) => {
  axiosInstance
    .get("/login/" + token)
    .then((response) => {
      configureAxios(response);
      dispatch(userLoggedIn(response.data, response.headers));
      getInitalData(dispatch);
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

const getInitalData = (dispatch) => {
  dispatch(fetchLocations());
  dispatch(fetchDishes());
  dispatch(fetchMenus());
};

export const logout = () => (dispatch) => {
  console.log("Logging out");
  dispatch(userLoggedOut());
};

export const userLoggedIn = (data, headers) => ({
  type: ActionTypes.USER_LOGIN,
  payload: data,
});

export const userLoginError = (errmess) => ({
  type: ActionTypes.USER_LOGIN_ERROR,
  payload: errmess,
});

export const userLoggedOut = () => ({
  type: ActionTypes.USER_LOGOUT,
});
