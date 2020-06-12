import * as ActionTypes from "./MenusActionTypes";

const initState = {
  isLoading: true,
  error: null,
  menus: [],
};

export const MenusReducer = (state = initState, action) => {
  switch (action.type) {
    case ActionTypes.SHOW_ALL_MENUS:
      return { ...state, isLoading: false, error: null, menus: action.payload };

    case ActionTypes.MENUS_LOADING:
      return { ...state, isLoading: true, error: null };

    case ActionTypes.MENUS_LOADING_FAILED:
      return { ...state, isLoading: false, error: action.payload };

    case ActionTypes.ADD_NEW_MENU:
      return { ...state, isLoading: false, error: null };

    case ActionTypes.REMOVE_MENU:
      return { ...state, isLoading: false, error: null };

    default:
      return state;
  }
};
