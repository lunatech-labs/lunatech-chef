import axios from "axios";
import { User } from "oidc-client-ts";

export const axiosInstance = axios.create({
  baseURL: import.meta.env.REACT_APP_BASE_URL,
  timeout: 10000,
});

const oidcStorageKey =
  `oidc.user:${import.meta.env.REACT_APP_REALMS_URL}:${import.meta.env.REACT_APP_CLIENT_ID}`;

const getAccessToken = () => {
  const stored = sessionStorage.getItem(oidcStorageKey);
  return stored ? User.fromStorageString(stored).access_token : null;
};

axiosInstance.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let unauthorizedHandler = null;

// Called once at login with a handler that logs the user out of the app state.
export const onUnauthorized = (handler) => {
  unauthorizedHandler = handler;
};

axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && unauthorizedHandler) {
      unauthorizedHandler();
    }
    return Promise.reject(error);
  }
);
