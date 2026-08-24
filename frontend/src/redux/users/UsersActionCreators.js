import { userLoggedIn, userLoggedInFailed, userLoggedOut, userUpdatedProfile, userUpdatedProfileFailed, allUsersLoading, allUsersShown, allUsersLoadingFailed, userEditedFailed, userDeletedFailed } from "./UsersSlice";
import { axiosInstance, onUnauthorized } from "../Axios";
import { fetchDishes } from "../dishes/DishesActionCreators";
import { fetchOffices } from "../offices/OfficesActionCreators";
import { fetchMenus } from "../menus/MenusActionCreators";
import {
    fetchSchedules,
    fetchRecurrentSchedules,
    fetchSchedulesAttendance,
    fetchSchedulesExternalAttendance
} from "../schedules/SchedulesActionCreators";
import { fetchAttendanceUser } from "../attendance/AttendanceActionCreators";
import { STORAGE_USER_UUID } from "../LocalStorageKeys";

export const login = () => (dispatch) => {
    onUnauthorized(() => dispatch(userLoggedOut()));

    axiosInstance
        .get("/me")
        .then((response) => {
            dispatch(userLoggedIn(response.data));

            const userUuid = response.data.uuid;
            localStorage.setItem(STORAGE_USER_UUID, userUuid);
            getInitialData(dispatch);
            if (response.data.isAdmin) {
                dispatch(fetchAllUsers());
            }
        })
        .catch(function (error) {
            console.log("Failed logging in user " + error);
            dispatch(userLoggedInFailed(error.message));
        });
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
        optOutLunches: userProfile.optOutLunches,
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
    dispatch(fetchSchedulesExternalAttendance());
};

export const fetchAllUsers = () => (dispatch) => {
    dispatch(allUsersLoading(true));

    axiosInstance
        .get("/users")
        .then((response) => {
            dispatch(allUsersShown(response.data));
        })
        .catch(function (error) {
            console.log("Failed loading Users: " + error);
            dispatch(allUsersLoadingFailed(error.message));
        });
};

export const editUser = (editedUser) => (dispatch) => {
    const userToSave = {
        officeUuid: editedUser.officeUuid,
        isVegetarian: editedUser.isVegetarian,
        hasHalalRestriction: editedUser.hasHalalRestriction,
        hasNutsRestriction: editedUser.hasNutsRestriction,
        hasSeafoodRestriction: editedUser.hasSeafoodRestriction,
        hasPorkRestriction: editedUser.hasPorkRestriction,
        hasBeefRestriction: editedUser.hasBeefRestriction,
        isGlutenIntolerant: editedUser.isGlutenIntolerant,
        isLactoseIntolerant: editedUser.isLactoseIntolerant,
        otherRestrictions: editedUser.otherRestrictions,
        optOutLunches: editedUser.optOutLunches,
    };

    axiosInstance
        .put("/users/" + editedUser.uuid, userToSave)
        .then(() => {
            dispatch(fetchAllUsers());
        })
        .catch(function (error) {
            console.log("Failed editing User: " + error);
            dispatch(userEditedFailed(error.message));
        });
};

export const deleteUser = (userUuid) => (dispatch) => {
    axiosInstance
        .delete("/users/" + userUuid)
        .then(() => {
            dispatch(fetchAllUsers());
        })
        .catch(function (error) {
            console.log("Failed removing User: " + error);
            dispatch(userDeletedFailed(error.message));
        });
};

export const logout = () => (dispatch) => {
    dispatch(userLoggedOut());
};
