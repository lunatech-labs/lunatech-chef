import * as ActionTypes from "./UsersActionTypes";

const axios = require("axios").default;

export const login = (token) => (dispatch) => {
  axios
    .get(process.env.REACT_APP_BASE_URL + "/login/" + token)
    .then((response) => {
      console.log(JSON.stringify(response));
      dispatch(userLoggedIn(response.data, response.headers));
    })
    .catch(function (error) {
      console.log("Failed logging in user " + error);
      dispatch(userLoginError(error));
    });
};

export const logout = () => (dispatch) => {
  console.log("Logging out user");
  dispatch(userLoggedOut());
};

export const userLoggedIn = (data, headers) => ({
  type: ActionTypes.USER_LOGIN,
  payload: data,
  sessionCookie: headers.chef_session,
});

export const userLoginError = (errmess) => ({
  type: ActionTypes.USER_LOGIN_ERROR,
  payload: errmess,
});

export const userLoggedOut = () => ({
  type: ActionTypes.USER_LOGOUT,
});
