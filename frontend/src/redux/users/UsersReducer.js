import * as ActionTypes from "./UsersActionTypes";

const initState = {
  isAuthenticated: false,
  name: null,
  email: null,
  isAdmin: false,
  error: null,
};

export const UsersReducer = (state = initState, action) => {
  switch (action.type) {
    case ActionTypes.USER_LOGIN:
      return {
        ...state,
        isAuthenticated: true,
        name: action.payload.name,
        email: action.payload.email,
        isAdmin: action.payload.isAdmin,
        error: null,
      };

    case ActionTypes.USER_LOGIN_ERROR:
      return { ...state, isAuthenticated: false, error: action.payload };

    case ActionTypes.USER_LOGOUT:
      return initState;

    default:
      return state;
  }
};
