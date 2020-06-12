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
      console.log("Failed loading Locations:" + error);
      dispatch(loadingLocationsFailed(error.message));
    });
};

export const locationsLoading = () => ({
  type: ActionTypes.LOCATIONS_LOADING,
});

export const loadingLocationsFailed = (errmess) => ({
  type: ActionTypes.LOCATIONS_LOADING_FAILED,
  payload: errmess,
});

export const showAllLocations = (locations) => ({
  type: ActionTypes.SHOW_ALL_LOCATIONS,
  payload: locations,
});

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
      dispatch(loadingLocationsFailed(error.message));
    });
};

export const deleteLocation = (locationUuid) => (dispatch) => {
  axiosInstance
    .delete("/locations/" + locationUuid)
    .then((response) => {
      console.log("Location deleted with response" + response);
      dispatch(fetchLocations());
    })
    .catch(function (error) {
      console.log("Failed removing Location: " + error);
      dispatch(loadingLocationsFailed(error.message));
    });
};
