import * as ActionTypes from "./AttendanceActionTypes";
import { axiosInstance } from "../Axios";
import { fetchSchedulesAttendance } from "../schedules/SchedulesActionCreators";

export const fetchAttendanceUser = (userUuid) => (dispatch) => {
  dispatch(attendanceLoading(true));

  axiosInstance
    .get("/attendancesWithScheduleInfo/" + userUuid)
    .then(function (response) {
      dispatch(showAllAttendance(response));
    })
    .catch(function (error) {
      console.log("Failed loading Attendance: " + error);
      dispatch(attendanceLoadingFailed(error.message));
    });
};

export const editAttendance = (attendance) => (dispatch) => {
  const attendanceToEdit = {
    isAttending: attendance.isAttending,
  };

  axiosInstance
    .put("/attendances/" + attendance.uuid, attendanceToEdit)
    .then(function (response) {
      dispatch(fetchSchedulesAttendance());
    })
    .catch(function (error) {
      console.log("Failed adding Schedule attendance: " + error);
      dispatch(attendanceEditingFailed(error.message));
    });
};

export const attendanceLoading = () => ({
  type: ActionTypes.ATTENDANCE_LOADING,
});

export const showAllAttendance = (attendance) => ({
  type: ActionTypes.SHOW_ALL_ATTENDANCE,
  payload: attendance.data,
});

export const showNewAttendance = (attendance) => ({
  type: ActionTypes.SHOW_NEW_ATTENDANCE,
  payload: attendance,
});

export const attendanceLoadingFailed = (errmess) => ({
  type: ActionTypes.ATTENDANCE_LOADING_FAILED,
  payload: errmess,
});

export const attendanceEditingFailed = (errmess) => ({
  type: ActionTypes.EDIT_ATTENDANCE_FAILED,
  payload: errmess,
});
