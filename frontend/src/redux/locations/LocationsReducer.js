import * as ActionTypes from "./LocationsActionTypes";

const initState = {
  isLoading: false,
  locations: [],
  errorListing: null,
  errorAdding: null,
  errorDeleting: null,
};

export const LocationsReducer = (state = initState, action) => {
  switch (action.type) {
    case ActionTypes.SHOW_ALL_LOCATIONS:
      return { ...initState, locations: action.payload };

    case ActionTypes.LOCATIONS_LOADING:
      return { ...initState, isLoading: true };

    case ActionTypes.LOCATIONS_LOADING_FAILED:
      return { ...initState, errorListing: action.payload };

    case ActionTypes.ADD_NEW_LOCATION:
      return { ...initState };

    case ActionTypes.ADD_NEW_LOCATION_FAILED:
      return { ...state, errorAdding: action.payload };

    case ActionTypes.DELETE_LOCATION:
      return { ...initState };

    case ActionTypes.DELETE_LOCATION_FAILED:
      return {
        ...state,
        errorDeleting: action.payload,
        errorListing: null,
        errorAdding: null,
      };

    default:
      return state;
  }
};
