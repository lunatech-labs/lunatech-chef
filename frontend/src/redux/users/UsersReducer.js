import * as ActionTypes from "./UsersActionTypes";

const initState = {
  isLoading: false,
  isAuthenticated: false,
  isAdmin: false,
  uuid: "",
  name: "",
  emailAddress: "",
  locationUuid: "",
  isVegetarian: false,
  hasHalalRestriction: false,
  hasNutsRestriction: false,
  hasSeafoodRestriction: false,
  hasPorkRestriction: false,
  hasBeefRestriction: false,
  isGlutenIntolerant: false,
  isLactoseIntolerant: false,
  otherRestrictions: false,
  error: null,
  token: "",
};

export const UsersReducer = (state = initState, action) => {
  switch (action.type) {
    case ActionTypes.USER_LOGIN:
      return {
        ...state,
        isAuthenticated: true,
        isAdmin: action.payload.isAdmin,
        uuid: action.payload.uuid,
        name: action.payload.name,
        emailAddress: action.payload.emailAddress,
        locationUuid: action.payload.locationUuid,
        isVegetarian: action.payload.isVegetarian,
        hasHalalRestriction: action.payload.hasHalalRestriction,
        hasNutsRestriction: action.payload.hasNutsRestriction,
        hasSeafoodRestriction: action.payload.hasSeafoodRestriction,
        hasPorkRestriction: action.payload.hasPorkRestriction,
        hasBeefRestriction: action.payload.hasBeefRestriction,
        isGlutenIntolerant: action.payload.isGlutenIntolerant,
        isLactoseIntolerant: action.payload.isLactoseIntolerant,
        otherRestrictions: action.payload.otherRestrictions,
        error: null,
      };

    case ActionTypes.UPDATE_USER_PROFILE:
      return {
        ...state,
        locationUuid: action.payload.locationUuid,
        isVegetarian: action.payload.isVegetarian,
        hasHalalRestriction: action.payload.hasHalalRestriction,
        hasNutsRestriction: action.payload.hasNutsRestriction,
        hasSeafoodRestriction: action.payload.hasSeafoodRestriction,
        hasPorkRestriction: action.payload.hasPorkRestriction,
        hasBeefRestriction: action.payload.hasBeefRestriction,
        isGlutenIntolerant: action.payload.isGlutenIntolerant,
        isLactoseIntolerant: action.payload.isLactoseIntolerant,
        otherRestrictions: action.payload.otherRestrictions,
        error: null,
      };

    case ActionTypes.USER_LOGIN_ERROR:
      return {
        ...state,
        isLoading: false,
        isAuthenticated: false,
        error: action.payload,
      };

    case ActionTypes.TOKEN_GENERATED:
        return { ...state, isLoading: false, error: null, token: action.payload.token };

    case ActionTypes.TOKEN_GENERATING:
        return { ...state, isLoading: true, error: null, token: "" };

    case ActionTypes.TOKEN_GENERATION_ERROR:
        return { ...state, isLoading: false, error: action.payload, token: "" };

    case ActionTypes.TOKEN_CLEAR:
        return { ...state, isLoading: false, error: null, token: "" };

    case ActionTypes.USER_LOGOUT:
      return initState;

    case ActionTypes.USER_PROFILE_SAVE_ERROR:
      return { ...state, isLoading: false, error: action.payload };

    default:
      return state;
  }
};
