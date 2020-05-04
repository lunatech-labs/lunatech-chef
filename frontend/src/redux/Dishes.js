import * as ActionTypes from "./ActionTypes";

export const Dishes = (
  state = { isLoading: true, error: null, dishes: [] },
  action
) => {
  switch (action.type) {
    case ActionTypes.SHOW_ALL_DISHES:
      return {
        ...state,
        isLoading: false,
        error: null,
        dishes: action.payload,
      };

    case ActionTypes.DISHES_LOADING:
      return { ...state, isLoading: true, error: null, dishes: [] };

    case ActionTypes.DISHES_LOADING_FAILED:
      return { ...state, isLoading: false, error: action.payload };

    default:
      return state;
  }
};
