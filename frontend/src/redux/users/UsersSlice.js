import { createSlice } from '@reduxjs/toolkit'

const initState = {
    isAuthenticated: false,
    isAdmin: false,
    uuid: "",
    name: "",
    emailAddress: "",
    officeUuid: "",
    isVegetarian: false,
    hasHalalRestriction: false,
    hasNutsRestriction: false,
    hasSeafoodRestriction: false,
    hasPorkRestriction: false,
    hasBeefRestriction: false,
    isGlutenIntolerant: false,
    isLactoseIntolerant: false,
    otherRestrictions: false,
    error: null,
};

// https://redux.js.org/usage/migrating-to-modern-redux
const usersSlice = createSlice({
    name: 'users',
    initialState: initState,
    reducers: {
        userLoggedIn(state, action) {
            state.isAuthenticated = true;
            state.isAdmin = action.payload.isAdmin;
            state.uuid = action.payload.uuid;
            state.name = action.payload.name;
            state.emailAddress = action.payload.emailAddress;
            state.officeUuid = action.payload.officeUuid;
            state.isVegetarian = action.payload.isVegetarian;
            state.hasHalalRestriction = action.payload.hasHalalRestriction;
            state.hasNutsRestriction = action.payload.hasNutsRestriction;
            state.hasSeafoodRestriction = action.payload.hasSeafoodRestriction;
            state.hasPorkRestriction = action.payload.hasPorkRestriction;
            state.hasBeefRestriction = action.payload.hasBeefRestriction;
            state.isGlutenIntolerant = action.payload.isGlutenIntolerant;
            state.isLactoseIntolerant = action.payload.isLactoseIntolerant;
            state.otherRestrictions = action.payload.otherRestrictions;
            state.error = null;
        },
        userLoggedInFailed(state, action) {
            state.isAuthenticated = false;
            state.error = action.payload;
        },
        userLoggedOut(state, action) {
            state.isAuthenticated = false;
            state.error = null;
        },
        userUpdatedProfile(state, action) {
            state.officeUuid = action.payload.officeUuid;
            state.isVegetarian = action.payload.isVegetarian;
            state.hasHalalRestriction = action.payload.hasHalalRestriction;
            state.hasNutsRestriction = action.payload.hasNutsRestriction;
            state.hasSeafoodRestriction = action.payload.hasSeafoodRestriction;
            state.hasPorkRestriction = action.payload.hasPorkRestriction;
            state.hasBeefRestriction = action.payload.hasBeefRestriction;
            state.isGlutenIntolerant = action.payload.isGlutenIntolerant;
            state.isLactoseIntolerant = action.payload.isLactoseIntolerant;
            state.otherRestrictions = action.payload.otherRestrictions;
            state.error = null;
        },
        userUpdatedProfileFailed(state, action) {
            state.error = action.payload;
        },
    }
})

export const { userLoggedIn, userLoggedInFailed, userLoggedOut, userUpdatedProfile, userUpdatedProfileFailed } = usersSlice.actions

export default usersSlice.reducer