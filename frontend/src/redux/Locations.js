import * as ActionTypes from "./ActionTypes";

export const Locations = (
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
      return { ...state, isLoading: true, error: null, locations: [] };

    case ActionTypes.LOCATIONS_LOADING_FAILED:
      return { ...state, isLoading: false, error: action.payload };

    default:
      return state;
  }
};
