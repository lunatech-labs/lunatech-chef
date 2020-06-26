import * as ActionTypes from "./SchedulesActionTypes";
import { axiosInstance } from "../Axios";

export const fetchSchedules = () => (dispatch) => {
  dispatch(schedulesLoading(true));

  axiosInstance
    .get("/schedulesWithMenusNames")
    .then(function (response) {
      dispatch(showAllSchedules(response));
    })
    .catch(function (error) {
      console.log("Failed loading Schedules: " + error);
      dispatch(loadingSchedulesFailed(error.message));
    });
};

export const schedulesLoading = () => ({
  type: ActionTypes.SCHEDULES_LOADING,
});

export const loadingSchedulesFailed = (errmess) => ({
  type: ActionTypes.SCHEDULES_LOADING_FAILED,
  payload: errmess,
});

export const showAllSchedules = (schedules) => ({
  type: ActionTypes.SHOW_ALL_SCHEDULES,
  payload: schedules.data,
});

export const addNewSchedule = (newSchedule) => (dispatch) => {
  console.log("newSchedule: " + JSON.stringify(newSchedule));
  const scheduleToAdd = {
    menuUuid: newSchedule.menuUuid,
    location: newSchedule.locationUuid,
    date: newSchedule.date,
  };

  axiosInstance
    .post("/schedules", scheduleToAdd)
    .then((response) => {
      console.log("New Schedule added with response " + response);
      dispatch(fetchSchedules());
    })
    .catch(function (error) {
      console.log("Failed adding Schedule: " + error);
      dispatch(loadingSchedulesFailed(error.message));
    });
};

export const deleteSchedule = (scheduleUuid) => (dispatch) => {
  console.log("DELETING SCHEDULE " + scheduleUuid);
  axiosInstance
    .delete("/schedules/" + scheduleUuid)
    .then((response) => {
      console.log("Schedule deleted with response: " + response);
      dispatch(fetchSchedules());
    })
    .catch(function (error) {
      console.log("Failed removing Schedule: " + error);
      dispatch(loadingSchedulesFailed(error.message));
    });
};
