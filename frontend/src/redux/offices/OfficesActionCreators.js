import { allOfficesLoading, allOfficesShown, allOfficesLoadingFailed, officeAddedFailed, officeEditedFailed, officeDeletedFailed } from "./OfficesSlice";
import { axiosInstance } from "../Axios";
import {
    fetchSchedules,
    fetchSchedulesAttendance,
} from "../schedules/SchedulesActionCreators";
import { fetchAttendanceUser } from "../attendance/AttendanceActionCreators";
import { STORAGE_USER_UUID } from "../LocalStorageKeys";

export const fetchOffices = () => (dispatch) => {
    dispatch(allOfficesLoading(true));

    axiosInstance
        .get("/offices")
        .then(function (response) {
            dispatch(allOfficesShown(response.data));
        })
        .catch(function (error) {
            console.log("Failed loading Offices: " + error);
            dispatch(allOfficesLoadingFailed(error.message));
        });
};

export const addNewOffice = (newOffice) => (dispatch) => {
    const officeToAdd = {
        city: newOffice.city,
        country: newOffice.country,
    };

    axiosInstance
        .post("/offices", officeToAdd)
        .then(() => {
            dispatch(fetchOffices());
        })
        .catch(function (error) {
            console.log("Failed adding Office: " + error);
            dispatch(officeAddedFailed(error.message));
        });
};

export const editOffice = (editedOffice) => (dispatch) => {
    const officeToEdit = {
        city: editedOffice.city,
        country: editedOffice.country,
    };

    const userUuid = localStorage.getItem(STORAGE_USER_UUID);
    axiosInstance
        .put("/offices/" + editedOffice.uuid, officeToEdit)
        .then(() => {
            dispatch(fetchOffices());
            dispatch(fetchSchedules());
            dispatch(fetchSchedulesAttendance());
            dispatch(fetchAttendanceUser(userUuid));
        })
        .catch(function (error) {
            console.log("Failed editing Office: " + error);
            dispatch(officeEditedFailed(error.message));
        });
};

export const deleteOffice = (officeUuid) => (dispatch) => {
    const userUuid = localStorage.getItem(STORAGE_USER_UUID);
    axiosInstance
        .delete("/offices/" + officeUuid)
        .then(() => {
            dispatch(fetchOffices());
            dispatch(fetchSchedules());
            dispatch(fetchSchedulesAttendance());
            dispatch(fetchAttendanceUser(userUuid));
        })
        .catch(function (error) {
            console.log("Failed removing Office: " + error);
            dispatch(officeDeletedFailed(error.message));
        });
};
