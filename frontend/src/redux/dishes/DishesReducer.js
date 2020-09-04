import * as ActionTypes from "./DishesActionTypes";

const initState = {
  isLoading: false,
  dishes: [],
  errorListing: null,
  errorAdding: null,
  errorEditing: null,
  errorDeleting: null,
};

export const DishesReducer = (state = initState, action) => {
  switch (action.type) {
    case ActionTypes.SHOW_ALL_DISHES:
      return { ...initState, dishes: action.payload };

    case ActionTypes.DISHES_LOADING:
      return { ...initState, isLoading: true };

    case ActionTypes.DISHES_LOADING_FAILED:
      return {
        ...initState,
        errorListing: action.payload,
        errorAdding: null,
        errorEditing: null,
        errorDeleting: null,
      };

    case ActionTypes.ADD_NEW_DISH_FAILED:
      return {
        ...state,
        errorListing: null,
        errorAdding: action.payload,
        errorEditing: null,
        errorDeleting: null,
      };

    case ActionTypes.EDIT_DISH_FAILED:
      return {
        ...state,
        errorListing: null,
        errorAdding: null,
        errorEditing: action.payload,
        errorDeleting: null,
      };

    case ActionTypes.DELETE_DISH_FAILED:
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
