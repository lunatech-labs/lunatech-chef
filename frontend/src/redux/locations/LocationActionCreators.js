import * as ActionTypes from "./LocationActionTypes";
import { baseUrl } from "../../shared/baseUrl";

const axios = require("axios").default;

axios.interceptors.request.use((request) => {
  console.log("Starting Request", request);
  return request;
});

export const fetchLocations = () => (dispatch) => {
  dispatch(locationsLoading(true));

  axios
    .get(baseUrl + "/locations")
    .then(function (response) {
      dispatch(showAllLocations(response));
    })
    .catch(function (error) {
      console.log("Failed loading locations: " + error);
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

export const addNewlocation = (newLocation) => (dispatch) => {
  const locationToAdd = {
    city: newLocation.city,
    country: newLocation.country,
  };

  axios
    .post(baseUrl + "/locations", locationToAdd)
    .then((response) => {
      console.log("New location added with response " + response);
      dispatch(fetchLocations());
    })
    .catch(function (error) {
      //TODO mostrar o erro
      console.log("Failed adding location: " + error);
    });
};

export const deleteLocation = (locationUuid) => (dispatch) => {
  axios
    .delete(baseUrl + "/locations/" + locationUuid)
    .then((response) => {
      console.log("Location deleted with response" + response);
      dispatch(fetchLocations());
    })
    .catch(function (error) {
      //TODO mostrar o erro
      console.log("Failed removing location: " + error);
    });
};
