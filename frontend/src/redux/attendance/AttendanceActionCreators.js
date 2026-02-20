import { allAttendancesLoading, allAttendancesLoadingFailed, allAttendancesShown, newAttendanceShown, attendanceEditedFailed } from "./AttendanceSlice";
import { axiosInstance } from "../Axios";
import { fetchSchedulesAttendance } from "../schedules/SchedulesActionCreators";
import { STORAGE_USER_UUID, STORAGE_FILTER_OFFICE_MEALS } from "../LocalStorageKeys";

export const fetchAttendanceUser = (userUuid) => (dispatch) => {
    dispatch(allAttendancesLoading(true));

    const uuid = userUuid || localStorage.getItem(STORAGE_USER_UUID);
    const office = localStorage.getItem(STORAGE_FILTER_OFFICE_MEALS);
    const date = new Date().toISOString().substring(0, 10);

    var filter =
        office === null || office === ""
            ? "?fromdate=" + date
            : "?fromdate=" + date + "&office=" + office;

    axiosInstance
        .get("/attendancesWithScheduleInfo/" + uuid + filter)
        .then(function (response) {
            dispatch(allAttendancesShown(response.data));
        })
        .catch(function (error) {
            console.log("Failed loading Attendance: " + error);
            dispatch(allAttendancesLoadingFailed(error.message));
        });
};

export const editAttendance = (attendance) => (dispatch) => {
    const attendanceToEdit = {
        isAttending: attendance.isAttending,
    };

    axiosInstance
        .put("/attendances/" + attendance.uuid, attendanceToEdit)
        .then(function () {
            dispatch(fetchSchedulesAttendance());
        })
        .catch(function (error) {
            console.log("Failed adding Schedule attendance: " + error);
            dispatch(attendanceEditedFailed(error.message));
        });
};

export const showNewAttendance = (attendance) => (dispatch) => {
    dispatch(newAttendanceShown(attendance));
};
