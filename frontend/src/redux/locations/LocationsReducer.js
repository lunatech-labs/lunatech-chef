import * as ActionTypes from "./LocationActionTypes";

export const LocationsReducer = (
  state = { isLoading: true, error: null, locations: [] },
  action
) => {
  switch (action.type) {
    case ActionTypes.SHOW_ALL_LOCATIONS:
      return {
        ...state,
        isLoading: false,
        error: null,
        locations: action.payload,
      };

    case ActionTypes.LOCATIONS_LOADING:
      return { ...state, isLoading: true, error: null };

    case ActionTypes.LOCATIONS_LOADING_FAILED:
      return { ...state, isLoading: false, error: action.payload };

    case ActionTypes.ADD_NEW_LOCATION:
      return { ...state, isLoading: false, error: null };

    case ActionTypes.REMOVE_LOCATION:
      return { ...state, isLoading: false, error: null };

    default:
      return state;
  }
};
