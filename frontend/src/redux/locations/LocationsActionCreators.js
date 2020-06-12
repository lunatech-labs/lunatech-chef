import * as ActionTypes from "./LocationsActionTypes";

const axios = require("axios").default;

export const fetchLocations = () => (dispatch) => {
  dispatch(locationsLoading(true));

  axios
    .get(process.env.REACT_APP_BASE_URL + "/locations")
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
  // console.log("i have a token " + userToken);

  // axios.defaults.headers.common = { Authorization: `Bearer ${userToken}` };

  const locationToAdd = {
    city: newLocation.city,
    country: newLocation.country,
  };

  axios
    .post(process.env.REACT_APP_BASE_URL + "/locations", locationToAdd)
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
  axios
    .delete(process.env.REACT_APP_BASE_URL + "/locations/" + locationUuid)
    .then((response) => {
      console.log("Location deleted with response" + response);
      dispatch(fetchLocations());
    })
    .catch(function (error) {
      console.log("Failed removing Location: " + error);
      dispatch(loadingLocationsFailed(error.message));
    });
};
