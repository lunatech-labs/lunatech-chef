import * as ActionTypes from "./LocationsActionTypes";
import { axiosInstance } from "../Axios";
import {
  fetchSchedules,
  fetchSchedulesAttendance,
} from "../schedules/SchedulesActionCreators";
import { fetchAttendanceUser } from "../attendance/AttendanceActionCreators";

export const fetchLocations = () => (dispatch) => {
  dispatch(locationsLoading(true));

  axiosInstance
    .get("/locations")
    .then(function (response) {
      dispatch(showAllLocations(response));
    })
    .catch(function (error) {
      console.log("Failed loading Locations: " + error);
      dispatch(locationsLoadingFailed(error.message));
    });
};

export const addNewLocation = (newLocation) => (dispatch) => {
  const locationToAdd = {
    city: newLocation.city,
    country: newLocation.country,
  };

  axiosInstance
    .post("/locations", locationToAdd)
    .then((response) => {
      dispatch(fetchLocations());
    })
    .catch(function (error) {
      console.log("Failed adding Location: " + error);
      dispatch(locationAddingFailed(error.message));
    });
};

export const editLocation = (editedLocation) => (dispatch) => {
  const locationToEdit = {
    city: editedLocation.city,
    country: editedLocation.country,
  };

  const userUuid = localStorage.getItem("userUuid");
  axiosInstance
    .put("/locations/" + editedLocation.uuid, locationToEdit)
    .then((response) => {
      dispatch(fetchLocations());
      dispatch(fetchSchedules());
      dispatch(fetchSchedulesAttendance());
      dispatch(fetchAttendanceUser(userUuid));
    })
    .catch(function (error) {
      console.log("Failed editing Location: " + error);
      dispatch(locationEditingFailed(error.message));
    });
};

export const deleteLocation = (locationUuid) => (dispatch) => {
  const userUuid = localStorage.getItem("userUuid");
  axiosInstance
    .delete("/locations/" + locationUuid)
    .then((response) => {
      dispatch(fetchLocations());
      dispatch(fetchSchedules());
      dispatch(fetchSchedulesAttendance());
      dispatch(fetchAttendanceUser(userUuid));

    })
    .catch(function (error) {
      console.log("Failed removing Location: " + error);
      dispatch(locationDeletingFailed(error.message));
    });
};

export const locationsLoading = () => ({
  type: ActionTypes.LOCATIONS_LOADING,
});

export const showAllLocations = (locations) => ({
  type: ActionTypes.SHOW_ALL_LOCATIONS,
  payload: locations.data,
});

export const locationsLoadingFailed = (errmess) => ({
  type: ActionTypes.LOCATIONS_LOADING_FAILED,
  payload: errmess,
});

export const locationAddingFailed = (errmess) => ({
  type: ActionTypes.ADD_NEW_LOCATION_FAILED,
  payload: errmess,
});

export const locationEditingFailed = (errmess) => ({
  type: ActionTypes.EDIT_LOCATION_FAILED,
  payload: errmess,
});

export const locationDeletingFailed = (errmess) => ({
  type: ActionTypes.DELETE_LOCATION_FAILED,
  payload: errmess,
});
