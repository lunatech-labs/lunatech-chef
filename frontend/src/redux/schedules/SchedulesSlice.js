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
        },
        allSchedulesLoadingFailed(state, action) {
            state.isLoading = false;
            state.errorListing = action.payload;
        },
        allSchedulesShown(state, action) {
            state.isLoading = false;
            state.schedules = action.payload.data
        },
        scheduleAddedFailed(state, action) {
            state.errorAdding = action.payload;
        },
        scheduleEditedFailed(state, action) {
            state.errorEditing = action.payload;
        },
        scheduleDeletedFailed(state, action) {
            state.errorDeleting = action.payload;
        },

        // recurrent schedules
        allRecurrentSchedulesShown(state, action) {
            state.isLoading = false;
            state.recurrentSchedules = action.payload.data
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
            state.attendance = action.payload.data
        }
    }
})

export const { allSchedulesLoading, allSchedulesLoadingFailed, allSchedulesShown, scheduleAddedFailed, scheduleEditedFailed, scheduleDeletedFailed, allRecurrentSchedulesShown, allSchedulesAttendanceLoading, allSchedulesAttendanceLoadingFailed, allSchedulesAttendanceShown } = schedulesSlice.actions

export default schedulesSlice.reducer