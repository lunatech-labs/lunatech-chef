import * as ActionTypes from "./SchedulesActionTypes";

const initState = {
  isLoading: true,
  error: null,
  schedules: [],
};

export const SchedulesReducer = (state = initState, action) => {
  switch (action.type) {
    case ActionTypes.SHOW_ALL_SCHEDULES:
      return {
        ...state,
        isLoading: false,
        error: null,
        schedules: action.payload,
      };

    case ActionTypes.SCHEDULES_LOADING:
      return { ...state, isLoading: true, error: null };

    case ActionTypes.SCHEDULES_LOADING_FAILED:
      return { ...state, isLoading: false, error: action.payload };

    case ActionTypes.ADD_NEW_SCHEDULE:
      return { ...state, isLoading: false, error: null };

    case ActionTypes.REMOVE_SCHEDULE:
      return { ...state, isLoading: false, error: null };

    default:
      return state;
  }
};
