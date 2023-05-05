import { createSlice } from '@reduxjs/toolkit'

const initState = {
    isLoading: false,
    attendance: [],
    errorListing: null,
    errorEditing: null,
};

// https://redux.js.org/usage/migrating-to-modern-redux
const attendancesSlice = createSlice({
    name: 'attendances',
    initialState: initState,
    reducers: {
        allAttendancesLoading(state, action) {
            state.isLoading = true
        },
        allAttendancesLoadingFailed(state, action) {
            state.isLoading = false;
            state.errorListing = action.payload;
        },
        allAttendancesShown(state, action) {
            state.isLoading = false;
            state.attendance = action.payload.data
        },
        newAttendanceShown(state, action) {
            state.isLoading = false;
            state.attendance = action.payload
        },
        attendanceEditedFailed(state, action) {
            state.errorEditing = action.payload;
        },
    }
})

export const { allAttendancesLoading, allAttendancesLoadingFailed, allAttendancesShown, newAttendanceShown, attendanceEditedFailed } = attendancesSlice.actions

export default attendancesSlice.reducer