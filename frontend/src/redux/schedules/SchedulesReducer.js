import * as ActionTypes from "./SchedulesActionTypes";

const initState = {
  isLoading: false,
  schedules: [],
  errorListing: null,
  errorAdding: null,
  errorDeleting: null,
};

export const SchedulesReducer = (state = initState, action) => {
  switch (action.type) {
    case ActionTypes.SHOW_ALL_SCHEDULES:
      return { ...initState, schedules: action.payload };

    case ActionTypes.SCHEDULES_LOADING:
      return { ...initState, isLoading: true };

    case ActionTypes.SCHEDULES_LOADING_FAILED:
      return { ...initState, errorListing: action.payload };

    case ActionTypes.ADD_NEW_SCHEDULE_FAILED:
      return { ...state, errorAdding: action.payload };

    case ActionTypes.DELETE_SCHEDULE_FAILED:
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
