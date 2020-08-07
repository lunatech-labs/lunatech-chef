import * as ActionTypes from "./MenusActionTypes";

const initState = {
  isLoading: false,
  menus: [],
  errorListing: null,
  errorAdding: null,
  errorDeleting: null,
};

export const MenusReducer = (state = initState, action) => {
  switch (action.type) {
    case ActionTypes.SHOW_ALL_MENUS:
      return { ...initState, menus: action.payload };

    case ActionTypes.MENUS_LOADING:
      return { ...initState, isLoading: true };

    case ActionTypes.MENUS_LOADING_FAILED:
      return { ...initState, errorListing: action.payload };

    case ActionTypes.ADD_NEW_MENU_FAILED:
      return { ...state, errorAdding: action.payload };

    case ActionTypes.DELETE_MENU_FAILED:
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
