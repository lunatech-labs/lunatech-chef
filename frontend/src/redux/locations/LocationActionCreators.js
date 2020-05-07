import * as ActionTypes from "./LocationActionTypes";
import { baseUrl } from "../../shared/baseUrl";

const axios = require("axios").default;

export const fetchLocations = () => (dispatch) => {
  dispatch(locationsLoading(true));

  axios
    .get(baseUrl + "/locations")
    .then(function (response) {
      dispatch(showAllLocations(response));
    })
    .catch(function (error) {
      console.log("Failed loading locations:" + error);
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
