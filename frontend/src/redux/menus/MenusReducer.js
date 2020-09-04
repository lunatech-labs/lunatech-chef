import * as ActionTypes from "./MenusActionTypes";

const initState = {
  isLoading: false,
  menus: [],
  errorListing: null,
  errorAdding: null,
  errorEditing: null,
  errorDeleting: null,
};

export const MenusReducer = (state = initState, action) => {
  switch (action.type) {
    case ActionTypes.SHOW_ALL_MENUS:
      return { ...initState, menus: action.payload };

    case ActionTypes.MENUS_LOADING:
      return { ...initState, isLoading: true };

    case ActionTypes.MENUS_LOADING_FAILED:
      return {
        ...initState,
        errorListing: action.payload,
        errorAdding: null,
        errorEditing: null,
        errorDeleting: null,
      };

    case ActionTypes.ADD_NEW_MENU_FAILED:
      return {
        ...state,
        errorListing: null,
        errorAdding: action.payload,
        errorEditing: null,
        errorDeleting: null,
      };

    case ActionTypes.EDIT_MENU_FAILED:
      return {
        ...state,
        errorListing: null,
        errorAdding: null,
        errorEditing: action.payload,
        errorDeleting: null,
      };

    case ActionTypes.DELETE_MENU_FAILED:
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
