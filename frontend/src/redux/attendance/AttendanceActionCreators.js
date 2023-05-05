import { allAttendancesLoading, allAttendancesLoadingFailed, allAttendancesShown, newAttendanceShown, attendanceEditedFailed } from "./AttendanceSlice";
import { axiosInstance } from "../Axios";
import { fetchSchedulesAttendance } from "../schedules/SchedulesActionCreators";

export const fetchAttendanceUser = () => (dispatch) => {
    dispatch(allAttendancesLoading(true));

    const userUuid = localStorage.getItem("userUuid");
    const office = localStorage.getItem("filterOfficeScheduledMeals");
    const date = new Date().toISOString().substring(0, 10);

    var filter =
        office === null || office === ""
            ? "?fromdate=" + date
            : "?fromdate=" + date + "&office=" + office;

    axiosInstance
        .get("/attendancesWithScheduleInfo/" + userUuid + filter)
        .then(function (response) {
            dispatch(allAttendancesShown(response));
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
        .then(function (response) {
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

