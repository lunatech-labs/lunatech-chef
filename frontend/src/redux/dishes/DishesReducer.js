import * as ActionTypes from "./DishesActionTypes";

const initState = {
  isLoading: false,
  dishes: [],
  errorListing: null,
  errorAdding: null,
  errorDeleting: null,
};

export const DishesReducer = (state = initState, action) => {
  switch (action.type) {
    case ActionTypes.SHOW_ALL_DISHES:
      return { ...initState, dishes: action.payload };

    case ActionTypes.DISHES_LOADING:
      return { ...initState, isLoading: true };

    case ActionTypes.DISHES_LOADING_FAILED:
      return { ...initState, errorListing: action.payload };

    case ActionTypes.ADD_NEW_DISH:
      return { ...initState };

    case ActionTypes.ADD_NEW_DISH_FAILED:
      return { ...state, errorAdding: action.payload };

    case ActionTypes.DELETE_DISH:
      return { ...initState };

    case ActionTypes.DELETE_DISH_FAILED:
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
