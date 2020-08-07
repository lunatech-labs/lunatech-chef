import * as ActionTypes from "./LocationsActionTypes";

const initState = {
  isLoading: false,
  locations: [],
  errorListing: null,
  errorAdding: null,
  errorEditing: null,
  errorDeleting: null,
};

export const LocationsReducer = (state = initState, action) => {
  switch (action.type) {
    case ActionTypes.SHOW_ALL_LOCATIONS:
      return { ...initState, locations: action.payload };

    case ActionTypes.LOCATIONS_LOADING:
      return { ...initState, isLoading: true };

    case ActionTypes.LOCATIONS_LOADING_FAILED:
      return {
        ...initState,
        errorListing: action.payload,
        errorAdding: null,
        errorEditing: null,
        errorDeleting: null,
      };

    case ActionTypes.ADD_NEW_LOCATION_FAILED:
      return {
        ...state,
        errorListing: null,
        errorAdding: action.payload,
        errorEditing: null,
        errorDeleting: null,
      };

    case ActionTypes.EDIT_LOCATION_FAILED:
      return {
        ...state,
        errorListing: null,
        errorAdding: null,
        errorEditing: action.payload,
        errorDeleting: null,
      };

    case ActionTypes.DELETE_LOCATION_FAILED:
      return {
        ...state,
        errorListing: null,
        errorAdding: null,
        errorEditing: null,
        errorDeleting: action.payload,
      };

    default:
      return state;
  }
};
