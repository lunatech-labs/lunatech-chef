import * as ActionTypes from "./SchedulesActionTypes";

const initState = {
  isLoading: false,
  schedules: [],
  errorListing: null,
  errorAdding: null,
  errorEditing: null,
  errorDeleting: null,
};

export const SchedulesReducer = (state = initState, action) => {
  switch (action.type) {
    case ActionTypes.SHOW_ALL_SCHEDULES:
      return { ...initState, schedules: action.payload };

    case ActionTypes.SCHEDULES_LOADING:
      return { ...initState, isLoading: true };

    case ActionTypes.SCHEDULES_LOADING_FAILED:
      return {
        ...initState,
        errorListing: action.payload,
        errorAdding: null,
        errorEditing: null,
        errorDeleting: null,
      };

    case ActionTypes.ADD_NEW_SCHEDULE_FAILED:
      return {
        ...state,
        errorListing: null,
        errorAdding: action.payload,
        errorEditing: null,
        errorDeleting: null,
      };

    case ActionTypes.EDIT_SCHEDULE_FAILED:
      return {
        ...state,
        errorListing: null,
        errorAdding: null,
        errorEditing: action.payload,
        errorDeleting: null,
      };

    case ActionTypes.DELETE_SCHEDULE_FAILED:
      return {
        ...state,
        errorListing: null,
        errorAdding: null,
        errorAddingAttedance: null,
        errorEditing: null,
        errorDeleting: action.payload,
      };

    default:
      return state;
  }
};
