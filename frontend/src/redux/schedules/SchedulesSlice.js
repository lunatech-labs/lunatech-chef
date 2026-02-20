import { createSlice } from '@reduxjs/toolkit'

const initState = {
    isLoading: false,
    isLoadingAttendance: false,
    schedules: [],
    recurrentSchedules: [],
    attendance: [],
    errorListing: null,
    errorListingAttendance: null,
    errorAdding: null,
    errorEditing: null,
    errorDeleting: null,
};

// https://redux.js.org/usage/migrating-to-modern-redux
const schedulesSlice = createSlice({
    name: 'schedules',
    initialState: initState,
    reducers: {
        // schedules
        allSchedulesLoading(state, action) {
            state.isLoading = true
            state.errorListing = null;
            state.errorAdding = null;
            state.errorEditing = null;
            state.errorDeleting = null;
        },
        allSchedulesLoadingFailed(state, action) {
            state.isLoading = false;
            state.errorAdding = null;
            state.errorEditing = null;
            state.errorDeleting = null;
            state.errorListing = action.payload;
        },
        allSchedulesShown(state, action) {
            state.isLoading = false;
            state.errorListing = null;
            state.errorAdding = null;
            state.errorEditing = null;
            state.errorDeleting = null;
            state.schedules = action.payload
        },
        scheduleAddedFailed(state, action) {
            state.isLoading = false;
            state.errorListing = null;
            state.errorEditing = null;
            state.errorDeleting = null;
            state.errorAdding = action.payload;
        },
        scheduleEditedFailed(state, action) {
            state.isLoading = false;
            state.errorListing = null;
            state.errorDeleting = null;
            state.errorEditing = action.payload;
        },
        scheduleDeletedFailed(state, action) {
            state.isLoading = false;
            state.errorListing = null;
            state.errorAdding = null;
            state.errorEditing = null;
            state.errorDeleting = action.payload;
        },

        // recurrent schedules
        allRecurrentSchedulesShown(state, action) {
            state.isLoading = false;
            state.errorListing = null;
            state.errorAdding = null;
            state.errorEditing = null;
            state.errorDeleting = null;
            state.recurrentSchedules = action.payload
        },

        // schedules attendance
        allSchedulesAttendanceLoading(state, action) {
            state.isLoadingAttendance = true
        },
        allSchedulesAttendanceLoadingFailed(state, action) {
            state.errorListingAttendance = action.payload
        },
        allSchedulesAttendanceShown(state, action) {
            state.isLoadingAttendance = false
            state.attendance = action.payload
        }
    }
})

export const { allSchedulesLoading, allSchedulesLoadingFailed, allSchedulesShown, scheduleAddedFailed, scheduleEditedFailed, scheduleDeletedFailed, allRecurrentSchedulesShown, allSchedulesAttendanceLoading, allSchedulesAttendanceLoadingFailed, allSchedulesAttendanceShown } = schedulesSlice.actions

export default schedulesSlice.reducer
