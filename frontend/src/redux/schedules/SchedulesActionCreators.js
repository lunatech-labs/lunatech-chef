import * as ActionTypes from "./SchedulesActionTypes";
import { axiosInstance } from "../Axios";
import { fetchAttendanceUser } from "../attendance/AttendanceActionCreators";

export const fetchSchedules = () => (dispatch) => {
  dispatch(schedulesLoading(true));

  const savedDate = localStorage.getItem("filterDate");
  const date =
    savedDate === null ? new Date().toISOString().substring(0, 10) : savedDate;

  const location = localStorage.getItem("filterLocation");

  var filter =
    location === null || location === ""
      ? "?fromdate=" + date
      : "?fromdate=" + date + "&location=" + location;

  axiosInstance
    .get("/schedulesWithMenusInfo" + filter)
    .then(function (response) {
      dispatch(showAllSchedules(response));
    })
    .catch(function (error) {
      console.log("Failed loading Schedules: " + error);
      dispatch(schedulesLoadingFailed(error.message));
    });
};

export const fetchSchedulesAttendance = () => (dispatch) => {
  dispatch(schedulesAttendanceLoading(true));

  const savedDate = localStorage.getItem("filterDate");
  const date =
    savedDate === null ? new Date().toISOString().substring(0, 10) : savedDate;

  const location = localStorage.getItem("filterLocation");

  var filter =
    location === null || location === ""
      ? "?fromdate=" + date
      : "?fromdate=" + date + "&location=" + location;

  axiosInstance
    .get("/schedulesWithAttendanceInfo" + filter)
    .then(function (response) {
      dispatch(showAllSchedulesAttendance(response));
    })
    .catch(function (error) {
      console.log("Failed loading Schedules: " + error);
      dispatch(schedulesLoadingAttendanceFailed(error.message));
    });
};

export const addNewSchedule = (newSchedule) => (dispatch) => {
  const scheduleToAdd = {
    menuUuid: newSchedule.menuUuid,
    locationUuid: newSchedule.locationUuid,
    date: newSchedule.date,
  };

  const userUuid = localStorage.getItem("userUuid");
  axiosInstance
    .post("/schedules", scheduleToAdd)
    .then((response) => {
      dispatch(fetchSchedules());
      dispatch(fetchSchedulesAttendance());
      dispatch(fetchAttendanceUser(userUuid));
    })
    .catch(function (error) {
      console.log("Failed adding Schedule: " + error);
      dispatch(scheduleAddingFailed(error.message));
    });
};

export const editSchedule = (editedSchedule) => (dispatch) => {
  const sheduleToEdit = {
    menuUuid: editedSchedule.menuUuid,
    locationUuid: editedSchedule.locationUuid,
    date: editedSchedule.date,
  };

  const userUuid = localStorage.getItem("userUuid");
  axiosInstance
    .put("/schedules/" + editedSchedule.uuid, sheduleToEdit)
    .then((response) => {
      dispatch(fetchSchedules());
      dispatch(fetchSchedulesAttendance());
      dispatch(fetchAttendanceUser(userUuid));
    })
    .catch(function (error) {
      console.log("Failed editing Schedule: " + error);
      dispatch(scheduleEditingFailed(error.message));
    });
};

export const deleteSchedule = (scheduleUuid) => (dispatch) => {
  console.log("lets remove a schedule");

  axiosInstance
    .delete("/schedules/" + scheduleUuid)
    .then((response) => {
      dispatch(fetchSchedules());
    })
    .catch(function (error) {
      console.log("Failed removing Schedule: " + error);
      dispatch(scheduleDeletingFailed(error.message));
    });
};

export const schedulesLoading = () => ({
  type: ActionTypes.SCHEDULES_LOADING,
});

export const schedulesAttendanceLoading = () => ({
  type: ActionTypes.SCHEDULES_ATTENDANCE_LOADING,
});

export const showAllSchedules = (schedules) => ({
  type: ActionTypes.SHOW_ALL_SCHEDULES,
  payload: schedules.data,
});

export const showAllSchedulesAttendance = (schedulesAttendance) => ({
  type: ActionTypes.SHOW_ALL_SCHEDULES_ATTENDANCE,
  payload: schedulesAttendance.data,
});

export const schedulesLoadingFailed = (errmess) => ({
  type: ActionTypes.SCHEDULES_LOADING_FAILED,
  payload: errmess,
});

export const schedulesLoadingAttendanceFailed = (errmess) => ({
  type: ActionTypes.SCHEDULES_ATTENDANCE_LOADING_FAILED,
  payload: errmess,
});

export const scheduleAddingFailed = (errmess) => ({
  type: ActionTypes.ADD_NEW_SCHEDULE_FAILED,
  payload: errmess,
});

export const scheduleEditingFailed = (errmess) => ({
  type: ActionTypes.EDIT_SCHEDULE_FAILED,
  payload: errmess,
});

export const scheduleDeletingFailed = (errmess) => ({
  type: ActionTypes.DELETE_SCHEDULE_FAILED,
  payload: errmess,
});
