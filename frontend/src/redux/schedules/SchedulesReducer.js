import * as ActionTypes from "./SchedulesActionTypes";

const initState = {
  isLoading: false,
  isLoadingAttendance: false,
  schedules: [],
  recurrentSchedules: [],
  attendance: [],
  errorListing: null,
  errorListingAttendance: null,
  errorAdding: null,
  errorEditing: null,
  errorDeleting: null,
};

export const SchedulesReducer = (state = initState, action) => {
  switch (action.type) {
    case ActionTypes.SHOW_ALL_SCHEDULES:
      return {
        ...state,
        schedules: action.payload,
        isLoading: false,
        errorListing: null,
        errorAdding: null,
        errorEditing: null,
        errorDeleting: null,
      };
    case ActionTypes.SHOW_ALL_RECURRENT_SCHEDULES:
      return {
        ...state,
        recurrentSchedules: action.payload,
        isLoading: false,
        errorListing: null,
        errorAdding: null,
        errorEditing: null,
        errorDeleting: null,
      };
    case ActionTypes.SHOW_ALL_SCHEDULES_ATTENDANCE:
      return {
        ...state,
        attendance: action.payload,
        isLoadingAttendance: false,
        errorListingAttendance: false,
      };

    case ActionTypes.SCHEDULES_LOADING:
      return { ...state, isLoading: true };

    case ActionTypes.SCHEDULES_ATTENDANCE_LOADING:
      return { ...state, isLoadingAttendance: true };

    case ActionTypes.SCHEDULES_LOADING_FAILED:
      return {
        ...initState,
        errorListing: action.payload,
        errorAdding: null,
        errorEditing: null,
        errorDeleting: null,
      };

    case ActionTypes.SCHEDULES_ATTENDANCE_LOADING_FAILED:
      return { ...initState, errorListingAttendance: action.payload };

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
