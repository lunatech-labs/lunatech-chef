import { userLoggedIn, userLoggedInFailed, userLoggedOut, userUpdatedProfile, userUpdatedProfileFailed } from "./UsersSlice";
import { axiosInstance } from "../Axios";
import { fetchDishes } from "../dishes/DishesActionCreators";
import { fetchOffices } from "../offices/OfficesActionCreators";
import { fetchMenus } from "../menus/MenusActionCreators";
import {
    fetchSchedules,
    fetchRecurrentSchedules,
    fetchSchedulesAttendance,
} from "../schedules/SchedulesActionCreators";
import { fetchAttendanceUser } from "../attendance/AttendanceActionCreators";
import { STORAGE_USER_UUID } from "../LocalStorageKeys";

export const login = (token) => (dispatch) => {
    const authHeader = {
        headers: {
            Authorization: "BEARER " + token,
        }
    };

    axiosInstance
        .get("/login", authHeader)
        .then((response) => {
            configureAxios(response);
            dispatch(userLoggedIn(response.data));

            const userUuid = response.data.uuid;
            localStorage.setItem(STORAGE_USER_UUID, userUuid);
            getInitialData(dispatch);
        })
        .catch(function (error) {
            console.log("Failed logging in user " + error);
            dispatch(userLoggedInFailed(error.message));
        });
};

let requestInterceptorId = null;

const configureAxios = (response) => {
    if (requestInterceptorId !== null) {
        axiosInstance.interceptors.request.eject(requestInterceptorId);
    }
    requestInterceptorId = axiosInstance.interceptors.request.use(
        function (config) {
            config.headers = {
                ...config.headers,
                CHEF_SESSION: response.headers.chef_session,
            };
            return config;
        },
        function (error) {
            return Promise.reject(error);
        }
    );
};

export const saveUserProfile = (userUuid, userProfile) => (dispatch) => {
    const userProfileToSave = {
        officeUuid: userProfile.officeUuid,
        isVegetarian: userProfile.isVegetarian,
        hasNutsRestriction: userProfile.hasNutsRestriction,
        hasHalalRestriction: userProfile.hasHalalRestriction,
        hasSeafoodRestriction: userProfile.hasSeafoodRestriction,
        hasPorkRestriction: userProfile.hasPorkRestriction,
        hasBeefRestriction: userProfile.hasBeefRestriction,
        isGlutenIntolerant: userProfile.isGlutenIntolerant,
        isLactoseIntolerant: userProfile.isLactoseIntolerant,
        otherRestrictions: userProfile.otherRestrictions,
    };

    axiosInstance
        .put("/users/" + userUuid, userProfileToSave)
        .then(() => {
            dispatch(fetchSchedulesAttendance());
            dispatch(userUpdatedProfile(userProfileToSave));
        })
        .catch(function (error) {
            console.log("Failed saving user profile: " + error);
            dispatch(userUpdatedProfileFailed(error.message));
        });
};

const getInitialData = (dispatch) => {
    dispatch(fetchOffices());
    dispatch(fetchDishes());
    dispatch(fetchMenus());
    dispatch(fetchSchedules());
    dispatch(fetchRecurrentSchedules());
    dispatch(fetchAttendanceUser());
    dispatch(fetchSchedulesAttendance());
};

export const logout = () => (dispatch) => {
    dispatch(userLoggedOut());
};
