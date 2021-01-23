import * as ActionTypes from "./AttendanceActionTypes";

const initState = {
  isLoading: false,
  attendance: [],
  errorListing: null,
  errorEditing: null,
  errorDeleting: null,
};

export const AttendanceReducer = (state = initState, action) => {
  switch (action.type) {
    case ActionTypes.SHOW_ALL_ATTENDANCE:
      return { ...initState, attendance: action.payload };

    case ActionTypes.SHOW_NEW_ATTENDANCE:
      return { ...initState, attendance: action.payload };

    case ActionTypes.ATTENDANCE_LOADING:
      return { ...initState, isLoading: true };

    case ActionTypes.ATTENDANCE_LOADING_FAILED:
      return {
        ...initState,
        errorListing: action.payload,
        errorEditing: null,
        errorDeleting: null,
      };
    case ActionTypes.EDIT_ATTENDANCE_FAILED:
      return {
        ...state,
        errorListing: null,
        errorEditing: action.payload,
        errorDeleting: null,
      };

    default:
      return state;
  }
};
