import * as ActionTypes from "./OfficesActionTypes";
import { axiosInstance } from "../Axios";
import {
    fetchSchedules,
    fetchSchedulesAttendance,
} from "../schedules/SchedulesActionCreators";
import { fetchAttendanceUser } from "../attendance/AttendanceActionCreators";

export const fetchOffices = () => (dispatch) => {
    dispatch(officesLoading(true));

    axiosInstance
        .get("/offices")
        .then(function (response) {
            dispatch(showAllOffices(response));
        })
        .catch(function (error) {
            console.log("Failed loading Offices: " + error);
            dispatch(officesLoadingFailed(error.message));
        });
};

export const addNewOffice = (newOffice) => (dispatch) => {
    const officeToAdd = {
        city: newOffice.city,
        country: newOffice.country,
    };

    axiosInstance
        .post("/offices", officeToAdd)
        .then((response) => {
            dispatch(fetchOffices());
        })
        .catch(function (error) {
            console.log("Failed adding Office: " + error);
            dispatch(officeAddingFailed(error.message));
        });
};

export const editOffice = (editedOffice) => (dispatch) => {
    const officeToEdit = {
        city: editedOffice.city,
        country: editedOffice.country,
    };

    const userUuid = localStorage.getItem("userUuid");
    axiosInstance
        .put("/offices/" + editedOffice.uuid, officeToEdit)
        .then((response) => {
            dispatch(fetchOffices());
            dispatch(fetchSchedules());
            dispatch(fetchSchedulesAttendance());
            dispatch(fetchAttendanceUser(userUuid));
        })
        .catch(function (error) {
            console.log("Failed editing Office: " + error);
            dispatch(officeEditingFailed(error.message));
        });
};

export const deleteOffice = (officeUuid) => (dispatch) => {
    const userUuid = localStorage.getItem("userUuid");
    axiosInstance
        .delete("/offices/" + officeUuid)
        .then((response) => {
            dispatch(fetchOffices());
            dispatch(fetchSchedules());
            dispatch(fetchSchedulesAttendance());
            dispatch(fetchAttendanceUser(userUuid));

        })
        .catch(function (error) {
            console.log("Failed removing Office: " + error);
            dispatch(officeDeletingFailed(error.message));
        });
};

export const officesLoading = () => ({
    type: ActionTypes.OFFICES_LOADING,
});

export const showAllOffices = (offices) => ({
    type: ActionTypes.SHOW_ALL_OFFICES,
    payload: offices.data,
});

export const officesLoadingFailed = (errmess) => ({
    type: ActionTypes.OFFICES_LOADING_FAILED,
    payload: errmess,
});

export const officeAddingFailed = (errmess) => ({
    type: ActionTypes.ADD_NEW_OFFICE_FAILED,
    payload: errmess,
});

export const officeEditingFailed = (errmess) => ({
    type: ActionTypes.EDIT_OFFICE_FAILED,
    payload: errmess,
});

export const officeDeletingFailed = (errmess) => ({
    type: ActionTypes.DELETE_OFFICE_FAILED,
    payload: errmess,
});
