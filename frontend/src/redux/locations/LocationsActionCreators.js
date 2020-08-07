import * as ActionTypes from "./LocationsActionTypes";
import { axiosInstance } from "../Axios";

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
      console.log("New Location added with response " + response);
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

  axiosInstance
    .put("/locations/" + editedLocation.uuid, locationToEdit)
    .then((response) => {
      console.log("Location edited with response " + response);
      dispatch(fetchLocations());
    })
    .catch(function (error) {
      console.log("Failed editing Location: " + error);
      dispatch(locationEditingFailed(error.message));
    });
};

export const deleteLocation = (locationUuid) => (dispatch) => {
  axiosInstance
    .delete("/locations/" + locationUuid)
    .then((response) => {
      console.log("Location deleted with response: " + response);
      dispatch(fetchLocations());
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
