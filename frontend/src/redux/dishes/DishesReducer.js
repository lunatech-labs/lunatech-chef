import * as ActionTypes from "./DishesActionTypes";

export const DishesReducer = (
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

    case ActionTypes.ADD_NEW_DISH:
      return { ...state, isLoading: true, error: null, dishes: [] };

    default:
      return state;
  }
};
