import { allSchedulesLoading, allSchedulesLoadingFailed, allSchedulesShown, scheduleAddedFailed, scheduleEditedFailed, scheduleDeletedFailed, allRecurrentSchedulesShown, allSchedulesAttendanceLoading, allSchedulesAttendanceLoadingFailed, allSchedulesAttendanceShown } from "./SchedulesSlice";
import { axiosInstance } from "../Axios";
import { fetchAttendanceUser } from "../attendance/AttendanceActionCreators";
import {
    STORAGE_USER_UUID,
    STORAGE_FILTER_DATE_SCHEDULE,
    STORAGE_FILTER_OFFICE_SCHEDULE,
    STORAGE_FILTER_DATE_WHO_IS_JOINING,
    STORAGE_FILTER_OFFICE_WHO_IS_JOINING,
} from "../LocalStorageKeys";

export const fetchSchedules = () => (dispatch) => {
    dispatch(allSchedulesLoading(true));

    const savedDate = localStorage.getItem(STORAGE_FILTER_DATE_SCHEDULE);
    const date =
        savedDate === null ? new Date().toISOString().substring(0, 10) : savedDate;

    const office = localStorage.getItem(STORAGE_FILTER_OFFICE_SCHEDULE);

    var filter =
        office === null || office === ""
            ? "?fromdate=" + date
            : "?fromdate=" + date + "&office=" + office;

    axiosInstance
        .get("/schedulesWithMenusInfo" + filter)
        .then(function (response) {
            dispatch(allSchedulesShown(response.data));
        })
        .catch(function (error) {
            console.log("Failed loading Schedules: " + error);
            dispatch(allSchedulesLoadingFailed(error.message));
        });
};

export const fetchRecurrentSchedules = () => (dispatch) => {
    dispatch(allSchedulesLoading(true));

    const office = localStorage.getItem(STORAGE_FILTER_OFFICE_SCHEDULE);

    var filter =
        office === null || office === "" ? "" : "?office=" + office;

    axiosInstance
        .get("/recurrentSchedulesWithMenusInfo" + filter)
        .then(function (response) {
            dispatch(allRecurrentSchedulesShown(response.data));
        })
        .catch(function (error) {
            console.log("Failed loading Recurrent Schedules: " + error);
            dispatch(allSchedulesLoadingFailed(error.message));
        });
};

export const fetchSchedulesAttendance = () => (dispatch) => {
    dispatch(allSchedulesAttendanceLoading(true));

    const savedDate = localStorage.getItem(STORAGE_FILTER_DATE_WHO_IS_JOINING);
    const date =
        savedDate === null ? new Date().toISOString().substring(0, 10) : savedDate;

    const office = localStorage.getItem(STORAGE_FILTER_OFFICE_WHO_IS_JOINING);

    var filter =
        office === null || office === ""
            ? "?fromdate=" + date
            : "?fromdate=" + date + "&office=" + office;

    axiosInstance
        .get("/schedulesWithAttendanceInfo" + filter)
        .then(function (response) {
            dispatch(allSchedulesAttendanceShown(response.data));
        })
        .catch(function (error) {
            console.log("Failed loading Schedules: " + error);
            dispatch(allSchedulesAttendanceLoadingFailed(error.message));
        });
};

export const addNewSchedule = (newSchedule) => (dispatch) => {
    const scheduleToAdd = {
        menuUuid: newSchedule.menuUuid,
        officeUuid: newSchedule.officeUuid,
        date: newSchedule.date,
    };

    const userUuid = localStorage.getItem(STORAGE_USER_UUID);
    axiosInstance
        .post("/schedules", scheduleToAdd)
        .then(() => {
            if (newSchedule.recurrency > 0) {
                dispatch(addNewRecurrentSchedule(newSchedule));
            }
        })
        .then(() => {
            dispatch(fetchSchedules());
            dispatch(fetchSchedulesAttendance());
            dispatch(fetchAttendanceUser(userUuid));
        })
        .catch(function (error) {
            console.log("Failed adding Schedule: " + error);
            dispatch(scheduleAddedFailed(error.message));
        });
};

export const addNewRecurrentSchedule = (newRecurrentSchedule) => (dispatch) => {
    const scheduleToAdd = {
        menuUuid: newRecurrentSchedule.menuUuid,
        officeUuid: newRecurrentSchedule.officeUuid,
        repetitionDays: newRecurrentSchedule.recurrency,
        nextDate: newRecurrentSchedule.nextDate,
    };

    axiosInstance
        .post("/recurrentschedules", scheduleToAdd)
        .then(() => {
            dispatch(fetchRecurrentSchedules());
        })
        .catch(function (error) {
            console.log("Failed adding Recurrent Schedule: " + error);
            dispatch(scheduleAddedFailed(error.message));
        });
};

export const editSchedule = (editedSchedule) => (dispatch) => {
    if (editedSchedule.recurrency > 0) {
        dispatch(editRecurrentSchedule(editedSchedule));
    } else {
        dispatch(editSingleSchedule(editedSchedule));
    }
};

export const editSingleSchedule = (editedSchedule) => (dispatch) => {
    const sheduleToEdit = {
        menuUuid: editedSchedule.menuUuid,
        officeUuid: editedSchedule.officeUuid,
        date: editedSchedule.date,
    };

    const userUuid = localStorage.getItem(STORAGE_USER_UUID);
    axiosInstance
        .put("/schedules/" + editedSchedule.uuid, sheduleToEdit)
        .then(() => {
            dispatch(fetchSchedules());
            dispatch(fetchSchedulesAttendance());
            dispatch(fetchAttendanceUser(userUuid));
        })
        .catch(function (error) {
            console.log("Failed editing Schedule: " + error);
            dispatch(scheduleEditedFailed(error.message));
        });
};

export const editRecurrentSchedule = (editedSchedule) => (dispatch) => {
    const recScheduleToEdit = {
        menuUuid: editedSchedule.menuUuid,
        officeUuid: editedSchedule.officeUuid,
        repetitionDays: editedSchedule.recurrency,
        nextDate: editedSchedule.date,
    };

    const userUuid = localStorage.getItem(STORAGE_USER_UUID);
    axiosInstance
        .put("/recurrentschedules/" + editedSchedule.uuid, recScheduleToEdit)
        .then(() => {
            dispatch(fetchRecurrentSchedules());
            dispatch(fetchSchedulesAttendance());
            dispatch(fetchAttendanceUser(userUuid));
        })
        .catch(function (error) {
            console.log("Failed editing Recurrent Schedule: " + error);
            dispatch(scheduleEditedFailed(error.message));
        });
};

export const deleteSchedule = (scheduleUuid) => (dispatch) => {
    const userUuid = localStorage.getItem(STORAGE_USER_UUID);
    axiosInstance
        .delete("/schedules/" + scheduleUuid)
        .then(() => {
            dispatch(fetchSchedules());
            dispatch(fetchSchedulesAttendance());
            dispatch(fetchAttendanceUser(userUuid));
        })
        .catch(function (error) {
            console.log("Failed removing Schedule: " + error);
            dispatch(scheduleDeletedFailed(error.message));
        });
};

export const deleteRecurrentSchedule = (recScheduleUuid) => (dispatch) => {
    axiosInstance
        .delete("/recurrentschedules/" + recScheduleUuid)
        .then(() => {
            dispatch(fetchRecurrentSchedules());
        })
        .catch(function (error) {
            console.log("Failed removing Schedule: " + error);
            dispatch(scheduleDeletedFailed(error.message));
        });
};
